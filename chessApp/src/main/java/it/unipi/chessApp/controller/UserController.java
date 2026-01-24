package it.unipi.chessApp.controller;

import it.unipi.chessApp.dto.*;
import it.unipi.chessApp.service.Neo4jService;
import it.unipi.chessApp.service.UserService;
import it.unipi.chessApp.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final Neo4jService neo4jService;

  @PostMapping
  public ResponseEntity<ResponseWrapper<UserDTO>> createUser(
    @RequestBody UserDTO userDTO
  ) throws BusinessException {
    //User creation and MongoDB insertion
    UserDTO createdUser = userService.createUser(userDTO);
    //Neo4j insertion
    neo4jService.createUser(createdUser.getId(), createdUser.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(
      new ResponseWrapper<>("User created successfully", createdUser)
    );
  }

  @GetMapping("/{id}")
  public ResponseEntity<ResponseWrapper<UserDTO>> getUserById(
    @PathVariable String id
  ) throws BusinessException {
    UserDTO user = userService.getUserById(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User retrieved successfully", user)
    );
  }

  @PostMapping("/{userName}/clubs/{clubName}")
  public ResponseEntity<ResponseWrapper<Void>> joinClub(@PathVariable String userName,
         @PathVariable String clubName
         ) throws BusinessException{
        neo4jService.joinClub(userName, clubName);
        return ResponseEntity.ok(
              new ResponseWrapper<>("User retrieved successfully", null)
      );
  }

  @GetMapping
  public ResponseEntity<ResponseWrapper<PageDTO<UserDTO>>> getAllUsers(
    @RequestParam(defaultValue = "1") int page
  ) throws BusinessException {
    PageDTO<UserDTO> users = userService.getAllUsers(page);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Users retrieved successfully", users)
    );
  }

  @PutMapping("/{id}")
  public ResponseEntity<ResponseWrapper<UserDTO>> updateUser(
    @PathVariable String id,
    @RequestBody UserDTO userDTO
  ) throws BusinessException {
    UserDTO updatedUser = userService.updateUser(id, userDTO);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User updated successfully", updatedUser)
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ResponseWrapper<Void>> deleteUser(
    @PathVariable String id
  ) throws BusinessException {
    userService.deleteUser(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User deleted successfully", null)
    );
  }

    @PostMapping("/{SourceId}/follows/{TargetId}")
    public ResponseEntity<ResponseWrapper<Void>> followUser(
            @PathVariable String SourceId,
            @PathVariable String TargetId) {
        neo4jService.followUser(SourceId, TargetId);
        return ResponseEntity.ok(new ResponseWrapper<>("User follow relationship created successfully in Neo4j", null));
    }

    @PostMapping("/{SourceId}/unfollows/{TargetId}")
    public ResponseEntity<ResponseWrapper<Void>> unfollowUser(
            @PathVariable String SourceId,
            @PathVariable String TargetId) {
        neo4jService.unfollowUser(SourceId, TargetId);
        return ResponseEntity.ok(new ResponseWrapper<>("User follow relationship created successfully in Neo4j", null));
    }

    @PostMapping("/{userId}/participates/{tournamentId}")
    public ResponseEntity<ResponseWrapper<Void>> participateTournament(
            @PathVariable String userId,
            @PathVariable String tournamentId) {
        neo4jService.participateTournament(userId, tournamentId);
        return ResponseEntity.ok(new ResponseWrapper<>("User participation in tournament recorded successfully in Neo4j", null));
    }

    @GetMapping("/{userId}/follows/suggestions")
    public ResponseEntity<ResponseWrapper<List<FriendRecommendationDTO>>> suggestFriends(
            @RequestParam String userId)
    {
      List<FriendRecommendationDTO> suggestions = neo4jService.suggestFriends(userId);
      return ResponseEntity.ok(new ResponseWrapper<>("User friends suggestions include", suggestions));
    }

    @GetMapping("/{userId}/follows")
    public ResponseEntity<ResponseWrapper<List<Neo4jEntityDTO>>> findUserFollows(
            @RequestParam String userId
    ){
      List<Neo4jEntityDTO> follows = neo4jService.findUserFollows(userId);
      return ResponseEntity.ok(new ResponseWrapper<>("User follows", follows));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<ResponseWrapper<List<Neo4jEntityDTO>>> findUserFollowers(
            @RequestParam String userId
    ){
        List<Neo4jEntityDTO> follows = neo4jService.findUserFollowers(userId);
        return ResponseEntity.ok(new ResponseWrapper<>("User followers", follows));
    }

    @GetMapping("/{userId}/tournament")
    public ResponseEntity<ResponseWrapper<List<TournamentParticipantDTO>>> findUserTournaments(
            @PathVariable String userId
    ){
      List<TournamentParticipantDTO> userTournaments = neo4jService.findUserTournaments(userId);
      return ResponseEntity.ok(new ResponseWrapper<>("User tournaments",userTournaments));
    }
}
