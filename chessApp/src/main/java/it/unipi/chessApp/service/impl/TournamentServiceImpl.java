package it.unipi.chessApp.service.impl;

import it.unipi.chessApp.dto.*;
import it.unipi.chessApp.model.GameSummary;
import it.unipi.chessApp.model.Tournament;
import it.unipi.chessApp.model.User;
import it.unipi.chessApp.dto.GameDTO;
import it.unipi.chessApp.model.neo4j.TournamentParticipant;
import it.unipi.chessApp.repository.TournamentRepository;
import it.unipi.chessApp.repository.neo4j.TournamentNodeRepository;
import it.unipi.chessApp.repository.neo4j.UserNodeRepository;
import it.unipi.chessApp.service.TournamentService;
import it.unipi.chessApp.service.exception.BusinessException;
import it.unipi.chessApp.utils.Constants;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import it.unipi.chessApp.utils.Outcomes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

  private final TournamentRepository tournamentRepository;
  private final TournamentNodeRepository tournamentNodeRepository;
  private final MongoTemplate mongoTemplate;
  private final UserNodeRepository userNodeRepository;

  @Override
  public TournamentDTO createTournament(TournamentDTO tournamentDTO)
    throws BusinessException {
    try {
      //Adding placeholders to tournament document
      List<GameSummaryDTO> placeholders = new ArrayList<>();
      for(int i = 0; i < tournamentDTO.getMaxParticipants() * Constants.USER_GAMES_IN_TOURNAMENT; i++)
          placeholders.add(new GameSummaryDTO());
      tournamentDTO.setGames(placeholders);
      Tournament tournament = convertToEntity(tournamentDTO);
      Tournament createdTournament = tournamentRepository.save(tournament);
      return convertToDTO(createdTournament);
    } catch (Exception e) {
      throw new BusinessException("Error creating tournament", e);
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
  public List<TournamentParticipantDTO> getTournamentParticipants(String id) throws BusinessException{
      try {
          List<TournamentParticipant> participants = tournamentNodeRepository.findTournamentParticipants(id);

          return participants
                  .stream()
                  .map(this::convertoParticipantToDTO)
                  .toList();
      }
      catch (Exception e){
          System.out.println(e.getMessage());
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
      tournamentRepository.deleteById(id);
    } catch (Exception e) {
      throw new BusinessException("Error deleting tournament", e);
    }
  }

  @Override
  public String bufferTournamentGame(String tournamentId, GameDTO game, String whiteId, String blackId){

      //Summarises game
      GameSummaryDTO summary = GameSummaryDTO.summarize(game);

      Tournament tournament = tournamentRepository.findById(tournamentId)
              .orElseThrow(() -> new RuntimeException("Tournament not found"));

      int currentIndex = tournament.getBufferedGames();

      System.out.println(" " + currentIndex + " / " + tournament.getMaxParticipants()*Constants.USER_GAMES_IN_TOURNAMENT);

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

  private TournamentParticipantDTO convertoParticipantToDTO(TournamentParticipant participant){
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
