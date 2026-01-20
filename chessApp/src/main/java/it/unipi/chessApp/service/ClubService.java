package it.unipi.chessApp.service;

import it.unipi.chessApp.dto.ClubDTO;
import it.unipi.chessApp.dto.ClubMemberDTO;
import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.service.exception.BusinessException;

import java.util.List;

public interface ClubService {
  ClubDTO createClub(ClubDTO clubDTO) throws BusinessException;
  ClubDTO getClubByName(String name) throws BusinessException;
  PageDTO<ClubDTO> getAllClubs(int page) throws BusinessException;
  ClubDTO updateClub(String id, ClubDTO clubDTO) throws BusinessException;
  void deleteClub(String id) throws BusinessException;
  List<ClubMemberDTO> getClubMembers(String name) throws BusinessException;
}
