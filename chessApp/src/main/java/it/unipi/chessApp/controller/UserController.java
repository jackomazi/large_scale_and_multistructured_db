package it.unipi.chessApp.controller;

import it.unipi.chessApp.dto.Neo4jJoinClubDTO;
import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.dto.ResponseWrapper;
import it.unipi.chessApp.dto.UserDTO;
import it.unipi.chessApp.service.Neo4jService;
import it.unipi.chessApp.service.UserService;
import it.unipi.chessApp.service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
