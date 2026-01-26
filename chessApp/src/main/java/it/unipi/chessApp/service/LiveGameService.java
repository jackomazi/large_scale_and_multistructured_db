package it.unipi.chessApp.service;

import it.unipi.chessApp.dto.GameStatusDTO;
import it.unipi.chessApp.dto.MatchmakingResultDTO;
import it.unipi.chessApp.dto.MoveResultDTO;
import it.unipi.chessApp.service.exception.BusinessException;

public interface LiveGameService {

    MatchmakingResultDTO joinMatchmaking(String username, String tournamentId) throws BusinessException;

    void leaveMatchmaking(String username, String tournamentId) throws BusinessException;

    MoveResultDTO makeMove(String gameId, String username, String move) throws BusinessException;

    GameStatusDTO getGameStatus(String gameId) throws BusinessException;

    void resignGame(String gameId, String username) throws BusinessException;
}
