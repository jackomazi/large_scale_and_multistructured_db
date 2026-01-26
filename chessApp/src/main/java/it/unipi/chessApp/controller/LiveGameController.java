package it.unipi.chessApp.controller;

import it.unipi.chessApp.dto.GameStatusDTO;
import it.unipi.chessApp.dto.MatchmakingRequestDTO;
import it.unipi.chessApp.dto.MatchmakingResultDTO;
import it.unipi.chessApp.dto.MoveRequestDTO;
import it.unipi.chessApp.dto.MoveResultDTO;
import it.unipi.chessApp.dto.ResponseWrapper;
import it.unipi.chessApp.service.LiveGameService;
import it.unipi.chessApp.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/live-games")
@RequiredArgsConstructor
public class LiveGameController {

    private final LiveGameService liveGameService;

    @PostMapping("/matchmaking")
    public ResponseEntity<ResponseWrapper<MatchmakingResultDTO>> joinMatchmaking(
            @RequestBody(required = false) MatchmakingRequestDTO request) throws BusinessException {
        String username = getCurrentUsername();
        String tournamentId = request != null ? request.getTournamentId() : null;

        MatchmakingResultDTO result = liveGameService.joinMatchmaking(username, tournamentId);

        String message = result.isMatched()
            ? "Match found! Game created."
            : "No opponent found yet. Still in matchmaking queue.";

        return ResponseEntity.status(HttpStatus.OK).body(
            new ResponseWrapper<>(message, result)
        );
    }

    @DeleteMapping("/matchmaking")
    public ResponseEntity<ResponseWrapper<Void>> leaveMatchmaking(
            @RequestParam(required = false) String tournamentId) throws BusinessException {
        String username = getCurrentUsername();
        liveGameService.leaveMatchmaking(username, tournamentId);

        return ResponseEntity.ok(
            new ResponseWrapper<>("Left matchmaking queue", null)
        );
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<ResponseWrapper<MoveResultDTO>> makeMove(
            @PathVariable String gameId,
            @RequestBody MoveRequestDTO moveRequest) throws BusinessException {
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

    @GetMapping("/{gameId}/status")
    public ResponseEntity<ResponseWrapper<GameStatusDTO>> getGameStatus(
            @PathVariable String gameId) throws BusinessException {
        GameStatusDTO status = liveGameService.getGameStatus(gameId);

        return ResponseEntity.ok(
            new ResponseWrapper<>("Game status retrieved successfully", status)
        );
    }

    @PostMapping("/{gameId}/resign")
    public ResponseEntity<ResponseWrapper<Void>> resignGame(
            @PathVariable String gameId) throws BusinessException {
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
