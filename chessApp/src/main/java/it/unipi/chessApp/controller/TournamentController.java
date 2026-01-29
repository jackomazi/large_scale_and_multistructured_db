package it.unipi.chessApp.controller;

import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.dto.ResponseWrapper;
import it.unipi.chessApp.dto.TournamentCreateDTO;
import it.unipi.chessApp.dto.TournamentDTO;
import it.unipi.chessApp.service.Neo4jService;
import it.unipi.chessApp.service.TournamentService;
import it.unipi.chessApp.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {

  private final TournamentService tournamentService;
  private final Neo4jService neo4jService;

  // List all tournaments (public)
  @GetMapping
  public ResponseEntity<ResponseWrapper<PageDTO<TournamentDTO>>> getAllTournaments(
    @RequestParam(defaultValue = "1") int page
  ) throws BusinessException {
    PageDTO<TournamentDTO> tournaments = tournamentService.getAllTournaments(page);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Tournaments retrieved successfully", tournaments)
    );
  }

  // List active tournaments (public)
  @GetMapping("/active")
  public ResponseEntity<ResponseWrapper<PageDTO<TournamentDTO>>> getActiveTournaments(
    @RequestParam(defaultValue = "1") int page
  ) throws BusinessException {
    PageDTO<TournamentDTO> tournaments = tournamentService.getActiveTournaments(page);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Active tournaments retrieved successfully", tournaments)
    );
  }

  // Get tournament by ID (public)
  @GetMapping("/{id}")
  public ResponseEntity<ResponseWrapper<TournamentDTO>> getTournamentById(
    @PathVariable String id
  ) throws BusinessException {
    TournamentDTO tournament = tournamentService.getTournamentById(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Tournament retrieved successfully", tournament)
    );
  }

  // Create tournament (admin only)
  @PostMapping("/create")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseWrapper<TournamentDTO>> createTournament(
    @RequestBody TournamentCreateDTO tournamentCreateDTO
  ) throws BusinessException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String creatorUsername = authentication.getName();
    TournamentDTO createdTournamentDTO = tournamentService.createTournament(tournamentCreateDTO, creatorUsername);
    neo4jService.createTournament(createdTournamentDTO.getId(), createdTournamentDTO.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(
      new ResponseWrapper<>("Tournament created successfully", createdTournamentDTO)
    );
  }

  // Edit tournament (admin only)
  @PostMapping("/{id}/edit")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseWrapper<TournamentDTO>> updateTournament(
    @PathVariable String id,
    @RequestBody TournamentDTO tournamentDTO
  ) throws BusinessException {
    TournamentDTO updatedTournament = tournamentService.updateTournament(id, tournamentDTO);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Tournament updated successfully", updatedTournament)
    );
  }

  // Delete tournament (admin only)
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseWrapper<Void>> deleteTournament(
    @PathVariable String id
  ) throws BusinessException {
    tournamentService.deleteTournament(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Tournament deleted successfully", null)
    );
  }

  // Register for tournament (authenticated)
  @PostMapping("/{id}/register")
  public ResponseEntity<ResponseWrapper<Void>> registerTournament(
    @PathVariable String id
  ) throws BusinessException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    tournamentService.subscribeTournament(id, username);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Successfully registered for tournament", null)
    );
  }

  // Unregister from tournament (authenticated)
  @PostMapping("/{id}/unregister")
  public ResponseEntity<ResponseWrapper<Void>> unregisterTournament(
    @PathVariable String id
  ) throws BusinessException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    tournamentService.unsubscribeTournament(id, username);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Successfully unregistered from tournament", null)
    );
  }
}
