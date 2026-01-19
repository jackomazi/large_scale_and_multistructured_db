package it.unipi.chessApp.service.impl;

import it.unipi.chessApp.model.neo4j.ClubNode;
import it.unipi.chessApp.model.neo4j.ClubMember;
import it.unipi.chessApp.model.neo4j.ParticipatedRelationship;
import it.unipi.chessApp.model.neo4j.TournamentNode;
import it.unipi.chessApp.model.neo4j.UserNode;
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
    public void joinClub(String userMongoId, String clubMongoId, String country, int bulletRating, int blitzRating, int rapidRating) {
        Optional<UserNode> userOpt = userNodeRepository.findById(userMongoId);
        Optional<ClubNode> clubOpt = clubNodeRepository.findById(clubMongoId);

        if (userOpt.isPresent() && clubOpt.isPresent()) {
            UserNode user = userOpt.get();
            ClubNode club = clubOpt.get();

            //

            ClubMember joined = new ClubMember();
            joined.setCountry(country);
            joined.setBulletRating(bulletRating);
            joined.setBlitzRating(blitzRating);
            joined.setRapidRating(rapidRating);

            user.getClubs().add(joined);
            userNodeRepository.save(user);
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

            ParticipatedRelationship participated = new ParticipatedRelationship();
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