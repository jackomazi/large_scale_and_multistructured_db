package it.unipi.chessApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    private String name;
    private String country;
    private boolean isStreamer;
    private boolean verified;
    private List<String> streamingPlatforms;
    private String mail;
    private String password;
}
