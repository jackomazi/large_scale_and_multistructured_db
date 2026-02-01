package it.unipi.chessApp.service;

import it.unipi.chessApp.dto.*;
import it.unipi.chessApp.service.exception.BusinessException;

import java.util.List;

public interface UserService {
  UserDTO createUser(UserDTO userDTO) throws BusinessException;
  UserDTO registerUser(UserRegistrationDTO registrationDTO) throws BusinessException;
  UserDTO getUserById(String id) throws BusinessException;
  PageDTO<UserDTO> getAllUsers(int page) throws BusinessException;
  UserDTO updateUser(String id, UserUpdateDTO userUpdateDTO, boolean isAdmin) throws BusinessException;
  void deleteUser(String id) throws BusinessException;
  void bufferGame(String userId, GameSummaryDTO summary, String timeClass);
  void unbufferGame(String userId, String gameId, String timeClass);
  void promoteToAdmin(String username) throws BusinessException;
  List<TiltPlayerDTO> getTiltPlayers() throws BusinessException;
  UserWinRateDTO getUserWinRate(String userId) throws BusinessException;
  UserFavoriteOpeningDTO getUserFavOpening(String userId) throws BusinessException;
}
