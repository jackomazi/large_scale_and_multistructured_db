package it.unipi.chessApp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.chessApp.model.ChessOpening;
import it.unipi.chessApp.service.OpeningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpeningServiceImpl implements OpeningService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "chess:opening:";

    @Override
    public ChessOpening findOpeningByFen(String fen) {
        if (fen == null || fen.isEmpty()) {
            return null;
        }

        try {
            String normalizedFen = normalizeFen(fen);
            String result = redisTemplate.opsForValue().get(KEY_PREFIX + normalizedFen);
            
            if (result != null) {
                JsonNode node = objectMapper.readTree(result);
                return new ChessOpening(
                    node.path("eco").asText(),
                    node.path("name").asText(),
                    null,
                    null
                );
            }
        } catch (Exception e) {
            log.error("Error looking up opening for FEN {}: {}", fen, e.getMessage());
        }

        return null;
    }

    /**
     * Normalize FEN to only include position and side to move.
     * Full FEN: "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
     * Normalized: "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b"
     */
    private String normalizeFen(String fen) {
        String[] parts = fen.split(" ");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        }
        return fen;
    }
}
