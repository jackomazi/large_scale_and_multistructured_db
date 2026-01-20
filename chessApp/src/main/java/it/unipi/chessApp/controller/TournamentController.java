package it.unipi.chessApp.controller;

import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.dto.ResponseWrapper;
import it.unipi.chessApp.dto.TournamentDTO;
import it.unipi.chessApp.dto.TournamentParticipantDTO;
import it.unipi.chessApp.service.TournamentService;
import it.unipi.chessApp.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {

  private final TournamentService tournamentService;

  @PostMapping
  public ResponseEntity<ResponseWrapper<TournamentDTO>> createTournament(
    @RequestBody TournamentDTO tournamentDTO
  ) throws BusinessException {
    TournamentDTO createdTournament = tournamentService.createTournament(
      tournamentDTO
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(
      new ResponseWrapper<>(
        "Tournament created successfully",
        createdTournament
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

  @GetMapping("/{name}/participants")
  public ResponseEntity<ResponseWrapper<List<TournamentParticipantDTO>>>
  getTournamentParticipants(@RequestParam String name) throws BusinessException{
      List<TournamentParticipantDTO> participants = tournamentService.getTournamentParticipants(name);
      return ResponseEntity.ok(
              new ResponseWrapper<>("Tournaments retrieved successfully", participants)
      );
  }

  @PutMapping("/{id}")
  public ResponseEntity<ResponseWrapper<TournamentDTO>> updateTournament(
    @PathVariable String id,
    @RequestBody TournamentDTO tournamentDTO
  ) throws BusinessException {
    TournamentDTO updatedTournament = tournamentService.updateTournament(
      id,
      tournamentDTO
    );
    return ResponseEntity.ok(
      new ResponseWrapper<>(
        "Tournament updated successfully",
        updatedTournament
      )
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
}
