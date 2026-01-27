package it.unipi.chessApp.service;

import it.unipi.chessApp.dto.GameSummaryDTO;
import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.dto.TiltPlayerDTO;
import it.unipi.chessApp.dto.UserDTO;
import it.unipi.chessApp.dto.UserRegistrationDTO;
import it.unipi.chessApp.service.exception.BusinessException;

import java.util.List;

public interface UserService {
  UserDTO createUser(UserDTO userDTO) throws BusinessException;
  UserDTO registerUser(UserRegistrationDTO registrationDTO) throws BusinessException;
  UserDTO getUserById(String id) throws BusinessException;
  PageDTO<UserDTO> getAllUsers(int page) throws BusinessException;
  UserDTO updateUser(String id, UserDTO userDTO) throws BusinessException;
  void deleteUser(String id) throws BusinessException;
  void bufferGame(String userId, GameSummaryDTO summary, String timeClass);
  void promoteToAdmin(String username) throws BusinessException;
  List<TiltPlayerDTO> getTiltPlayers() throws BusinessException;
}
