package it.unipi.chessApp.controller;

import it.unipi.chessApp.dto.ClubDTO;
import it.unipi.chessApp.dto.ClubMemberDTO;
import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.dto.ResponseWrapper;
import it.unipi.chessApp.service.ClubService;
import it.unipi.chessApp.service.Neo4jService;
import it.unipi.chessApp.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

  private final ClubService clubService;
  private final Neo4jService neo4jService;

  // List all clubs (public)
  @GetMapping
  public ResponseEntity<ResponseWrapper<PageDTO<ClubDTO>>> getAllClubs(
    @RequestParam(defaultValue = "1") int page
  ) throws BusinessException {
    PageDTO<ClubDTO> clubs = clubService.getAllClubs(page);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Clubs retrieved successfully", clubs)
    );
  }

  // Get club by name (public)
  @GetMapping("/{name}")
  public ResponseEntity<ResponseWrapper<ClubDTO>> getClubByName(
    @PathVariable String name
  ) throws BusinessException {
    ClubDTO club = clubService.getClubByName(name);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Club retrieved successfully", club)
    );
  }

    @GetMapping("/{name}/members")
    public ResponseEntity<ResponseWrapper<List<ClubMemberDTO>>> getClubMembers(
            @PathVariable String name
    ) throws BusinessException {
        List<ClubMemberDTO> members = clubService.getClubMembers(name);
        return ResponseEntity.ok(
                new ResponseWrapper<List<ClubMemberDTO>>("Club retrieved successfully", members)
        );
    }

  // Create club (authenticated - creator becomes owner)
  @PostMapping
  public ResponseEntity<ResponseWrapper<ClubDTO>> createClub(
    @RequestBody ClubDTO clubDTO
  ) throws BusinessException {
    // Set the current user as the club admin/owner
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = authentication.getName();
    clubDTO.setAdmin(currentUsername);
    
    ClubDTO createdClub = clubService.createClub(clubDTO);
    neo4jService.createClub(createdClub.getId(), createdClub.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(
      new ResponseWrapper<>("Club created successfully", createdClub)
    );
  }

  // Edit club info (owner or admin)
  @PostMapping("/{id}/edit")
  public ResponseEntity<ResponseWrapper<ClubDTO>> updateClub(
    @PathVariable String id,
    @RequestBody ClubDTO clubDTO
  ) throws BusinessException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = authentication.getName();
    
    // Get the club to check ownership
    ClubDTO existingClub = clubService.getClubById(id);
    
    // Check if current user is the owner or an admin
    boolean isOwner = existingClub.getAdmin() != null && existingClub.getAdmin().equals(currentUsername);
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    
    if (!isOwner && !isAdmin) {
      throw new BusinessException("You can only edit clubs you own");
    }
    
    ClubDTO updatedClub = clubService.updateClub(id, clubDTO);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Club updated successfully", updatedClub)
    );
  }

  // Delete club (admin only)
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResponseWrapper<Void>> deleteClub(
    @PathVariable String id
  ) throws BusinessException {
    clubService.deleteClub(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Club deleted successfully", null)
    );
  }
}
