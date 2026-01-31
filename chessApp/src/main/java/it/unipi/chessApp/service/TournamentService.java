package it.unipi.chessApp.service;

import it.unipi.chessApp.dto.*;
import it.unipi.chessApp.service.exception.BusinessException;

import java.util.List;

public interface TournamentService {
  TournamentDTO createTournament(TournamentCreateDTO tournamentCreateDTO, String creatorUsername)
    throws BusinessException;
  TournamentDTO getTournamentById(String id) throws BusinessException;
  PageDTO<TournamentDTO> getAllTournaments(int page) throws BusinessException;
  PageDTO<TournamentDTO> getActiveTournaments(int page) throws BusinessException;
  TournamentDTO updateTournament(String id, TournamentDTO tournamentDTO)
    throws BusinessException;
  void deleteTournament(String id) throws BusinessException;
  List<TournamentPlayerResultDTO> getTournamentParticipants(String name) throws BusinessException;
  void subscribeTournament(String tournamentId, String username) throws BusinessException;
  void unsubscribeTournament(String tournamentId, String username) throws BusinessException;
  String bufferTournamentGame(String tournamentId, GameDTO summary, String whiteId, String blackId);
  void unbufferTournamentGame(String tournamentId, String gameId, String whiteId, String blackId, String resultWhite);
}
