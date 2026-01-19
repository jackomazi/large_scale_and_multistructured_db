package it.unipi.chessApp.service;

import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.dto.UserDTO;
import it.unipi.chessApp.service.exception.BusinessException;

public interface UserService {
  UserDTO createUser(UserDTO userDTO) throws BusinessException;
  UserDTO getUserById(String id) throws BusinessException;
  PageDTO<UserDTO> getAllUsers(int page) throws BusinessException;
  UserDTO updateUser(String id, UserDTO userDTO) throws BusinessException;
  void deleteUser(String id) throws BusinessException;
}
