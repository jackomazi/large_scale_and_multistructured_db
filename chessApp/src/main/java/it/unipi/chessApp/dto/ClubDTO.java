package it.unipi.chessApp.dto;

import it.unipi.chessApp.model.Member;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubDTO {
    private String id;
    private String name;
    private String description;
    private String country;
    private String creationDate;
    private String admin;
    private List<Member> members;
}
