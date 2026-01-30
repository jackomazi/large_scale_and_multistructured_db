package it.unipi.chessApp.service.impl;

import it.unipi.chessApp.dto.*;
import it.unipi.chessApp.model.GameSummary;
import it.unipi.chessApp.model.Stats;
import it.unipi.chessApp.model.User;
import it.unipi.chessApp.repository.UserRepository;
import it.unipi.chessApp.repository.neo4j.UserNodeRepository;
import it.unipi.chessApp.service.AuthenticationService;
import it.unipi.chessApp.service.UserService;
import it.unipi.chessApp.service.exception.BusinessException;
import it.unipi.chessApp.utils.Constants;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final AuthenticationService authenticationService;

  private final MongoTemplate mongoTemplate;

  private final UserNodeRepository userNodeRepository;

  @Override
  public UserDTO createUser(UserDTO userDTO) throws BusinessException {
    try {
      //Adding placeholders to tournament document
      List<GameSummaryDTO> placeholders = new ArrayList<>();
      for(int i = 0; i < Constants.GAMES_BUFFER_NUMBER; i++)
          placeholders.add(new GameSummaryDTO());
      userDTO.setGames(placeholders);
      User user = convertToEntity(userDTO);
      user.setPassword(authenticationService.encodePassword(user.getPassword()));
      user.setAdmin(false);
      User createdUser = userRepository.save(user);
      return convertToDTO(createdUser);
    } catch (Exception e) {
      throw new BusinessException("Error creating user", e);
    }
  }

  @Override
  public UserDTO registerUser(UserRegistrationDTO registrationDTO) throws BusinessException {
    try {
      // Check if username already exists
      if (userRepository.existsByUsername(registrationDTO.getUsername())) {
        throw new BusinessException("Username already exists: " + registrationDTO.getUsername());
      }
      
      // Create a new User with only the registration fields
      User user = new User();
      user.setUsername(registrationDTO.getUsername());
      user.setName(registrationDTO.getName());
      user.setPassword(authenticationService.encodePassword(registrationDTO.getPassword()));
      user.setMail(registrationDTO.getMail());
      user.setCountry(registrationDTO.getCountry());
      user.setAdmin(false);
      user.setBufferedGames(0);
      
      // Set joined and lastOnline to current datetime
      String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      user.setJoined(now);
      user.setLastOnline(now);
      
      // Initialize stats with default ELO of 1000
      user.setStats(new Stats(1000, 1000, 1000));
      
      // Initialize empty games buffer with placeholders (using default date)
      List<GameSummary> placeholders = new ArrayList<>();
      for (int i = 0; i < Constants.GAMES_BUFFER_NUMBER; i++) {
          GameSummary placeholder = new GameSummary();
          placeholder.setDate(Constants.DEFAULT_PLACEHOLDER_DATE);
          placeholders.add(placeholder);
      }
      user.setGames(placeholders);
      
      User createdUser = userRepository.save(user);
      return convertToDTO(createdUser);
    } catch (Exception e) {
      throw new BusinessException("Error registering user", e);
    }
  }

  @Override
  public UserDTO getUserById(String id) throws BusinessException {
    try {
      User user = userRepository
        .findById(id)
        .orElseThrow(() ->
          new BusinessException("User not found with ID: " + id)
        );
      return convertToDTO(user);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error fetching user", e);
    }
  }

  @Override
  public PageDTO<UserDTO> getAllUsers(int page) throws BusinessException {
    try {
      PageRequest pageable = PageRequest.of(page - 1, Constants.PAGE_SIZE);
      Page<User> userPage = userRepository.findAll(pageable);
      List<UserDTO> userDTOs = userPage
        .getContent()
        .stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
      return new PageDTO<>((int) userPage.getTotalElements(), userDTOs);
    } catch (Exception e) {
      throw new BusinessException("Error fetching all users", e);
    }
  }

  @Override
  public UserDTO updateUser(String id, UserUpdateDTO userUpdateDTO, boolean isAdmin)
    throws BusinessException {
    try {
      User user = userRepository
        .findById(id)
        .orElseThrow(() ->
          new BusinessException("User not found with ID: " + id)
        );
      
      // Only update fields that are provided (not null)
      if (userUpdateDTO.getName() != null) {
        user.setName(userUpdateDTO.getName());
      }
      if (userUpdateDTO.getCountry() != null) {
        user.setCountry(userUpdateDTO.getCountry());
      }
      if (userUpdateDTO.getIsStreamer() != null) {
        user.setStreamer(userUpdateDTO.getIsStreamer());
      }
      if (userUpdateDTO.getStreamingPlatforms() != null) {
        user.setStreamingPlatforms(userUpdateDTO.getStreamingPlatforms());
      }
      if (userUpdateDTO.getMail() != null) {
        user.setMail(userUpdateDTO.getMail());
      }
      if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
        user.setPassword(authenticationService.encodePassword(userUpdateDTO.getPassword()));
      }
      
      // Only admins can change verified status
      if (userUpdateDTO.getVerified() != null && isAdmin) {
        user.setVerified(userUpdateDTO.getVerified());
      }
      
      User updatedUser = userRepository.save(user);
      return convertToDTO(updatedUser);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error updating user", e);
    }
  }

  @Override
  public void deleteUser(String id) throws BusinessException {
    try {
      userRepository.deleteById(id);
    } catch (Exception e) {
      throw new BusinessException("Error deleting user", e);
    }
  }

  private UserDTO convertToDTO(User user) {
    UserDTO dto = new UserDTO();
    dto.setId(user.getId());
    dto.setName(user.getName());
    dto.setUsername(user.getUsername());
    dto.setCountry(user.getCountry());
    dto.setLastOnline(user.getLastOnline());
    dto.setJoined(user.getJoined());
    dto.setStreamer(user.isStreamer());
    dto.setVerified(user.isVerified());
    dto.setStreamingPlatforms(user.getStreamingPlatforms());
    List<GameSummaryDTO> summaryDTO = user.getGames()
            .stream()
            .map(GameSummaryDTO::convertToDTO)
            .toList();
    dto.setGames(summaryDTO);
    dto.setStats(user.getStats());
    dto.setMail(user.getMail());
    dto.setPassword(user.getPassword());
    dto.setAdmin(user.isAdmin());
    return dto;
  }

  @Override
  public void promoteToAdmin(String username) throws BusinessException {
    try {
      User user = userRepository.findByUsername(username)
              .orElseThrow(() -> new BusinessException("User not found"));

      user.setAdmin(true);
      userRepository.save(user);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Error promoting user", e);
    }
  }

  private User convertToEntity(UserDTO dto) {
    User user = new User();
    user.setId(dto.getId());
    user.setName(dto.getName());
    user.setUsername(dto.getUsername());
    user.setCountry(dto.getCountry());
    user.setLastOnline(dto.getLastOnline());
    user.setJoined(dto.getJoined());
    user.setStreamer(dto.isStreamer());
    user.setVerified(dto.isVerified());
    user.setStreamingPlatforms(dto.getStreamingPlatforms());
    List<GameSummary> summaries = dto.getGames()
            .stream()
            .map(GameSummary::convertToEntity)
            .toList();
    user.setGames(summaries);
    user.setStats(dto.getStats());
    user.setMail(dto.getMail());
    user.setPassword(dto.getPassword());
    user.setAdmin(dto.isAdmin());
    return user;
  }

  public void bufferGame(String userId, GameSummaryDTO summary, String timeClass){

      //MongoDB user update
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new RuntimeException("User not found"));

      //Calculate elo gain/loss
      int eloDiff = this.calculateEloChange(summary, user.getUsername());
      int oldElo = 0;
      int eloRapid = user.getStats().getRapid();
      int eloBlitz = user.getStats().getBlitz();
      int eloBullet = user.getStats().getBullet();
      if(timeClass.equals("rapid")) {
          oldElo = user.getStats().getRapid();
          eloRapid += eloDiff;
      }
      if(timeClass.equals("blitz")) {
          oldElo = user.getStats().getBlitz();
          eloBlitz += eloDiff;
      }
      if(timeClass.equals("bullet")) {
          oldElo = user.getStats().getBullet();
          eloBullet += eloDiff;
      }

      // Find first placeholder index (where id == null)
      int placeholderIndex = -1;
      if (user.getGames() != null) {
          for (int i = 0; i < user.getGames().size(); i++) {
              if (user.getGames().get(i).getId() == null) {
                  placeholderIndex = i;
                  break;
              }
          }
      }

      int insertIndex;
      int nextIndex;
      if (placeholderIndex >= 0) {
          // Replace placeholder, don't advance buffer
          insertIndex = placeholderIndex;
          nextIndex = user.getBufferedGames();
      } else {
          // Circular buffer logic
          insertIndex = user.getBufferedGames();
          nextIndex = (insertIndex + 1) % Constants.GAMES_BUFFER_NUMBER;
      }

      Query query = new Query(Criteria.where("_id").is(userId));
      Update update = new Update()
              .set("games." + insertIndex, summary)
              .set("buffered_games", nextIndex)
              .set("stats." + timeClass, oldElo + eloDiff);

      mongoTemplate.updateFirst(query, update, User.class);

      //Neo4j redundancy update
      userNodeRepository.updateJoinedRelation(userId,eloBullet,eloBlitz,eloRapid);

  }

  private int calculateEloChange(GameSummaryDTO game, String nameUser){
      if(game.getWinner().equals(nameUser))
          return Constants.ELO_CHANGE;
      else
          return -Constants.ELO_CHANGE;
  }

  @Override
  public List<TiltPlayerDTO> getTiltPlayers() throws BusinessException {
    try {
        return userRepository.findTiltPlayers();
    } catch (Exception e) {
        throw new BusinessException("Error fetching tilt players", e);
    }
  }

  @Override
  public UserWinRateDTO getUserWinRate(String userId) throws BusinessException{
      try {
          return userRepository.calcUserWinRate(userId);
      } catch (Exception e) {
          throw new BusinessException("Error calculating user win rate", e);
      }
  }

  @Override
    public UserFavoriteOpeningDTO getUserFavOpening(String userId) throws BusinessException{
      try {
          return userRepository.calcFavoriteOpening(userId);
      } catch (Exception e) {
          throw new BusinessException("Error calculating user favorite opening", e);
      }
  }
}
