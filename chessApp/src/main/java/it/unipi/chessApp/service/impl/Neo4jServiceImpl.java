package it.unipi.chessApp.service.impl;

import it.unipi.chessApp.dto.UserDTO;
import it.unipi.chessApp.model.User;
import it.unipi.chessApp.model.neo4j.ClubNode;
import it.unipi.chessApp.model.neo4j.TournamentParticipant;
import it.unipi.chessApp.model.neo4j.TournamentNode;
import it.unipi.chessApp.model.neo4j.UserNode;
import it.unipi.chessApp.repository.UserRepository;
import it.unipi.chessApp.repository.neo4j.ClubNodeRepository;
import it.unipi.chessApp.repository.neo4j.TournamentNodeRepository;
import it.unipi.chessApp.repository.neo4j.UserNodeRepository;
import it.unipi.chessApp.service.Neo4jService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
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
            System.out.println(e.getMessage());
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
}