package it.unipi.chessApp.controller;

import it.unipi.chessApp.model.Chess;
import it.unipi.chessApp.repository.ChessRepository;
import it.unipi.chessApp.service.ChessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
//import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class ChessController {

    private final ChessRepository chessRepository;
    private final ChessService chessService;

    @GetMapping("/{id}")
    public Chess getGameById(@PathVariable String id) {
        return chessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    @GetMapping("/user/{username}")
    public List<Chess> getGamesByUsername(@PathVariable String username) {
        return chessService.getGamesByUsername(username);
    }



}
