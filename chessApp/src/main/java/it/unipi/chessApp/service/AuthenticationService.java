package it.unipi.chessApp.service;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthenticationService extends UserDetailsService {
    String encodePassword(String rawPassword);
    boolean matchesPassword(String rawPassword, String encodedPassword);
}
