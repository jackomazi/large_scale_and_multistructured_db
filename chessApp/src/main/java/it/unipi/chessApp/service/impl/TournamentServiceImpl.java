package it.unipi.chessApp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.chessApp.dto.*;
import it.unipi.chessApp.model.GameSummary;
import it.unipi.chessApp.model.Tournament;
import it.unipi.chessApp.model.LiveTournamentData;
import it.unipi.chessApp.model.User;
import it.unipi.chessApp.model.neo4j.TournamentParticipant;
import it.unipi.chessApp.repository.TournamentRepository;
import it.unipi.chessApp.repository.UserRepository;
import it.unipi.chessApp.repository.neo4j.TournamentNodeRepository;
import it.unipi.chessApp.repository.neo4j.UserNodeRepository;
import it.unipi.chessApp.service.TournamentService;
import it.unipi.chessApp.service.exception.BusinessException;
import it.unipi.chessApp.utils.Constants;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import it.unipi.chessApp.utils.Outcomes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentServiceImpl implements TournamentService {

  private final TournamentRepository tournamentRepository;
  private final TournamentNodeRepository tournamentNodeRepository;
  private final UserRepository userRepository;
  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  private static final String TOURNAMENT_PREFIX = "chess:tournament:";
  private static final String TOURNAMENT_SUBSCRIBERS_SUFFIX = ":subscribers";
  private static final String TOURNAMENT_DATA_SUFFIX = ":data";
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final MongoTemplate mongoTemplate;
  private final UserNodeRepository userNodeRepository;

  @Override
  public TournamentDTO createTournament(TournamentCreateDTO tournamentCreateDTO, String creatorUsername)
    throws BusinessException {
    try {
      // Validate finish time is at least 1 week from now
      validateFinishTime(tournamentCreateDTO.getFinishTime());

      //Adding placeholders to tournament document
      List<GameSummary> placeholders = new ArrayList<>();
      for(int i = 0; i < Constants.TOURNAMENT_MAX_PARTICIPANTS * Constants.USER_GAMES_IN_TOURNAMENT; i++)
          placeholders.add(new GameSummary());

      Tournament tournament = convertCreateDTOToEntity(tournamentCreateDTO);
      tournament.setGames(placeholders);
      // Set creator from authenticated admin
      tournament.setCreator(creatorUsername);
      // Set static values
      tournament.setMaxParticipants(Constants.TOURNAMENT_MAX_PARTICIPANTS);
      tournament.setTimeControl(Constants.TOURNAMENT_TIME_CONTROL);
      // Force status to "active"
      tournament.setStatus("active");

      Tournament createdTournament = tournamentRepository.save(tournament);

      // Store tournament data in Redis as JSON string
      LiveTournamentData liveTournamentData = LiveTournamentData.fromTournament(
          createdTournament.getStatus(),
          createdTournament.getMinRating(),
          createdTournament.getMaxRating(),
          createdTournament.getMaxParticipants(),
          createdTournament.getFinishTime()
      );
      saveLiveTournamentData(createdTournament.getId(), liveTournamentData);

      // Create Redis Set for subscribers (will be populated when users subscribe)
      log.info("Created tournament {} with Redis data key {} and subscribers key {}",
          createdTournament.getId(),
          getDataKey(createdTournament.getId()),
          getSubscribersKey(createdTournament.getId()));

      return convertToDTO(createdTournament);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error creating tournament", e);
    }
  }

  private void validateFinishTime(String finishTime) throws BusinessException {
    if (finishTime == null || finishTime.isEmpty()) {
      throw new BusinessException("Finish time is required");
    }
    try {
      LocalDateTime finish = LocalDateTime.parse(finishTime, DATE_TIME_FORMATTER);
      LocalDateTime minFinishTime = LocalDateTime.now().plusWeeks(1);
      if (finish.isBefore(minFinishTime)) {
        throw new BusinessException("Tournament finish time must be at least 1 week from now");
      }
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Invalid finish time format. Use: yyyy-MM-dd HH:mm:ss");
    }
  }

  private String getSubscribersKey(String tournamentId) {
    return TOURNAMENT_PREFIX + tournamentId + TOURNAMENT_SUBSCRIBERS_SUFFIX;
  }

  private String getDataKey(String tournamentId) {
    return TOURNAMENT_PREFIX + tournamentId + TOURNAMENT_DATA_SUFFIX;
  }

  /**
   * Retrieve tournament data from Redis and deserialize from JSON.
   * @param tournamentId The tournament ID
   * @return LiveTournamentData or null if not found
   */
  private LiveTournamentData getLiveTournamentData(String tournamentId) throws BusinessException {
    try {
      String json = redisTemplate.opsForValue().get(getDataKey(tournamentId));
      if (json == null) {
        return null;
      }
      return objectMapper.readValue(json, LiveTournamentData.class);
    } catch (JsonProcessingException e) {
      throw new BusinessException("Error reading tournament data from Redis");
    }
  }

  /**
   * Save tournament data to Redis as JSON string.
   * @param tournamentId The tournament ID
   * @param tournamentData The tournament data to save
   */
  private void saveLiveTournamentData(String tournamentId, LiveTournamentData tournamentData) throws BusinessException {
    try {
      String json = objectMapper.writeValueAsString(tournamentData);
      redisTemplate.opsForValue().set(getDataKey(tournamentId), json);
    } catch (JsonProcessingException e) {
      throw new BusinessException("Error saving tournament data to Redis");
    }
  }

  @Override
  public TournamentDTO getTournamentById(String id) throws BusinessException {
    try {
      Tournament tournament = tournamentRepository
        .findById(id)
        .orElseThrow(() ->
          new BusinessException("Tournament not found with ID: " + id)
        );
      return convertToDTO(tournament);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error fetching tournament", e);
    }
  }

  @Override
  public PageDTO<TournamentDTO> getAllTournaments(int page)
    throws BusinessException {
    try {
      PageRequest pageable = PageRequest.of(page - 1, Constants.PAGE_SIZE);
      Page<Tournament> tournamentPage = tournamentRepository.findAll(pageable);
      List<TournamentDTO> tournamentDTOs = tournamentPage
        .getContent()
        .stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
      return new PageDTO<>(
        (int) tournamentPage.getTotalElements(),
        tournamentDTOs
      );
    } catch (Exception e) {
      throw new BusinessException("Error fetching all tournaments", e);
    }
  }

  @Override
  public PageDTO<TournamentDTO> getActiveTournaments(int page)
    throws BusinessException {
    try {
      PageRequest pageable = PageRequest.of(page - 1, Constants.PAGE_SIZE);
      Page<Tournament> tournamentPage = tournamentRepository.findByStatus("active", pageable);
      List<TournamentDTO> tournamentDTOs = tournamentPage
        .getContent()
        .stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
      return new PageDTO<>(
        (int) tournamentPage.getTotalElements(),
        tournamentDTOs
      );
    } catch (Exception e) {
      throw new BusinessException("Error fetching active tournaments", e);
    }
  }

  @Override
  public List<TournamentPlayerResultDTO> getTournamentParticipants(String id) throws BusinessException{
      try {
          List<TournamentPlayerResultDTO> participants = tournamentNodeRepository.findTournamentParticipants(id);

          return participants;
      }
      catch (Exception e){
          log.error("Error fetching tournament participants: {}", e.getMessage());
          throw new BusinessException("Error fetching tournament participant", e);
      }
  }

  @Override
  public TournamentDTO updateTournament(String id, TournamentDTO tournamentDTO)
    throws BusinessException {
    try {
      if (!tournamentRepository.existsById(id)) {
        throw new BusinessException("Tournament not found with ID: " + id);
      }
      Tournament tournament = convertToEntity(tournamentDTO);
      tournament.setId(id);
      Tournament updatedTournament = tournamentRepository.save(tournament);
      return convertToDTO(updatedTournament);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error updating tournament", e);
    }
  }

  @Override
  public void deleteTournament(String id) throws BusinessException {
    try {
      // Delete Redis keys when tournament is deleted
      String subscribersKey = getSubscribersKey(id);
      String dataKey = getDataKey(id);
      redisTemplate.delete(subscribersKey);
      redisTemplate.delete(dataKey);
      tournamentRepository.deleteById(id);
    } catch (Exception e) {
      throw new BusinessException("Error deleting tournament", e);
    }
  }

  @Override
  public void subscribeTournament(String tournamentId, String username) throws BusinessException {
    try {
      String subscribersKey = getSubscribersKey(tournamentId);

      // Get tournament data from Redis JSON string
      LiveTournamentData tournamentData = getLiveTournamentData(tournamentId);
      if (tournamentData == null) {
        throw new BusinessException("Tournament not found in Redis: " + tournamentId);
      }

      // Check subscription window (1 day window, starting 1 week before finish time)
      validateSubscriptionWindow(tournamentData.getFinishTime());

      // Check user's bullet elo is within tournament rating range
      int minRating = tournamentData.getMinRating();
      int maxRating = tournamentData.getMaxRating();

      User user = userRepository.findByUsername(username)
          .orElseThrow(() -> new BusinessException("User not found: " + username));
      int bulletElo = user.getStats() != null ? user.getStats().getBullet() : 0;
      if (bulletElo < minRating || bulletElo > maxRating) {
        throw new BusinessException("Your bullet elo (" + bulletElo + ") is not within the tournament rating range ("
            + minRating + " - " + maxRating + ")");
      }

      // Add first, then check - this avoids a race condition where multiple users
      // could pass the count check simultaneously and exceed maxParticipants
      int maxParticipants = tournamentData.getMaxParticipants();
      Long added = redisTemplate.opsForSet().add(subscribersKey, username);

      if (added == null || added == 0) {
        // User was already in set (SADD returns 0 if element already exists)
        throw new BusinessException("You are already subscribed to this tournament");
      }

      // Now check if we exceeded the limit
      Long currentCount = redisTemplate.opsForSet().size(subscribersKey);
      if (currentCount != null && currentCount > maxParticipants) {
        // Over limit - remove ourselves and reject
        redisTemplate.opsForSet().remove(subscribersKey, username);
        throw new BusinessException("Tournament has reached maximum participants");
      }

      log.info("User {} subscribed to tournament {}", username, tournamentId);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error subscribing to tournament", e);
    }
  }

  @Override
  public void unsubscribeTournament(String tournamentId, String username) throws BusinessException {
    try {
      String subscribersKey = getSubscribersKey(tournamentId);

      // Get tournament data from Redis JSON string
      LiveTournamentData tournamentData = getLiveTournamentData(tournamentId);
      if (tournamentData == null) {
        throw new BusinessException("Tournament not found in Redis: " + tournamentId);
      }

      // Check subscription window is still open
      validateSubscriptionWindow(tournamentData.getFinishTime());

      // Check if subscribed
      Boolean isSubscribed = redisTemplate.opsForSet().isMember(subscribersKey, username);
      if (!Boolean.TRUE.equals(isSubscribed)) {
        throw new BusinessException("You are not subscribed to this tournament");
      }

      // Check if user has already played a game in this tournament
      String gameCountKey = TOURNAMENT_PREFIX + tournamentId + ":player:" + username + ":games";
      String gameCountStr = redisTemplate.opsForValue().get(gameCountKey);
      int gameCount = gameCountStr != null ? Integer.parseInt(gameCountStr) : 0;
      if (gameCount > 0) {
        throw new BusinessException("Cannot unsubscribe: you have already played " + gameCount + " game(s) in this tournament");
      }

      // Remove from Redis Set
      redisTemplate.opsForSet().remove(subscribersKey, username);

      log.info("User {} unsubscribed from tournament {}", username, tournamentId);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error unsubscribing from tournament", e);
    }
  }

  private void validateSubscriptionWindow(String finishTime) throws BusinessException {
    LocalDateTime finish = LocalDateTime.parse(finishTime, DATE_TIME_FORMATTER);
    // Subscription window: from tournament creation until 6 days before the end
    LocalDateTime subscriptionEnd = finish.minusDays(6);
    LocalDateTime now = LocalDateTime.now();

    if (now.isAfter(subscriptionEnd)) {
      throw new BusinessException("Subscription window has closed");
    }
  }

  public String bufferTournamentGame(String tournamentId, GameDTO game, String whiteId, String blackId){

      //Summarises game
      GameSummaryDTO summaryDTO = GameSummaryDTO.summarize(game);
      GameSummary summary = GameSummary.convertToEntity(summaryDTO);

      Tournament tournament = tournamentRepository.findById(tournamentId)
              .orElseThrow(() -> new RuntimeException("Tournament not found"));

      int currentIndex = tournament.getBufferedGames();

      //Check if the limit is reached
      if(currentIndex == tournament.getMaxParticipants()*Constants.USER_GAMES_IN_TOURNAMENT)
          return Outcomes.TOURNAMENT_BUFFERING_ENDED;

      int nextIndex = currentIndex + 1;

      Query query = new Query(Criteria.where("_id").is(tournamentId));
      Update update = new Update()
              .set("games." + currentIndex, summary)
              .set("buffered_games", nextIndex);

      //Neo4j user tournament stats update - create relationship if it doesn't exist
      TournamentParticipant whiteJoinedRelationM = userNodeRepository.findUserTournamentStats(tournamentId, whiteId);
      if(whiteJoinedRelationM == null) {
          tournamentNodeRepository.createParticipatedRelation(whiteId, tournamentId);
          whiteJoinedRelationM = userNodeRepository.findUserTournamentStats(tournamentId, whiteId);
          if(whiteJoinedRelationM == null)
              return Outcomes.TOURNAMENT_BUFFERING_FAILURE;
      }
      TournamentParticipantDTO whiteJoinedRelation = TournamentParticipantDTO.convertToDTO(whiteJoinedRelationM);

      TournamentParticipant blackJoinedRelationM = userNodeRepository.findUserTournamentStats(tournamentId, blackId);
      if(blackJoinedRelationM == null) {
          tournamentNodeRepository.createParticipatedRelation(blackId, tournamentId);
          blackJoinedRelationM = userNodeRepository.findUserTournamentStats(tournamentId, blackId);
          if(blackJoinedRelationM == null)
              return Outcomes.TOURNAMENT_BUFFERING_FAILURE;
      }
      TournamentParticipantDTO blackJoinedRelation = TournamentParticipantDTO.convertToDTO(blackJoinedRelationM);

      // Update tournament stats (wins/draws/losses)
      if(game.getResultWhite().equals("stalemate")){
          whiteJoinedRelation.setDraws(whiteJoinedRelation.getDraws() + 1);
          blackJoinedRelation.setDraws(blackJoinedRelation.getDraws() + 1);
      }
      else if(game.getResultWhite().equals("win")){
          whiteJoinedRelation.setWins(whiteJoinedRelation.getWins() + 1);
          blackJoinedRelation.setLosses(blackJoinedRelation.getLosses() + 1);
      }
      else {
          blackJoinedRelation.setWins(blackJoinedRelation.getWins() + 1);
          whiteJoinedRelation.setLosses(whiteJoinedRelation.getLosses() + 1);
      }

      userNodeRepository.updateUserTournamentStats(whiteId,tournamentId,
              whiteJoinedRelation.getWins(),
              whiteJoinedRelation.getLosses(),
              whiteJoinedRelation.getDraws(),
              whiteJoinedRelation.getPlacement());

      userNodeRepository.updateUserTournamentStats(blackId,tournamentId,
              blackJoinedRelation.getWins(),
              blackJoinedRelation.getLosses(),
              blackJoinedRelation.getDraws(),
              blackJoinedRelation.getPlacement());

      //After we see if the users are actually participants
      mongoTemplate.updateFirst(query, update, Tournament.class);

      // Note: bullet ELO update is handled by bufferGame in LiveGameServiceImpl

      return Outcomes.TOURNAMENT_BUFFERING_SUCCESS;
  }

  private TournamentDTO convertToDTO(Tournament tournament) {
    TournamentDTO dto = new TournamentDTO();
    dto.setId(tournament.getId());
    dto.setName(tournament.getName());
    dto.setDescription(tournament.getDescription());
    dto.setCreator(tournament.getCreator());
    dto.setStatus(tournament.getStatus());
    dto.setFinishTime(tournament.getFinishTime());
    dto.setMinRating(tournament.getMinRating());
    dto.setMaxRating(tournament.getMaxRating());
    dto.setMaxParticipants(tournament.getMaxParticipants());
    dto.setTimeControl(tournament.getTimeControl());
    dto.setBufferedGames(tournament.getBufferedGames());
    List<GameSummaryDTO> summaryDTOS = tournament.getGames()
            .stream()
            .map(GameSummaryDTO::convertToDTO)
            .toList();
    dto.setGames(summaryDTOS);
    return dto;
  }

  private TournamentParticipantDTO convertParticipantToDTO(TournamentParticipant participant){
      TournamentParticipantDTO participantDTO = new TournamentParticipantDTO();
      participantDTO.setName(participant.getName());
      participantDTO.setWins(participant.getWins());
      participantDTO.setDraws(participant.getDraws());
      participantDTO.setLosses(participant.getLosses());
      participantDTO.setPlacement(participant.getPlacement());
      return participantDTO;
  }

  private Tournament convertToEntity(TournamentDTO dto) {
    Tournament tournament = new Tournament();
    tournament.setId(dto.getId());
    tournament.setName(dto.getName());
    tournament.setDescription(dto.getDescription());
    tournament.setCreator(dto.getCreator());
    tournament.setStatus(dto.getStatus());
    tournament.setFinishTime(dto.getFinishTime());
    tournament.setMinRating(dto.getMinRating());
    tournament.setMaxRating(dto.getMaxRating());
    tournament.setMaxParticipants(dto.getMaxParticipants());
    tournament.setTimeControl(dto.getTimeControl());
    List<GameSummary> summary = dto.getGames()
            .stream()
            .map(GameSummary::convertToEntity)
            .toList();
    tournament.setGames(summary);
    return tournament;
  }

  private Tournament convertCreateDTOToEntity(TournamentCreateDTO dto) {
    Tournament tournament = new Tournament();
    tournament.setName(dto.getName());
    tournament.setDescription(dto.getDescription());
    tournament.setFinishTime(dto.getFinishTime());
    tournament.setMinRating(dto.getMinRating());
    tournament.setMaxRating(dto.getMaxRating());
    return tournament;
  }
}
