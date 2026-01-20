package it.unipi.chessApp.service;

public interface Neo4jService {
    void createUser(String mongoId, String username);
    void createClub(String mongoId, String clubName);
    void createTournament(String mongoId, String tournamentName);
    void joinClub(String userMongoId, String clubMongoId);
    void participateTournament(String userMongoId, String tournamentMongoId, int wins, int draws, int losses, int placement);
    void followUser(String followerMongoId, String followedMongoId);
}
