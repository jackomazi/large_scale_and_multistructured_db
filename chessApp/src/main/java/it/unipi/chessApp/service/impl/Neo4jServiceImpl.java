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
            System.out.println("I'm at least heree");
            Optional<UserNode> userOPT = userNodeRepository.findByName(userName);
            System.out.println("I'm at least heree");
            Optional<ClubNode> clubOPT = clubNodeRepository.findByName(clubName);
            System.out.println("I'm at least heree");

            if (userOPT.isPresent() && clubOPT.isPresent()) {
                //Getting user infos & stats
                Optional<User> user = userRepository.findByUsername(userOPT.get().getName());
                System.out.println("Ciaoo");
                if (user.isPresent()) {
                    UserDTO userDTO = UserDTO.convertToDTO(user.get());
                    System.out.println("I'm at least heree");
                    userNodeRepository.createJoinedRelation(userName,
                            clubName,
                            userDTO.getCountry(),
                            userDTO.getStats().getBullet(),
                            userDTO.getStats().getBlitz(),
                            userDTO.getStats().getRapid());
                }
            System.out.println("Ciaooooooo");
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    @Transactional
    public void participateTournament(String userMongoId, String tournamentMongoId, int wins, int draws, int losses, int placement) {
        Optional<UserNode> userOpt = userNodeRepository.findById(userMongoId);
        Optional<TournamentNode> tournamentOpt = tournamentNodeRepository.findById(tournamentMongoId);

        if (userOpt.isPresent() && tournamentOpt.isPresent()) {
            UserNode user = userOpt.get();
            TournamentNode tournament = tournamentOpt.get();

            TournamentParticipant participated = new TournamentParticipant();
            participated.setWins(wins);
            participated.setDraws(draws);
            participated.setLosses(losses);
            participated.setPlacement(placement);
            participated.setTournament(tournament);

            user.getTournaments().add(participated);
            userNodeRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void followUser(String followerMongoId, String followedMongoId) {
        Optional<UserNode> followerOpt = userNodeRepository.findById(followerMongoId);
        Optional<UserNode> followedOpt = userNodeRepository.findById(followedMongoId);

        if (followerOpt.isPresent() && followedOpt.isPresent()) {
            UserNode follower = followerOpt.get();
            UserNode followed = followedOpt.get();

            follower.getFollowing().add(followed);
            userNodeRepository.save(follower);
        }
    }
}