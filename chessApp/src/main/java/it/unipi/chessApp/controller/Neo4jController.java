package it.unipi.chessApp.controller;

import it.unipi.chessApp.dto.Neo4jEntityDTO;
import it.unipi.chessApp.dto.Neo4jJoinClubDTO;
import it.unipi.chessApp.dto.TournamentParticipantDTO;
import it.unipi.chessApp.dto.ResponseWrapper;
import it.unipi.chessApp.service.Neo4jService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/neo4j")
@RequiredArgsConstructor
public class Neo4jController {

    private final Neo4jService neo4jService;

    @PostMapping("/users")
    public ResponseEntity<ResponseWrapper<Void>> createUser(@RequestBody Neo4jEntityDTO dto) {
        neo4jService.createUser(dto.getMongoId(), dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseWrapper<>("Neo4j User created successfully", null));
    }

    @PostMapping("/clubs")
    public ResponseEntity<ResponseWrapper<Void>> createClub(@RequestBody Neo4jEntityDTO dto) {
        neo4jService.createClub(dto.getMongoId(), dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseWrapper<>("Neo4j Club created successfully", null));
    }

    @PostMapping("/tournaments")
    public ResponseEntity<ResponseWrapper<Void>> createTournament(@RequestBody Neo4jEntityDTO dto) {
        neo4jService.createTournament(dto.getMongoId(), dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseWrapper<>("Neo4j Tournament created successfully", null));
    }

    @PostMapping("/users/{userId}/clubs/{clubId}")
    public ResponseEntity<ResponseWrapper<Void>> joinClub(
            @PathVariable String userId,
            @PathVariable String clubId,
            @RequestBody Neo4jJoinClubDTO dto) {
        neo4jService.joinClub(userId, clubId, dto.getCountry(), dto.getBulletRating(), dto.getBlitzRating(), dto.getRapidRating());
        return ResponseEntity.ok(new ResponseWrapper<>("User joined club successfully in Neo4j", null));
    }

    @PostMapping("/users/{userId}/tournaments/{tournamentId}")
    public ResponseEntity<ResponseWrapper<Void>> participateTournament(
            @PathVariable String userId,
            @PathVariable String tournamentId,
            @RequestBody TournamentParticipantDTO dto) {
        neo4jService.participateTournament(userId, tournamentId, dto.getWins(), dto.getDraws(), dto.getLosses(), dto.getPlacement());
        return ResponseEntity.ok(new ResponseWrapper<>("User participation in tournament recorded successfully in Neo4j", null));
    }

    @PostMapping("/users/{followerId}/follows/{followedId}")
    public ResponseEntity<ResponseWrapper<Void>> followUser(
            @PathVariable String followerId,
            @PathVariable String followedId) {
        neo4jService.followUser(followerId, followedId);
        return ResponseEntity.ok(new ResponseWrapper<>("User follow relationship created successfully in Neo4j", null));
    }
}
