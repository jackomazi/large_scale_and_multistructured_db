package it.unipi.chessApp.controller;

import it.unipi.chessApp.dto.ClubDTO;
import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.dto.ResponseWrapper;
import it.unipi.chessApp.service.ClubService;
import it.unipi.chessApp.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

  private final ClubService clubService;

  @PostMapping
  public ResponseEntity<ResponseWrapper<ClubDTO>> createClub(
    @RequestBody ClubDTO clubDTO
  ) throws BusinessException {
    ClubDTO createdClub = clubService.createClub(clubDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(
      new ResponseWrapper<>("Club created successfully", createdClub)
    );
  }

  @GetMapping("/{name}")
  public ResponseEntity<ResponseWrapper<ClubDTO>> getClubByName(
    @PathVariable String name
  ) throws BusinessException {
    ClubDTO club = clubService.getClubByName(name);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Club retrieved successfully", club)
    );
  }

  @GetMapping
  public ResponseEntity<ResponseWrapper<PageDTO<ClubDTO>>> getAllClubs(
    @RequestParam(defaultValue = "1") int page
  ) throws BusinessException {
    PageDTO<ClubDTO> clubs = clubService.getAllClubs(page);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Clubs retrieved successfully", clubs)
    );
  }

  @PutMapping("/{id}")
  public ResponseEntity<ResponseWrapper<ClubDTO>> updateClub(
    @PathVariable String id,
    @RequestBody ClubDTO clubDTO
  ) throws BusinessException {
    ClubDTO updatedClub = clubService.updateClub(id, clubDTO);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Club updated successfully", updatedClub)
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ResponseWrapper<Void>> deleteClub(
    @PathVariable String id
  ) throws BusinessException {
    clubService.deleteClub(id);
    return ResponseEntity.ok(
      new ResponseWrapper<>("Club deleted successfully", null)
    );
  }
}
