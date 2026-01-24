package it.unipi.chessApp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import it.unipi.chessApp.dto.GameStatusDTO;
import it.unipi.chessApp.dto.MatchmakingResultDTO;
import it.unipi.chessApp.dto.MoveResultDTO;
import it.unipi.chessApp.model.LiveGameState;
import it.unipi.chessApp.service.LiveGameService;
import it.unipi.chessApp.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveGameServiceImpl implements LiveGameService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

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
                // TODO: Check if player is subscribed to the tournament
                // This should verify that the player is in Tournament.players list
                // For now, we assume the player is subscribed

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

                // Timeout expired - still waiting
                return new MatchmakingResultDTO(
                    null,
                    null,
                    null,
                    tournamentId,
                    false,
                    "No opponent found. Still in queue."
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
        
        String gameId = UUID.randomUUID().toString();
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
                redisTemplate.delete(PLAYER_GAME_PREFIX + gameState.getWhitePlayer());
                redisTemplate.delete(PLAYER_GAME_PREFIX + gameState.getBlackPlayer());
            }

            String nextTurn = board.getSideToMove() == Side.WHITE ? "WHITE" : "BLACK";

            return MoveResultDTO.success(outcome, board.getFen(), nextTurn, gameStatus);

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
                gameState.getLastMoveAt()
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
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }
}
