package it.unipi.chessApp.dto;


import it.unipi.chessApp.model.neo4j.ClubMember;

public class ClubMemberDTO {
    private String name;

    private String country;

    private int bulletRating;

    private int blitzRating;

    private int rapidRating;

    public String getCountry() {
        return country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getBulletRating() {
        return bulletRating;
    }

    public void setBulletRating(int bulletRating) {
        this.bulletRating = bulletRating;
    }

    public int getBlitzRating() {
        return blitzRating;
    }

    public void setBlitzRating(int blitzRating) {
        this.blitzRating = blitzRating;
    }

    public int getRapidRating() {
        return rapidRating;
    }

    public void setRapidRating(int rapidRating) {
        this.rapidRating = rapidRating;
    }

    public static ClubMemberDTO convertMemberToDTO(ClubMember member){
        ClubMemberDTO dto = new ClubMemberDTO();
        dto.setName(member.getName());
        dto.setCountry(member.getCountry());
        dto.setBlitzRating(member.getBlitzRating());
        dto.setBulletRating(member.getBulletRating());
        dto.setBulletRating(member.getBulletRating());
        return dto;
    }
}
