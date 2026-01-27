package it.unipi.chessApp.service.impl;

import it.unipi.chessApp.dto.*;
import it.unipi.chessApp.model.GameSummary;
import it.unipi.chessApp.model.Tournament;
import it.unipi.chessApp.model.TournamentPlayer;
import it.unipi.chessApp.model.neo4j.TournamentParticipant;
import it.unipi.chessApp.repository.TournamentRepository;
import it.unipi.chessApp.repository.neo4j.TournamentNodeRepository;
import it.unipi.chessApp.repository.neo4j.UserNodeRepository;
import it.unipi.chessApp.service.TournamentService;
import it.unipi.chessApp.service.exception.BusinessException;
import it.unipi.chessApp.utils.Constants;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
  private final StringRedisTemplate redisTemplate;

  private static final String TOURNAMENT_SUBSCRIBERS_PREFIX = "chess:tournament:";
  private static final String TOURNAMENT_SUBSCRIBERS_SUFFIX = ":subscribers";
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final MongoTemplate mongoTemplate;
  private final UserNodeRepository userNodeRepository;

  @Override
  public TournamentDTO createTournament(TournamentDTO tournamentDTO)
    throws BusinessException {
    try {
      // Validate finish time is at least 1 week from now
      validateFinishTime(tournamentDTO.getFinishTime());

      //Adding placeholders to tournament document
      List<GameSummaryDTO> placeholders = new ArrayList<>();
      for(int i = 0; i < tournamentDTO.getMaxParticipants() * Constants.USER_GAMES_IN_TOURNAMENT; i++)
          placeholders.add(new GameSummaryDTO());
      tournamentDTO.setGames(placeholders);
      Tournament tournament = convertToEntity(tournamentDTO);
      // Force status to "active" and initialize participants
      tournament.setStatus("active");
      tournament.setParticipants(0);
      tournament.setPlayers(new ArrayList<>());

      Tournament createdTournament = tournamentRepository.save(tournament);

      // Create Redis Set for subscribers
      String subscribersKey = getSubscribersKey(createdTournament.getId());
      // Initialize empty set by adding and removing a placeholder (Redis doesn't create empty sets)
      // The set will be populated when users subscribe
      log.info("Created tournament {} with Redis key {}", createdTournament.getId(), subscribersKey);

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
    return TOURNAMENT_SUBSCRIBERS_PREFIX + tournamentId + TOURNAMENT_SUBSCRIBERS_SUFFIX;
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
  public List<TournamentParticipantDTO> getTournamentParticipants(String id) throws BusinessException{
      try {
          List<TournamentParticipant> participants = tournamentNodeRepository.findTournamentParticipants(id);

          return participants
                  .stream()
                  .map(this::convertParticipantToDTO)
                  .toList();
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
      // Also delete Redis key when tournament is deleted
      String subscribersKey = getSubscribersKey(id);
      redisTemplate.delete(subscribersKey);
      tournamentRepository.deleteById(id);
    } catch (Exception e) {
      throw new BusinessException("Error deleting tournament", e);
    }
  }

  @Override
  public void subscribeTournament(String tournamentId, String username) throws BusinessException {
    try {
      Tournament tournament = tournamentRepository
          .findById(tournamentId)
          .orElseThrow(() -> new BusinessException("Tournament not found with ID: " + tournamentId));

      // Check tournament is active
      if (!"active".equals(tournament.getStatus())) {
        throw new BusinessException("Tournament is not active");
      }

      // Check subscription window (1. day window, starting 1 week before finish time)
      validateSubscriptionWindow(tournament.getFinishTime());

      String subscribersKey = getSubscribersKey(tournamentId);

      // Check if already subscribed
      Boolean isAlreadySubscribed = redisTemplate.opsForSet().isMember(subscribersKey, username);
      if (Boolean.TRUE.equals(isAlreadySubscribed)) {
        throw new BusinessException("You are already subscribed to this tournament");
      }

      // Check max participants
      Long currentCount = redisTemplate.opsForSet().size(subscribersKey);
      if (currentCount != null && currentCount >= tournament.getMaxParticipants()) {
        throw new BusinessException("Tournament has reached maximum participants");
      }

      // Add to Redis Set
      redisTemplate.opsForSet().add(subscribersKey, username);

      // Update MongoDB - add to players list and increment participants
      TournamentPlayer player = new TournamentPlayer();
      player.setUsername(username);
      player.setStatus("subscribed");
      if (tournament.getPlayers() == null) {
        tournament.setPlayers(new ArrayList<>());
      }
      tournament.getPlayers().add(player);
      tournament.setParticipants(tournament.getParticipants() + 1);
      tournamentRepository.save(tournament);

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
      Tournament tournament = tournamentRepository
          .findById(tournamentId)
          .orElseThrow(() -> new BusinessException("Tournament not found with ID: " + tournamentId));

      // Check tournament is active
      if (!"active".equals(tournament.getStatus())) {
        throw new BusinessException("Tournament is not active");
      }

      // Check subscription window is still open
      validateSubscriptionWindow(tournament.getFinishTime());

      String subscribersKey = getSubscribersKey(tournamentId);

      // Check if subscribed
      Boolean isSubscribed = redisTemplate.opsForSet().isMember(subscribersKey, username);
      if (!Boolean.TRUE.equals(isSubscribed)) {
        throw new BusinessException("You are not subscribed to this tournament");
      }

      // Remove from Redis Set
      redisTemplate.opsForSet().remove(subscribersKey, username);

      // Update MongoDB - remove from players list and decrement participants
      if (tournament.getPlayers() != null) {
        tournament.getPlayers().removeIf(p -> username.equals(p.getUsername()));
      }
      tournament.setParticipants(Math.max(0, tournament.getParticipants() - 1));
      tournamentRepository.save(tournament);

      log.info("User {} unsubscribed from tournament {}", username, tournamentId);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error unsubscribing from tournament", e);
    }
  }

  private void validateSubscriptionWindow(String finishTime) throws BusinessException {
    LocalDateTime finish = LocalDateTime.parse(finishTime, DATE_TIME_FORMATTER);
    // Subscription window: from (finishTime - 1 week) to (finishTime - 6 days)
    // This gives a 1 day window starting 1 week before the tournament ends
    LocalDateTime subscriptionStart = finish.minusWeeks(1);
    LocalDateTime subscriptionEnd = subscriptionStart.plusDays(1);
    LocalDateTime now = LocalDateTime.now();

    if (now.isBefore(subscriptionStart)) {
      throw new BusinessException("Subscription window has not started yet");
    }
    if (now.isAfter(subscriptionEnd)) {
      throw new BusinessException("Subscription window has closed");
    }
  }

  public String bufferTournamentGame(String tournamentId, GameDTO game, String whiteId, String blackId){

      //Summarises game
      GameSummaryDTO summary = GameSummaryDTO.summarize(game);

      Tournament tournament = tournamentRepository.findById(tournamentId)
              .orElseThrow(() -> new RuntimeException("Tournament not found"));

      int currentIndex = tournament.getBufferedGames();

      log.debug("Buffering game {} / {}", currentIndex, tournament.getMaxParticipants() * Constants.USER_GAMES_IN_TOURNAMENT);

      //Check if the limit is reached
      if(currentIndex == tournament.getMaxParticipants()*Constants.USER_GAMES_IN_TOURNAMENT)
          return Outcomes.TOURNAMENT_BUFFERING_ENDED;

      int nextIndex = currentIndex + 1;

      Query query = new Query(Criteria.where("_id").is(tournamentId));
      Update update = new Update()
              .set("games." + currentIndex, summary)
              .set("buffered_games", nextIndex);

      //Neo4j user tournament stats update
      TournamentParticipant whiteJoinedRelationM = userNodeRepository.findUserTournamentStats(tournamentId, whiteId);
      if(whiteJoinedRelationM == null)
          return Outcomes.TOURNAMENT_BUFFERING_FAILURE;
      TournamentParticipantDTO whiteJoinedRelation = TournamentParticipantDTO.convertToDTO(whiteJoinedRelationM);

      TournamentParticipant blackJoinedRelationM = userNodeRepository.findUserTournamentStats(tournamentId, blackId);
      if(blackJoinedRelationM == null)
          return Outcomes.TOURNAMENT_BUFFERING_FAILURE;
      TournamentParticipantDTO blackJoinedRelation = TournamentParticipantDTO.convertToDTO(blackJoinedRelationM);

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
              whiteJoinedRelation.getDraws(),
              whiteJoinedRelation.getLosses(),
              whiteJoinedRelation.getPlacement());

      userNodeRepository.updateUserTournamentStats(blackId,tournamentId,
              blackJoinedRelation.getWins(),
              blackJoinedRelation.getDraws(),
              blackJoinedRelation.getLosses(),
              blackJoinedRelation.getPlacement());

      //After we see if the users are actually participants
      mongoTemplate.updateFirst(query, update, Tournament.class);

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
    dto.setParticipants(tournament.getParticipants());
    dto.setMaxParticipants(tournament.getMaxParticipants());
    dto.setTimeControl(tournament.getTimeControl());
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
    tournament.setParticipants(dto.getParticipants());
    tournament.setMaxParticipants(dto.getMaxParticipants());
    tournament.setTimeControl(dto.getTimeControl());
    List<GameSummary> summary = dto.getGames()
            .stream()
            .map(GameSummary::convertToEntity)
            .toList();
    tournament.setGames(summary);
    return tournament;
  }
}
