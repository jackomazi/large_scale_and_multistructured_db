package it.unipi.chessApp.service.impl;

import it.unipi.chessApp.dto.*;
import it.unipi.chessApp.model.User;
import it.unipi.chessApp.model.neo4j.*;
import it.unipi.chessApp.repository.UserRepository;
import it.unipi.chessApp.repository.neo4j.ClubNodeRepository;
import it.unipi.chessApp.repository.neo4j.TournamentNodeRepository;
import it.unipi.chessApp.repository.neo4j.UserNodeRepository;
import it.unipi.chessApp.service.Neo4jService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.unipi.chessApp.dto.FriendRecommendationDTO;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class Neo4jServiceImpl implements Neo4jService {

    private final UserNodeRepository userNodeRepository;
    private final ClubNodeRepository clubNodeRepository;
    private final TournamentNodeRepository tournamentNodeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createUser(String mongoId, String username) {
        UserNode user = new UserNode(mongoId, username);
        userNodeRepository.save(user);
    }

    @Override
    @Transactional
    public void createClub(String mongoId, String clubName) {
        ClubNode club = new ClubNode(mongoId, clubName);
        clubNodeRepository.save(club);
    }

    @Override
    @Transactional
    public void createTournament(String mongoId, String tournamentName) {
        TournamentNode tournament = new TournamentNode(mongoId, tournamentName);
        tournamentNodeRepository.save(tournament);
    }

    @Override
    @Transactional
    public void joinClub(String userName, String clubName) {
        try {
            Optional<UserNode> userOPT = userNodeRepository.findByName(userName);
            Optional<ClubNode> clubOPT = clubNodeRepository.findByName(clubName);

            if (userOPT.isPresent() && clubOPT.isPresent()) {
                //Getting user infos & stats
                Optional<User> user = userRepository.findByUsername(userOPT.get().getName());
                if (user.isPresent()) {
                    UserDTO userDTO = UserDTO.convertToDTO(user.get());
                    userNodeRepository.createJoinedRelation(userName,
                            clubName,
                            userDTO.getCountry(),
                            userDTO.getStats().getBullet(),
                            userDTO.getStats().getBlitz(),
                            userDTO.getStats().getRapid());
                }
            }
        }
        catch (Exception e){
            log.error("Error joining club: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void participateTournament(String userMongoId, String tournamentMongoId) {
        tournamentNodeRepository.createParticipatedRelation(userMongoId, tournamentMongoId);
    }

    @Override
    @Transactional
    public void followUser(String SourceId, String TargetId) {
        userNodeRepository.follow(SourceId, TargetId);
    }

    @Override
    @Transactional
    public void unfollowUser(String SourceId, String TargetId) {
        userNodeRepository.deleteFollowsRelationship(SourceId, TargetId);
    }

    @Override
    @Transactional
    public List<FriendRecommendationDTO> suggestFriends(String userID){
        List<FriendRecommendation> recomandations = userNodeRepository.suggestFriends(userID);
        List<FriendRecommendationDTO> recomandationDTOS = recomandations
                .stream()
                .map(FriendRecommendationDTO::convertToDTO)
                .toList();
        return recomandationDTOS;
    }

    @Override
    @Transactional
    public List<Neo4jEntityDTO> findUserFollows(String userID){
        List<UserNode> follows = userNodeRepository.findUserFollows(userID);
        List<Neo4jEntityDTO> followDTOS = follows
                .stream()
                .map(Neo4jEntityDTO::convertToDTO)
                .toList();
        return followDTOS;
    }

    @Override
    @Transactional
    public List<Neo4jEntityDTO>  findUserFollowers(String userID){
        List<UserNode> follows = userNodeRepository.findUserFollowers(userID);
        List<Neo4jEntityDTO> followDTOS = follows
                .stream()
                .map(Neo4jEntityDTO::convertToDTO)
                .toList();
        return followDTOS;
    }

    @Override
    @Transactional
    public List<TournamentParticipantDTO> findUserTournaments(String userId){
        List<TournamentParticipant> userTournaments = userNodeRepository.findUserTournaments(userId);
        List<TournamentParticipantDTO> tournamentsDTO = userTournaments
                .stream()
                .map(TournamentParticipantDTO::convertToDTO)
                .toList();
        return tournamentsDTO;
    }
}
