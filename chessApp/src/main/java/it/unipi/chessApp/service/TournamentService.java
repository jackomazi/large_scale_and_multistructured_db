package it.unipi.chessApp.service;

import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.dto.TournamentDTO;
import it.unipi.chessApp.service.exception.BusinessException;

public interface TournamentService {
  TournamentDTO createTournament(TournamentDTO tournamentDTO)
    throws BusinessException;
  TournamentDTO getTournamentById(String id) throws BusinessException;
  PageDTO<TournamentDTO> getAllTournaments(int page) throws BusinessException;
  TournamentDTO updateTournament(String id, TournamentDTO tournamentDTO)
    throws BusinessException;
  void deleteTournament(String id) throws BusinessException;
}
