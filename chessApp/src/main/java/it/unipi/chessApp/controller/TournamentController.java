package it.unipi.chessApp.controller;

import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.dto.ResponseWrapper;
import it.unipi.chessApp.dto.TournamentDTO;
import it.unipi.chessApp.dto.TournamentParticipantDTO;
import it.unipi.chessApp.service.Neo4jService;
import it.unipi.chessApp.service.TournamentService;
import it.unipi.chessApp.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {

  private final TournamentService tournamentService;
  private final Neo4jService neo4jService;

  @PostMapping
  public ResponseEntity<ResponseWrapper<TournamentDTO>> createTournament(
    @RequestBody TournamentDTO tournamentDTO
  ) throws BusinessException {
    TournamentDTO createdTournamentDTO = tournamentService.createTournament(
      tournamentDTO
    );
    neo4jService.createTournament(createdTournamentDTO.getId(), createdTournamentDTO.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(
      new ResponseWrapper<>(
        "Tournament created successfully",
        createdTournamentDTO
      )
    );
  }

  @GetMapping("/{id}")
  public ResponseEntity<ResponseWrapper<TournamentDTO>> getTournamentById(
    @PathVariable String id
  ) throws BusinessException {
    TournamentDTO tournament = tournamentService.getTournamentById(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Tournament retrieved successfully", tournament)
    );
  }

  @GetMapping
  public ResponseEntity<
    ResponseWrapper<PageDTO<TournamentDTO>>
  > getAllTournaments(@RequestParam(defaultValue = "1") int page)
    throws BusinessException {
    PageDTO<TournamentDTO> tournaments = tournamentService.getAllTournaments(
      page
    );
    return ResponseEntity.ok(
      new ResponseWrapper<>("Tournaments retrieved successfully", tournaments)
    );
  }

  @GetMapping("/{id}/participants")
  public ResponseEntity<ResponseWrapper<List<TournamentParticipantDTO>>>
  getTournamentParticipants(@RequestParam String id) throws BusinessException{
      List<TournamentParticipantDTO> participants = tournamentService.getTournamentParticipants(id);
      return ResponseEntity.ok(
              new ResponseWrapper<>("Tournaments retrieved successfully", participants)
      );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ResponseWrapper<Void>> deleteTournament(
    @PathVariable String id
  ) throws BusinessException {
    tournamentService.deleteTournament(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Tournament deleted successfully", null)
    );
  }

  @PostMapping("/{id}/subscribe")
  public ResponseEntity<ResponseWrapper<Void>> subscribeTournament(
    @PathVariable String id
  ) throws BusinessException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    tournamentService.subscribeTournament(id, username);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Successfully subscribed to tournament", null)
    );
  }

  @PostMapping("/{id}/unsubscribe")
  public ResponseEntity<ResponseWrapper<Void>> unsubscribeTournament(
    @PathVariable String id
  ) throws BusinessException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    tournamentService.unsubscribeTournament(id, username);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Successfully unsubscribed from tournament", null)
    );
  }
}
