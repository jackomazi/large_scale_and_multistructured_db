package it.unipi.chessApp.controller;

import it.unipi.chessApp.dto.*;
import it.unipi.chessApp.service.GameService;
import it.unipi.chessApp.service.LiveGameService;
import it.unipi.chessApp.service.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

  private final GameService gameService;
  private final LiveGameService liveGameService;

  // List all games (public)
  @GetMapping
  public ResponseEntity<ResponseWrapper<PageDTO<GameDTO>>> getAllGames(
    @RequestParam(defaultValue = "1") int page
  ) throws BusinessException {
    PageDTO<GameDTO> games = gameService.getAllGames(page);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Games retrieved successfully", games)
    );
  }

  // Get game by ID (public)
  @GetMapping("/{id}")
  public ResponseEntity<ResponseWrapper<GameDTO>> getGameById(
    @PathVariable String id
  ) throws BusinessException {
    GameDTO game = gameService.getGameById(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Game retrieved successfully", game)
    );
  }

  // List games by username (public)
  @GetMapping("/user/{username}")
  public ResponseEntity<ResponseWrapper<List<GameDTO>>> getGamesByUsername(
    @PathVariable String username
  ) throws BusinessException {
    List<GameDTO> games = gameService.getGamesByUsername(username);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Games retrieved successfully", games)
    );
  }

  // Edit game (admin only)
  @PostMapping("/{id}/edit")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseWrapper<GameDTO>> updateGame(
    @PathVariable String id,
    @RequestBody GameDTO gameDTO
  ) throws BusinessException {
    GameDTO updatedGame = gameService.updateGame(id, gameDTO);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Game updated successfully", updatedGame)
    );
  }

  // Delete game (admin only)
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseWrapper<Void>> deleteGame(
    @PathVariable String id
  ) throws BusinessException {
    gameService.deleteGame(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Game deleted successfully", null)
    );
  }

  // ==================== Stats Endpoints (authenticated users) ====================

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

  @GetMapping("/stats/win-rate-by-opening")
  public ResponseEntity<ResponseWrapper<List<WinRateByOpeningDTO>>> getWinRateByOpening(
    @RequestParam(required = false) Integer minRating,
    @RequestParam(required = false) Integer maxRating,
    @RequestParam(required = false) String timeClass,
    @RequestParam(required = false) Integer minGames
  ) throws BusinessException {
    List<WinRateByOpeningDTO> stats = gameService.getWinRateByOpening(minRating, maxRating, timeClass, minGames);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Win rate by opening retrieved successfully", stats)
    );
  }

  // ==================== Live Game Endpoints ====================

  // Join regular matchmaking queue (authenticated)
  @PostMapping("/live/matchmaking")
  public ResponseEntity<ResponseWrapper<MatchmakingResultDTO>> joinMatchmaking(
    @RequestBody MatchmakingRequestDTO request
  ) throws BusinessException {
    String username = getCurrentUsername();
    String gameType = request != null ? request.getGameType() : null;

    MatchmakingResultDTO result = liveGameService.joinMatchmaking(username, gameType);

    String message = result.isMatched()
      ? "Match found! Game created."
      : "No opponent found yet. Still in matchmaking queue.";

    return ResponseEntity.status(HttpStatus.OK).body(
      new ResponseWrapper<>(message, result)
    );
  }

  // Join tournament matchmaking queue (authenticated)
  @PostMapping("/live/matchmaking/tournament/{tournamentId}")
  public ResponseEntity<ResponseWrapper<MatchmakingResultDTO>> joinTournamentMatchmaking(
    @PathVariable String tournamentId
  ) throws BusinessException {
    String username = getCurrentUsername();

    MatchmakingResultDTO result = liveGameService.joinTournamentMatchmaking(username, tournamentId);

    String message = result.isMatched()
      ? "Match found! Game created."
      : "No opponent found yet. Still in matchmaking queue.";

    return ResponseEntity.status(HttpStatus.OK).body(
      new ResponseWrapper<>(message, result)
    );
  }

  // Leave regular matchmaking queue (authenticated)
  @DeleteMapping("/live/matchmaking")
  public ResponseEntity<ResponseWrapper<Void>> leaveMatchmaking(
    @RequestParam String gameType
  ) throws BusinessException {
    String username = getCurrentUsername();
    liveGameService.leaveMatchmaking(username, gameType);

    return ResponseEntity.ok(
      new ResponseWrapper<>("Left matchmaking queue", null)
    );
  }

  // Leave tournament matchmaking queue (authenticated)
  @DeleteMapping("/live/matchmaking/tournament/{tournamentId}")
  public ResponseEntity<ResponseWrapper<Void>> leaveTournamentMatchmaking(
    @PathVariable String tournamentId
  ) throws BusinessException {
    String username = getCurrentUsername();
    liveGameService.leaveTournamentMatchmaking(username, tournamentId);

    return ResponseEntity.ok(
      new ResponseWrapper<>("Left tournament matchmaking queue", null)
    );
  }

  // Get live game status (public)
  @GetMapping("/live/{gameId}/status")
  public ResponseEntity<ResponseWrapper<GameStatusDTO>> getGameStatus(
    @PathVariable String gameId
  ) throws BusinessException {
    GameStatusDTO status = liveGameService.getGameStatus(gameId);

    return ResponseEntity.ok(
      new ResponseWrapper<>("Game status retrieved successfully", status)
    );
  }

  // Make a move (authenticated)
  @PostMapping("/live/{gameId}/move")
  public ResponseEntity<ResponseWrapper<MoveResultDTO>> makeMove(
    @PathVariable String gameId,
    @RequestBody MoveRequestDTO moveRequest
  ) throws BusinessException {
    String username = getCurrentUsername();
    MoveResultDTO result = liveGameService.makeMove(gameId, username, moveRequest.getMove());

    if (!result.isSuccess()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        new ResponseWrapper<>("Move failed", result)
      );
    }

    String message = switch (result.getOutcome()) {
      case MoveResultDTO.OUTCOME_CHECKMATE -> "Checkmate! Game over.";
      case MoveResultDTO.OUTCOME_STALEMATE -> "Stalemate! Game drawn.";
      case MoveResultDTO.OUTCOME_DRAW -> "Draw! Game over.";
      case MoveResultDTO.OUTCOME_CHECK -> "Check!";
      default -> "Move made successfully";
    };

    return ResponseEntity.ok(
      new ResponseWrapper<>(message, result)
    );
  }

  // Resign from game (authenticated)
  @PostMapping("/live/{gameId}/resign")
  public ResponseEntity<ResponseWrapper<Void>> resignGame(
    @PathVariable String gameId
  ) throws BusinessException {
    String username = getCurrentUsername();
    liveGameService.resignGame(gameId, username);

    return ResponseEntity.ok(
      new ResponseWrapper<>("You have resigned from the game", null)
    );
  }

  private String getCurrentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getName();
  }
}
