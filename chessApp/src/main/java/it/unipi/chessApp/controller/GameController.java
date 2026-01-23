package it.unipi.chessApp.controller;

import it.unipi.chessApp.dto.GameDTO;
import it.unipi.chessApp.dto.GameSummaryDTO;
import it.unipi.chessApp.dto.MonthlyOpeningStatDTO;
import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.dto.ResponseWrapper;
import it.unipi.chessApp.model.GameSummary;
import it.unipi.chessApp.service.GameService;
import it.unipi.chessApp.service.UserService;
import it.unipi.chessApp.service.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static it.unipi.chessApp.dto.GameSummaryDTO.summarize;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

  private final GameService gameService;
  private final UserService userService;

  @PostMapping("/addGame/{whiteUserId}/{blackUserId}")
  public ResponseEntity<ResponseWrapper<GameDTO>> createGame(
    @PathVariable String whiteUserId,
    @PathVariable String blackUserId,
    @RequestBody GameDTO gameDTO
  ) throws BusinessException {
        //Insert into mongoDB collection
        GameDTO createdGame = gameService.createGame(gameDTO);
        //Digest creation
        GameSummaryDTO gameSummary = GameSummaryDTO.summarize(createdGame);
        //Insert into mongoDB white user document
        userService.bufferGame(whiteUserId,gameSummary);
        //Insert into mongoDB black user document
        userService.bufferGame(blackUserId,gameSummary);
    return ResponseEntity.status(HttpStatus.CREATED).body(
      new ResponseWrapper<>("Game created successfully", createdGame)
    );
  }

  @GetMapping("/{id}")
  public ResponseEntity<ResponseWrapper<GameDTO>> getGameById(
    @PathVariable String id
  ) throws BusinessException {
    GameDTO game = gameService.getGameById(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Game retrieved successfully", game)
    );
  }

  @GetMapping("/user/{username}")
  public ResponseEntity<ResponseWrapper<List<GameDTO>>> getGamesByUsername(
    @PathVariable String username
  ) throws BusinessException {
    List<GameDTO> games = gameService.getGamesByUsername(username);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Games retrieved successfully", games)
    );
  }

  @GetMapping
  public ResponseEntity<ResponseWrapper<PageDTO<GameDTO>>> getAllGames(
    @RequestParam(defaultValue = "1") int page
  ) throws BusinessException {
    PageDTO<GameDTO> games = gameService.getAllGames(page);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Games retrieved successfully", games)
    );
  }

  @PutMapping("/{id}")
  public ResponseEntity<ResponseWrapper<GameDTO>> updateGame(
    @PathVariable String id,
    @RequestBody GameDTO gameDTO
  ) throws BusinessException {
    GameDTO updatedGame = gameService.updateGame(id, gameDTO);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Game updated successfully", updatedGame)
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ResponseWrapper<Void>> deleteGame(
    @PathVariable String id
  ) throws BusinessException {
    gameService.deleteGame(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Game deleted successfully", null)
    );
  }

  @GetMapping("/stats/top-openings")
  public ResponseEntity<ResponseWrapper<List<MonthlyOpeningStatDTO>>> getMonthlyTopOpenings(
          @RequestParam(required = false) Integer minWhite,
          @RequestParam(required = false) Integer minBlack,
          @RequestParam(required = false) Integer year,
          @RequestParam(required = false) Integer month
  ) throws BusinessException {
    List<MonthlyOpeningStatDTO> stats = gameService.getMonthlyTopOpenings(minWhite, minBlack, year, month);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Monthly top openings retrieved successfully", stats)
    );
  }

  @GetMapping("/stats/average-elo")
  public ResponseEntity<ResponseWrapper<Double>> getAverageEloForOpening(
          @RequestParam String opening,
          @RequestParam(required = false) Integer year,
          @RequestParam(required = false) Integer month
  ) throws BusinessException {
      Double avgElo = gameService.getAverageEloForOpening(opening, year, month);
      return ResponseEntity.ok(
              new ResponseWrapper<>("Average Elo retrieved successfully", avgElo)
      );
  }
}
