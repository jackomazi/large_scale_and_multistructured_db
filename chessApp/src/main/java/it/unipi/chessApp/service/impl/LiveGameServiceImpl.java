package it.unipi.chessApp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import it.unipi.chessApp.dto.GameDTO;
import it.unipi.chessApp.dto.GameStatusDTO;
import it.unipi.chessApp.dto.GameSummaryDTO;
import it.unipi.chessApp.dto.MatchmakingResultDTO;
import it.unipi.chessApp.dto.MoveResultDTO;
import it.unipi.chessApp.model.User;
import it.unipi.chessApp.model.ChessOpening;
import it.unipi.chessApp.model.LiveGameState;
import it.unipi.chessApp.repository.TournamentRepository;
import it.unipi.chessApp.repository.UserRepository;
import it.unipi.chessApp.service.GameService;
import it.unipi.chessApp.service.LiveGameService;
import it.unipi.chessApp.service.OpeningService;
import it.unipi.chessApp.service.TournamentService;
import it.unipi.chessApp.service.UserService;
import it.unipi.chessApp.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import org.bson.types.ObjectId;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveGameServiceImpl implements LiveGameService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final OpeningService openingService;
    private final GameService gameService;
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final TournamentService tournamentService;

    private static final String MATCHMAKING_QUEUE_KEY = "chess:matchmaking:queue";
    private static final String TOURNAMENT_QUEUE_PREFIX = "chess:matchmaking:tournament:";
    private static final String GAME_STATE_PREFIX = "chess:game:";
    private static final String PLAYER_GAME_PREFIX = "chess:player:game:";
    private static final String TOURNAMENT_GAME_COUNT_PREFIX = "chess:tournament:";

    @Value("${live-game.expiration-hours:24}")
    private int gameExpirationHours;

    @Value("${live-game.max-tournament-games:8}")
    private int maxTournamentGames;

    @Value("${live-game.matchmaking-timeout-seconds:60}")
    private int matchmakingTimeoutSeconds;

    @Value("${chess.openings.max-move-check:30}")
    private int maxMoveCheckForOpening;

    private static final int POLL_INTERVAL_MS = 500;

    @Override
    public MatchmakingResultDTO joinMatchmaking(String username, String tournamentId) throws BusinessException {
        try {
            String existingGameId = redisTemplate.opsForValue().get(PLAYER_GAME_PREFIX + username);
            if (existingGameId != null) {
                LiveGameState existingGame = getGameState(existingGameId);
                if (existingGame != null && LiveGameState.STATUS_IN_PROGRESS.equals(existingGame.getStatus())) {
                    throw new BusinessException("You are already in an active game: " + existingGameId);
                }
            }

            boolean isTournament = tournamentId != null && !tournamentId.isEmpty();

            if (isTournament) {
                if (!tournamentRepository.existsById(tournamentId)) {
                    throw new BusinessException("Tournament not found: " + tournamentId);
                }

                // Check if player is subscribed to the tournament
                String subscribersKey = "chess:tournament:" + tournamentId + ":subscribers";
                Boolean isSubscribed = redisTemplate.opsForSet().isMember(subscribersKey, username);
                if (!Boolean.TRUE.equals(isSubscribed)) {
                    throw new BusinessException("You are not subscribed to this tournament");
                }

                int gameCount = getTournamentGameCount(tournamentId, username);
                if (gameCount >= maxTournamentGames) {
                    throw new BusinessException("You have reached the maximum of " + maxTournamentGames + " games in this tournament");
                }
            }

            String queueKey = isTournament ? TOURNAMENT_QUEUE_PREFIX + tournamentId : MATCHMAKING_QUEUE_KEY;

            String opponent = redisTemplate.opsForList().leftPop(queueKey);

            if (opponent != null && !opponent.equals(username)) {
                // Match found immediately - create the game
                return createGameForMatch(username, opponent, tournamentId, queueKey);
            } else {
                // No opponent available - add to queue and wait
                if (opponent != null && opponent.equals(username)) {
                    redisTemplate.opsForList().leftPush(queueKey, username);
                }

                Long position = redisTemplate.opsForList().indexOf(queueKey, username);
                if (position == null || position < 0) {
                    redisTemplate.opsForList().rightPush(queueKey, username);
                }

                // Poll for match
                long startTime = System.currentTimeMillis();
                long timeoutMs = matchmakingTimeoutSeconds * 1000L;

                while (System.currentTimeMillis() - startTime < timeoutMs) {
                    // Check if another player matched us
                    String matchedGameId = redisTemplate.opsForValue().get(PLAYER_GAME_PREFIX + username);
                    if (matchedGameId != null) {
                        LiveGameState gameState = getGameState(matchedGameId);
                        if (gameState != null && LiveGameState.STATUS_IN_PROGRESS.equals(gameState.getStatus())) {
                            log.info("User {} was matched to game {} by another player", username, matchedGameId);
                            return new MatchmakingResultDTO(
                                matchedGameId,
                                gameState.getWhitePlayer(),
                                gameState.getBlackPlayer(),
                                gameState.getTournamentId(),
                                true,
                                "Match found! Game started."
                            );
                        }
                    }

                    try {
                        Thread.sleep(POLL_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new BusinessException("Matchmaking interrupted");
                    }
                }

                // Timeout expired - remove from queue
                redisTemplate.opsForList().remove(queueKey, 0, username);
                log.info("Matchmaking timeout for user {}. Removed from queue.", username);
                
                return new MatchmakingResultDTO(
                    null,
                    null,
                    null,
                    tournamentId,
                    false,
                    "No opponent found. Removed from queue."
                );
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in matchmaking for user: {}", username, e);
            throw new BusinessException("Error joining matchmaking");
        }
    }

    private MatchmakingResultDTO createGameForMatch(String username, String opponent, String tournamentId, String queueKey) throws BusinessException {
        boolean isTournament = tournamentId != null && !tournamentId.isEmpty();
        
        String gameId = new ObjectId().toHexString();
        LiveGameState gameState = LiveGameState.createNew(gameId, opponent, username, tournamentId);
        saveGameState(gameState);

        redisTemplate.opsForValue().set(
            PLAYER_GAME_PREFIX + opponent,
            gameId,
            gameExpirationHours,
            TimeUnit.HOURS
        );
        redisTemplate.opsForValue().set(
            PLAYER_GAME_PREFIX + username,
            gameId,
            gameExpirationHours,
            TimeUnit.HOURS
        );

        if (isTournament) {
            log.info("Incrementing tournament game count for opponent: {} and username: {} in tournament: {}", opponent, username, tournamentId);
            incrementTournamentGameCount(tournamentId, opponent);
            incrementTournamentGameCount(tournamentId, username);
        }

        log.info("Match created: {} (white: {}, black: {}, tournament: {})", gameId, opponent, username, tournamentId);

        return new MatchmakingResultDTO(
            gameId,
            opponent,
            username,
            tournamentId,
            true,
            "Match found! Game started."
        );
    }

    @Override
    public void leaveMatchmaking(String username, String tournamentId) throws BusinessException {
        try {
            String queueKey = (tournamentId != null && !tournamentId.isEmpty())
                ? TOURNAMENT_QUEUE_PREFIX + tournamentId
                : MATCHMAKING_QUEUE_KEY;

            redisTemplate.opsForList().remove(queueKey, 0, username);
            log.info("User {} left matchmaking queue (tournament: {})", username, tournamentId);
        } catch (Exception e) {
            log.error("Error leaving matchmaking for user: {}", username, e);
            throw new BusinessException("Error leaving matchmaking");
        }
    }

    @Override
    public MoveResultDTO makeMove(String gameId, String username, String move) throws BusinessException {
        try {
            LiveGameState gameState = getGameState(gameId);

            if (gameState == null) {
                return MoveResultDTO.error("Game not found: " + gameId);
            }

            if (!LiveGameState.STATUS_IN_PROGRESS.equals(gameState.getStatus())) {
                return MoveResultDTO.error("Game has already ended. Status: " + gameState.getStatus());
            }

            boolean isWhiteTurn = gameState.isWhiteTurn();
            boolean isWhitePlayer = username.equals(gameState.getWhitePlayer());
            boolean isBlackPlayer = username.equals(gameState.getBlackPlayer());

            if (!isWhitePlayer && !isBlackPlayer) {
                return MoveResultDTO.error("You are not a participant in this game");
            }

            if ((isWhiteTurn && !isWhitePlayer) || (!isWhiteTurn && !isBlackPlayer)) {
                String currentTurnPlayer = isWhiteTurn ? gameState.getWhitePlayer() : gameState.getBlackPlayer();
                return MoveResultDTO.error("Not your turn. Waiting for " + currentTurnPlayer);
            }

            Board board = new Board();
            board.loadFromFen(gameState.getFen());

            Move parsedMove;
            try {
                parsedMove = new Move(move, gameState.isWhiteTurn() ? Side.WHITE : Side.BLACK);
            } catch (Exception e) {
                return MoveResultDTO.error("Invalid move notation: " + move);
            }

            if (!board.legalMoves().contains(parsedMove)) {
                return MoveResultDTO.error("Illegal move: " + move);
            }

            board.doMove(parsedMove);

            gameState.setFen(board.getFen());
            gameState.setLastMove(move);
            gameState.setLastMoveAt(System.currentTimeMillis());
            
            // Track move history in UCI notation (will be converted to SAN when saving)
            gameState.addMove(move);
            
            // Check for opening detection (only in the first N moves)
            if (gameState.getMoveCount() <= maxMoveCheckForOpening) {
                detectOpening(gameState, board.getFen());
            }

            String outcome = MoveResultDTO.OUTCOME_MOVE_MADE;
            String gameStatus = LiveGameState.STATUS_IN_PROGRESS;

            if (board.isMated()) {
                outcome = MoveResultDTO.OUTCOME_CHECKMATE;
                gameStatus = isWhiteTurn ? LiveGameState.STATUS_WHITE_WINS : LiveGameState.STATUS_BLACK_WINS;
            } else if (board.isStaleMate()) {
                outcome = MoveResultDTO.OUTCOME_STALEMATE;
                gameStatus = LiveGameState.STATUS_STALEMATE;
            } else if (board.isDraw()) {
                outcome = MoveResultDTO.OUTCOME_DRAW;
                gameStatus = LiveGameState.STATUS_DRAW;
            } else if (board.isKingAttacked()) {
                outcome = MoveResultDTO.OUTCOME_CHECK;
            }

            gameState.setStatus(gameStatus);
            saveGameState(gameState);

            if (!LiveGameState.STATUS_IN_PROGRESS.equals(gameStatus)) {
                // Game ended - save to MongoDB and cleanup
                saveCompletedGameToMongoDB(gameState);
                redisTemplate.delete(PLAYER_GAME_PREFIX + gameState.getWhitePlayer());
                redisTemplate.delete(PLAYER_GAME_PREFIX + gameState.getBlackPlayer());
            }

            String nextTurn = board.getSideToMove() == Side.WHITE ? "WHITE" : "BLACK";

            return MoveResultDTO.success(outcome, board.getFen(), nextTurn, gameStatus, gameState.getDetectedOpening());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error making move in game {}: {}", gameId, move, e);
            throw new BusinessException("Error making move");
        }
    }

    @Override
    public GameStatusDTO getGameStatus(String gameId) throws BusinessException {
        try {
            LiveGameState gameState = getGameState(gameId);

            if (gameState == null) {
                throw new BusinessException("Game not found: " + gameId);
            }

            String currentTurn = gameState.isWhiteTurn() ? "WHITE" : "BLACK";

            return new GameStatusDTO(
                gameState.getGameId(),
                gameState.getFen(),
                currentTurn,
                gameState.getWhitePlayer(),
                gameState.getBlackPlayer(),
                gameState.getStatus(),
                gameState.getLastMove(),
                gameState.getTournamentId(),
                gameState.getLastMoveAt(),
                gameState.getDetectedOpening(),
                gameState.getDetectedOpeningEco()
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting game status: {}", gameId, e);
            throw new BusinessException("Error getting game status");
        }
    }

    @Override
    public void resignGame(String gameId, String username) throws BusinessException {
        try {
            LiveGameState gameState = getGameState(gameId);

            if (gameState == null) {
                throw new BusinessException("Game not found: " + gameId);
            }

            if (!LiveGameState.STATUS_IN_PROGRESS.equals(gameState.getStatus())) {
                throw new BusinessException("Game has already ended");
            }

            boolean isWhitePlayer = username.equals(gameState.getWhitePlayer());
            boolean isBlackPlayer = username.equals(gameState.getBlackPlayer());

            if (!isWhitePlayer && !isBlackPlayer) {
                throw new BusinessException("You are not a participant in this game");
            }

            gameState.setStatus(isWhitePlayer ? LiveGameState.STATUS_BLACK_WINS : LiveGameState.STATUS_WHITE_WINS);
            gameState.setLastMoveAt(System.currentTimeMillis());
            saveGameState(gameState);

            // Save to MongoDB before cleanup
            saveCompletedGameToMongoDB(gameState);

            redisTemplate.delete(PLAYER_GAME_PREFIX + gameState.getWhitePlayer());
            redisTemplate.delete(PLAYER_GAME_PREFIX + gameState.getBlackPlayer());

            log.info("Player {} resigned from game {}", username, gameId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error resigning game {}: {}", gameId, username, e);
            throw new BusinessException("Error resigning game");
        }
    }

    /**
     * Detect and update the opening based on the current board position.
     */
    private void detectOpening(LiveGameState gameState, String fen) {
        try {
            ChessOpening opening = openingService.findOpeningByFen(fen);
            if (opening != null) {
                gameState.setDetectedOpening(opening.getName());
                gameState.setDetectedOpeningEco(opening.getEco());
                log.debug("Opening detected for game {}: {} ({})", 
                         gameState.getGameId(), opening.getName(), opening.getEco());
            }
        } catch (Exception e) {
            log.warn("Error detecting opening for game {}: {}", gameState.getGameId(), e.getMessage());
        }
    }

    /**
     * Save a completed game to MongoDB.
     */
    private void saveCompletedGameToMongoDB(LiveGameState gameState) {
        try {
            // Get users by username first to retrieve their ratings
            User whiteUser = userRepository.findByUsername(gameState.getWhitePlayer())
                    .orElse(null);
            User blackUser = userRepository.findByUsername(gameState.getBlackPlayer())
                    .orElse(null);

            GameDTO gameDTO = new GameDTO();
            gameDTO.setId(gameState.getGameId());
            gameDTO.setWhitePlayer(gameState.getWhitePlayer());
            gameDTO.setBlackPlayer(gameState.getBlackPlayer());
            gameDTO.setOpening(gameState.getDetectedOpening());
            // Format end time as string date (yyyy-MM-dd HH:mm:ss)
            String endTime = Instant.ofEpochMilli(gameState.getLastMoveAt())
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            gameDTO.setEndTime(endTime);
            gameDTO.setTimeClass("live");
            gameDTO.setRated(true);

            // Set player ratings from their user profiles (use rapid rating for live games)
            if (whiteUser != null && whiteUser.getStats() != null) {
                gameDTO.setWhiteRating(whiteUser.getStats().getRapid());
            }
            if (blackUser != null && blackUser.getStats() != null) {
                gameDTO.setBlackRating(blackUser.getStats().getRapid());
            }

            // Set results based on game status
            String resultString;
            switch (gameState.getStatus()) {
                case LiveGameState.STATUS_WHITE_WINS:
                    gameDTO.setResultWhite("win");
                    gameDTO.setResultBlack("loss");
                    resultString = "1-0";
                    break;
                case LiveGameState.STATUS_BLACK_WINS:
                    gameDTO.setResultWhite("loss");
                    gameDTO.setResultBlack("win");
                    resultString = "0-1";
                    break;
                default:
                    gameDTO.setResultWhite("draw");
                    gameDTO.setResultBlack("draw");
                    resultString = "1/2-1/2";
                    break;
            }

            // Format moves as space-separated SAN notation with result at the end
            String movesFormatted = formatMovesForStorage(gameState, resultString);
            gameDTO.setMoves(movesFormatted);

            gameService.createGame(gameDTO);
            log.info("Saved completed game {} to MongoDB with opening: {}", 
                     gameState.getGameId(), gameState.getDetectedOpening());

            // Create game summary for embedding in user documents
            GameSummaryDTO summary = GameSummaryDTO.summarize(gameDTO);

            // Buffer game to both players (updates ELO and games array)
            if (whiteUser != null) {
                userService.bufferGame(whiteUser.getId(), summary, "rapid");
            }
            if (blackUser != null) {
                userService.bufferGame(blackUser.getId(), summary, "rapid");
            }

            // If tournament game, also buffer to tournament
            if (gameState.isTournamentGame()) {
                log.info("Buffering tournament game {} to tournament {}", gameState.getGameId(), gameState.getTournamentId());
                String result = tournamentService.bufferTournamentGame(
                        gameState.getTournamentId(),
                        gameDTO,
                        whiteUser != null ? whiteUser.getId() : null,
                        blackUser != null ? blackUser.getId() : null
                );
                log.info("Tournament buffering result for game {}: {}", gameState.getGameId(), result);
            } else {
                log.info("Game {} is not a tournament game (tournamentId: {})", gameState.getGameId(), gameState.getTournamentId());
            }

        } catch (Exception e) {
            log.error("Failed to save game {} to MongoDB: {}", gameState.getGameId(), e.getMessage(), e);
        }
    }

    private String formatMovesForStorage(LiveGameState gameState, String result) {
        if (gameState.getMoveHistory() == null || gameState.getMoveHistory().isEmpty()) {
            return result;
        }
        
        try {
            // Create a MoveList starting from the initial position
            MoveList moveList = new MoveList();
            
            // Add all UCI moves to the MoveList
            for (String uciMove : gameState.getMoveHistory()) {
                moveList.add(new Move(uciMove, Side.WHITE)); // Side doesn't matter for UCI parsing
            }
            
            // Convert to SAN notation (space-separated, no move numbers)
            String sanMoves = moveList.toSan();
            return sanMoves + " " + result;
        } catch (Exception e) {
            log.warn("Failed to convert moves to SAN for game {}, using raw moves: {}", 
                     gameState.getGameId(), e.getMessage());
            return String.join(" ", gameState.getMoveHistory()) + " " + result;
        }
    }

    private LiveGameState getGameState(String gameId) throws BusinessException {
        try {
            String json = redisTemplate.opsForValue().get(GAME_STATE_PREFIX + gameId);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, LiveGameState.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Error reading game state");
        }
    }

    private void saveGameState(LiveGameState gameState) throws BusinessException {
        try {
            String json = objectMapper.writeValueAsString(gameState);
            redisTemplate.opsForValue().set(
                GAME_STATE_PREFIX + gameState.getGameId(),
                json,
                gameExpirationHours,
                TimeUnit.HOURS
            );
        } catch (JsonProcessingException e) {
            throw new BusinessException("Error saving game state");
        }
    }

    private int getTournamentGameCount(String tournamentId, String username) {
        String key = TOURNAMENT_GAME_COUNT_PREFIX + tournamentId + ":player:" + username + ":games";
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    private void incrementTournamentGameCount(String tournamentId, String username) {
        String key = TOURNAMENT_GAME_COUNT_PREFIX + tournamentId + ":player:" + username + ":games";
        Long newValue = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        log.info("Incremented tournament game count for {} - key: {}, new value: {}", username, key, newValue);
    }
}
