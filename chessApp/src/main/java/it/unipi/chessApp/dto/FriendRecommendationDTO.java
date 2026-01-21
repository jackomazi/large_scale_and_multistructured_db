package it.unipi.chessApp.dto;

import it.unipi.chessApp.model.neo4j.FriendRecommendation;

public class FriendRecommendationDTO {

    private String mongoID;

    private String name;

    private String connectionType;

    public String getMongoID() {
        return mongoID;
    }

    public void setMongoID(String mongoID) {
        this.mongoID = mongoID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public static FriendRecommendationDTO convertToDTO(FriendRecommendation recomandation){
        FriendRecommendationDTO recomandationDTO = new FriendRecommendationDTO();
        recomandationDTO.setMongoID(recomandation.getMongoID());
        recomandationDTO.setName(recomandation.getName());
        recomandationDTO.setConnectionType(recomandation.getConnectionType());
        return recomandationDTO;
    }
}
