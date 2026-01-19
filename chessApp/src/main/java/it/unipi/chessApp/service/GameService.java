package it.unipi.chessApp.service;

import it.unipi.chessApp.dto.GameDTO;
import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.service.exception.BusinessException;
import java.util.List;

public interface GameService {
  GameDTO createGame(GameDTO gameDTO) throws BusinessException;
  GameDTO getGameById(String id) throws BusinessException;
  List<GameDTO> getGamesByUsername(String username) throws BusinessException;
  PageDTO<GameDTO> getAllGames(int page) throws BusinessException;
  GameDTO updateGame(String id, GameDTO gameDTO) throws BusinessException;
  void deleteGame(String id) throws BusinessException;
}
