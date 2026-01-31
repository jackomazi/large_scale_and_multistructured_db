package it.unipi.chessApp.controller;

import it.unipi.chessApp.dto.*;
import it.unipi.chessApp.service.AuthenticationService;
import it.unipi.chessApp.service.Neo4jService;
import it.unipi.chessApp.service.UserService;
import it.unipi.chessApp.service.exception.AuthenticationException;
import it.unipi.chessApp.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import it.unipi.chessApp.security.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final Neo4jService neo4jService;
  private final AuthenticationService authenticationService;
  private final JwtService jwtService;

  // List all players (public)
  @GetMapping
  public ResponseEntity<ResponseWrapper<PageDTO<UserDTO>>> getAllUsers(
    @RequestParam(defaultValue = "1") int page
  ) throws BusinessException {
    PageDTO<UserDTO> users = userService.getAllUsers(page);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Users retrieved successfully", users)
    );
  }

  // Get player by ID (public)
  @GetMapping("/{id}")
  public ResponseEntity<ResponseWrapper<UserDTO>> getUserById(
    @PathVariable String id
  ) throws BusinessException {
    UserDTO user = userService.getUserById(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User retrieved successfully", user)
    );
  }

  // Register new player (public)
  @PostMapping("/register")
  public ResponseEntity<ResponseWrapper<UserDTO>> createUser(
    @RequestBody UserRegistrationDTO registrationDTO
  ) throws BusinessException {
    UserDTO createdUser = userService.registerUser(registrationDTO);
    neo4jService.createUser(createdUser.getId(), createdUser.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(
      new ResponseWrapper<>("User created successfully", createdUser)
    );
  }

  // Login (public)
  @PostMapping("/login")
  public ResponseEntity<ResponseWrapper<String>> login(
    @RequestBody LoginRequest loginRequest
  ) throws AuthenticationException {
    try {
      UserDetails userDetails = authenticationService.loadUserByUsername(
        loginRequest.getUsername()
      );
      if (
        !authenticationService.matchesPassword(
          loginRequest.getPassword(),
          userDetails.getPassword()
        )
      ) {
        throw new AuthenticationException("Invalid credentials");
      }

      String token = jwtService.generateToken(userDetails);

      return ResponseEntity.ok(
        new ResponseWrapper<>("Login successful", token)
      );
    } catch (Exception e) {
      throw new AuthenticationException("Invalid credentials");
    }
  }

  // Edit player info (self or admin)
  @PostMapping("/{id}/edit")
  public ResponseEntity<ResponseWrapper<UserDTO>> updateUser(
    @PathVariable String id,
    @RequestBody UserUpdateDTO userUpdateDTO
  ) throws BusinessException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = authentication.getName();

    // Get the user being edited to check ownership
    UserDTO existingUser = userService.getUserById(id);

    // Check if current user is the owner or an admin
    boolean isOwner = existingUser.getUsername().equals(currentUsername);
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

    if (!isOwner && !isAdmin) {
      throw new BusinessException("You can only edit your own profile");
    }

    UserDTO updatedUser = userService.updateUser(id, userUpdateDTO, isAdmin);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User updated successfully", updatedUser)
    );
  }

  // Delete user (admin only)
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseWrapper<Void>> deleteUser(
    @PathVariable String id
  ) throws BusinessException {
    userService.deleteUser(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User deleted successfully", null)
    );
  }

  // Promote user to admin (admin only)
  @PostMapping("/promote")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseWrapper<Void>> promoteUser(
    @RequestBody PromoteUserRequest promoteUserRequest
  ) throws BusinessException {
    userService.promoteToAdmin(promoteUserRequest.getUsername());
    return ResponseEntity.ok(
      new ResponseWrapper<>("User promoted to admin successfully", null)
    );
  }

  @GetMapping("/{userId}/win_rate")
  public ResponseEntity<ResponseWrapper<UserWinRateDTO>> getUserWinRate(
          @PathVariable String userId
  ) throws BusinessException{
      UserWinRateDTO winRate = userService.getUserWinRate(userId);
      return ResponseEntity.ok(
              new ResponseWrapper<>("User win rate successfully calculated", winRate)
      );
  }

  @GetMapping("/{userId}/most_used_opening")
  public ResponseEntity<ResponseWrapper<UserFavoriteOpeningDTO>> getUserFavOpening(
          @PathVariable String userId
  ) throws BusinessException{
      UserFavoriteOpeningDTO opening = userService.getUserFavOpening(userId);
      return ResponseEntity.ok(
              new ResponseWrapper<>("User favorite opening successfully calculated", opening)
      );
  }

  // ==================== Stats Endpoints (authenticated users) ====================

  // Get players on tilt (lost last 3 games)
  @GetMapping("/stats/tilt")
  public ResponseEntity<ResponseWrapper<List<TiltPlayerDTO>>> getTiltPlayers() throws BusinessException {
    List<TiltPlayerDTO> tiltPlayers = userService.getTiltPlayers();
    return ResponseEntity.ok(
      new ResponseWrapper<>("Tilt players retrieved successfully", tiltPlayers)
    );
  }

  // ==================== Neo4j Endpoints ====================

  // Join club (Neo4j)
  @PostMapping("/{userName}/clubs/{clubName}")
  public ResponseEntity<ResponseWrapper<Void>> joinClub(
    @PathVariable String userName,
    @PathVariable String clubName
  ) throws BusinessException {
    neo4jService.joinClub(userName, clubName);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User joined club successfully", null)
    );
  }

  // Follow user (Neo4j)
  @PostMapping("/{SourceId}/follows/{TargetId}")
  public ResponseEntity<ResponseWrapper<Void>> followUser(
    @PathVariable String SourceId,
    @PathVariable String TargetId
  ) {
    neo4jService.followUser(SourceId, TargetId);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User follow relationship created successfully in Neo4j", null)
    );
  }

  // Unfollow user (Neo4j)
  @PostMapping("/{SourceId}/unfollows/{TargetId}")
  public ResponseEntity<ResponseWrapper<Void>> unfollowUser(
    @PathVariable String SourceId,
    @PathVariable String TargetId
  ) {
    neo4jService.unfollowUser(SourceId, TargetId);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User unfollow relationship removed successfully in Neo4j", null)
    );
  }

  // Get friend suggestions (Neo4j)
  @GetMapping("/{userId}/follows/suggestions")
  public ResponseEntity<ResponseWrapper<List<FriendRecommendationDTO>>> suggestFriends(
    @RequestParam String userId
  ) {
    List<FriendRecommendationDTO> suggestions = neo4jService.suggestFriends(userId);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User friends suggestions include", suggestions)
    );
  }

  // Get users followed by user (Neo4j)
  @GetMapping("/{userId}/follows")
  public ResponseEntity<ResponseWrapper<List<Neo4jEntityDTO>>> findUserFollows(
    @RequestParam String userId
  ) {
    List<Neo4jEntityDTO> follows = neo4jService.findUserFollows(userId);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User follows", follows)
    );
  }

  // Get user's followers (Neo4j)
  @GetMapping("/{userId}/followers")
  public ResponseEntity<ResponseWrapper<List<Neo4jEntityDTO>>> findUserFollowers(
    @RequestParam String userId
  ) {
    List<Neo4jEntityDTO> follows = neo4jService.findUserFollowers(userId);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User followers", follows)
    );
  }

  // Get user's tournaments (Neo4j)
  @GetMapping("/{userId}/tournaments")
  public ResponseEntity<ResponseWrapper<List<TournamentParticipantDTO>>> findUserTournaments(
    @PathVariable String userId
  ) {
    List<TournamentParticipantDTO> userTournaments = neo4jService.findUserTournaments(userId);
    return ResponseEntity.ok(
      new ResponseWrapper<>("User tournaments", userTournaments)
    );
  }

  // Get user's club (Neo4j)
  @GetMapping("/{userId}/club")
  public ResponseEntity<ResponseWrapper<Neo4jEntityDTO>> findUserClub(
    @PathVariable String userId
  ) throws BusinessException {
    Neo4jEntityDTO club = neo4jService.findUserClub(userId)
      .orElseThrow(() -> new BusinessException("User is not a member of any club"));
    return ResponseEntity.ok(
      new ResponseWrapper<>("User club", club)
    );
  }
}
