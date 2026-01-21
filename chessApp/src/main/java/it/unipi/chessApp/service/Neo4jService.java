package it.unipi.chessApp.service;

import it.unipi.chessApp.dto.FriendRecommendationDTO;
import it.unipi.chessApp.dto.Neo4jEntityDTO;

import java.util.List;

public interface Neo4jService {
    void createUser(String mongoId, String username);
    void createClub(String mongoId, String clubName);
    void createTournament(String mongoId, String tournamentName);
    void joinClub(String userMongoId, String clubMongoId);
    void participateTournament(String userMongoId, String tournamentMongoId);
    void followUser(String followerMongoId, String followedMongoId);
    void unfollowUser(String followerMongoId, String followedMongoId);
    List<FriendRecommendationDTO> suggestFriends(String userID);
    List<Neo4jEntityDTO> findUserFollows(String userID);
    List<Neo4jEntityDTO>  findUserFollowers(String userID);
}
