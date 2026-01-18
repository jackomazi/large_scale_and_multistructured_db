package it.unipi.chessApp.service.impl;

import it.unipi.chessApp.dto.GameDTO;
import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.model.Game;
import it.unipi.chessApp.repository.GameRepository;
import it.unipi.chessApp.service.GameService;
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
public class GameServiceImpl implements GameService {

  private final GameRepository gameRepository;

  @Override
  public GameDTO createGame(GameDTO gameDTO) throws BusinessException {
    try {
      Game game = convertToEntity(gameDTO);
      Game createdGame = gameRepository.save(game);
      return convertToDTO(createdGame);
    } catch (Exception e) {
      throw new BusinessException("Error creating game", e);
    }
  }

  @Override
  public GameDTO getGameById(String id) throws BusinessException {
    try {
      Game game = gameRepository
        .findById(id)
        .orElseThrow(() ->
          new BusinessException("Game not found with ID: " + id)
        );
      return convertToDTO(game);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error fetching game", e);
    }
  }

  @Override
  public List<GameDTO> getGamesByUsername(String username)
    throws BusinessException {
    try {
      List<Game> games = gameRepository.findByPlayer(username);
      return games
        .stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
    } catch (Exception e) {
      throw new BusinessException(
        "Error fetching games for user: " + username,
        e
      );
    }
  }

  @Override
  public PageDTO<GameDTO> getAllGames(int page) throws BusinessException {
    try {
      PageRequest pageable = PageRequest.of(page - 1, Constants.PAGE_SIZE);
      Page<Game> gamePage = gameRepository.findAll(pageable);
      List<GameDTO> gameDTOs = gamePage
        .getContent()
        .stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
      return new PageDTO<>((int) gamePage.getTotalElements(), gameDTOs);
    } catch (Exception e) {
      throw new BusinessException("Error fetching all games", e);
    }
  }

  @Override
  public GameDTO updateGame(String id, GameDTO gameDTO)
    throws BusinessException {
    try {
      if (!gameRepository.existsById(id)) {
        throw new BusinessException("Game not found with ID: " + id);
      }
      Game game = convertToEntity(gameDTO);
      game.setId(id);
      Game updatedGame = gameRepository.save(game);
      return convertToDTO(updatedGame);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error updating game", e);
    }
  }

  @Override
  public void deleteGame(String id) throws BusinessException {
    try {
      gameRepository.deleteById(id);
    } catch (Exception e) {
      throw new BusinessException("Error deleting game", e);
    }
  }

  private GameDTO convertToDTO(Game game) {
    GameDTO dto = new GameDTO();
    dto.setId(game.getId());
    dto.setWhitePlayer(game.getWhitePlayer());
    dto.setBlackPlayer(game.getBlackPlayer());
    dto.setWhiteRating(game.getWhiteRating());
    dto.setBlackRating(game.getBlackRating());
    dto.setResultWhite(game.getResultWhite());
    dto.setResultBlack(game.getResultBlack());
    dto.setOpening(game.getOpening());
    dto.setMoves(game.getMoves());
    dto.setTimeClass(game.getTimeClass());
    dto.setEndTime(game.getEndTime());
    return dto;
  }

  private Game convertToEntity(GameDTO dto) {
    Game game = new Game();
    game.setId(dto.getId());
    game.setWhitePlayer(dto.getWhitePlayer());
    game.setBlackPlayer(dto.getBlackPlayer());
    game.setWhiteRating(dto.getWhiteRating());
    game.setBlackRating(dto.getBlackRating());
    game.setResultWhite(dto.getResultWhite());
    game.setResultBlack(dto.getResultBlack());
    game.setOpening(dto.getOpening());
    game.setMoves(dto.getMoves());
    game.setTimeClass(dto.getTimeClass());
    game.setEndTime(dto.getEndTime());
    return game;
  }
}
