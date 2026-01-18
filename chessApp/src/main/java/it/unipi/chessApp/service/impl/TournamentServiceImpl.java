package it.unipi.chessApp.service.impl;

import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.dto.TournamentDTO;
import it.unipi.chessApp.model.Tournament;
import it.unipi.chessApp.repository.TournamentRepository;
import it.unipi.chessApp.service.TournamentService;
import it.unipi.chessApp.service.exception.BusinessException;
import it.unipi.chessApp.utils.Constants;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

  private final TournamentRepository tournamentRepository;

  @Override
  public TournamentDTO createTournament(TournamentDTO tournamentDTO)
    throws BusinessException {
    try {
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
    dto.setPlayers(tournament.getPlayers());
    dto.setGames(tournament.getGames());
    return dto;
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
    tournament.setPlayers(dto.getPlayers());
    tournament.setGames(dto.getGames());
    return tournament;
  }
}
