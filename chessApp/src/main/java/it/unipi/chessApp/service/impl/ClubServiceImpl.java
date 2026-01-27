package it.unipi.chessApp.service.impl;

import it.unipi.chessApp.dto.ClubDTO;
import it.unipi.chessApp.dto.ClubMemberDTO;
import it.unipi.chessApp.dto.PageDTO;
import it.unipi.chessApp.model.Club;
import it.unipi.chessApp.model.neo4j.ClubMember;
import it.unipi.chessApp.repository.ClubRepository;
import it.unipi.chessApp.repository.neo4j.ClubNodeRepository;
import it.unipi.chessApp.service.ClubService;
import it.unipi.chessApp.service.exception.BusinessException;
import it.unipi.chessApp.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubServiceImpl implements ClubService {

  private final ClubRepository clubRepository;
  private final ClubNodeRepository clubNodeRepository;

  @Override
  public ClubDTO createClub(ClubDTO clubDTO) throws BusinessException {
    try {
      Club club = convertToEntity(clubDTO);
      Club createdClub = clubRepository.save(club);
      return convertToDTO(createdClub);
    } catch (Exception e) {
      throw new BusinessException("Error creating club", e);
    }
  }

  @Override
  public ClubDTO getClubById(String id) throws BusinessException {
    try {
      Club club = clubRepository
        .findById(id)
        .orElseThrow(() ->
          new BusinessException("Club not found with ID: " + id)
        );
      return convertToDTO(club);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error fetching club", e);
    }
  }

  @Override
  public ClubDTO getClubByName(String name) throws BusinessException {
    try {
      Club club = clubRepository
        .findByName(name)
        .orElseThrow(() ->
          new BusinessException("Club not found with ID: " + name)
        );
      return convertToDTO(club);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error fetching club by name: {}", e.getMessage());
      throw new BusinessException("Error fetching club", e);
    }
  }

  @Override
  public List<ClubMemberDTO> getClubMembers(String name) throws BusinessException{
      try {
          List<ClubMember> members = clubNodeRepository.findClubMembers(name);
          List<ClubMemberDTO> clubMemberDTOS = new ArrayList<>();
          for(ClubMember member: members){
              clubMemberDTOS.add(ClubMemberDTO.convertMemberToDTO(member));
          }
          return clubMemberDTOS;
      }
      catch (Exception e){
          throw new BusinessException("Error fetching club", e);
      }

  }

  @Override
  public PageDTO<ClubDTO> getAllClubs(int page) throws BusinessException {
    try {
      PageRequest pageable = PageRequest.of(page - 1, Constants.PAGE_SIZE);
      Page<Club> clubPage = clubRepository.findAll(pageable);
      List<ClubDTO> clubDTOs = clubPage
        .getContent()
        .stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
      return new PageDTO<>((int) clubPage.getTotalElements(), clubDTOs);
    } catch (Exception e) {
      throw new BusinessException("Error fetching all clubs", e);
    }
  }

  @Override
  public ClubDTO updateClub(String id, ClubDTO clubDTO)
    throws BusinessException {
    try {
      if (!clubRepository.existsById(id)) {
        throw new BusinessException("Club not found with ID: " + id);
      }
      Club club = convertToEntity(clubDTO);
      club.setId(id);
      Club updatedClub = clubRepository.save(club);
      return convertToDTO(updatedClub);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error updating club", e);
    }
  }

  @Override
  public void deleteClub(String id) throws BusinessException {
    try {
      clubRepository.deleteById(id);
    } catch (Exception e) {
      throw new BusinessException("Error deleting club", e);
    }
  }

  private ClubDTO convertToDTO(Club club) {
    ClubDTO dto = new ClubDTO();
    dto.setId(club.getId());
    dto.setName(club.getName());
    dto.setDescription(club.getDescription());
    dto.setCountry(club.getCountry());
    dto.setCreationDate(club.getCreationDate());
    dto.setAdmin(club.getAdmin());
    return dto;
  }

  private Club convertToEntity(ClubDTO dto) {
    Club club = new Club();
    club.setId(dto.getId());
    club.setName(dto.getName());
    club.setDescription(dto.getDescription());
    club.setCountry(dto.getCountry());
    club.setCreationDate(dto.getCreationDate());
    club.setAdmin(dto.getAdmin());
    return club;
  }
}
