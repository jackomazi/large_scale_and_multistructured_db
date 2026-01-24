package it.unipi.chessApp.service;

import it.unipi.chessApp.dto.*;
import it.unipi.chessApp.service.exception.BusinessException;

import java.util.List;

public interface TournamentService {
  TournamentDTO createTournament(TournamentDTO tournamentDTO)
    throws BusinessException;
  TournamentDTO getTournamentById(String id) throws BusinessException;
  PageDTO<TournamentDTO> getAllTournaments(int page) throws BusinessException;
  TournamentDTO updateTournament(String id, TournamentDTO tournamentDTO)
    throws BusinessException;
  void deleteTournament(String id) throws BusinessException;
  List<TournamentParticipantDTO> getTournamentParticipants(String name) throws BusinessException;
  String bufferTournamentGame(String tournamentId, GameDTO summary, String whiteId, String blackId);
}
