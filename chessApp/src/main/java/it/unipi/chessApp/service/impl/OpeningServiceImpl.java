package it.unipi.chessApp.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.chessApp.model.ChessOpening;
import it.unipi.chessApp.service.OpeningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpeningServiceImpl implements OpeningService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chess.openings.data-path:data/api/opening_json_data}")
    private String openingsDataPath;

    @Value("${chess.openings.redis-key:chess:openings}")
    private String redisKey;

    private static final String[] ECO_FILES = {"ecoA.json", "ecoB.json", "ecoC.json", "ecoD.json", "ecoE.json"};

    @Override
    public void loadOpeningsToRedis() {
        log.info("Starting to load chess openings into Redis...");
        
        int totalLoaded = 0;
        Map<String, String> openingsToLoad = new HashMap<>();

        for (String fileName : ECO_FILES) {
            try {
                File file = new File(openingsDataPath, fileName);
                if (!file.exists()) {
                    log.warn("Opening file not found: {}", file.getAbsolutePath());
                    continue;
                }

                Map<String, Map<String, Object>> fileOpenings = objectMapper.readValue(
                    file, 
                    new TypeReference<Map<String, Map<String, Object>>>() {}
                );

                for (Map.Entry<String, Map<String, Object>> entry : fileOpenings.entrySet()) {
                    String fen = entry.getKey();
                    Map<String, Object> openingData = entry.getValue();

                    ChessOpening opening = new ChessOpening();
                    opening.setFen(fen);
                    opening.setEco((String) openingData.get("eco"));
                    opening.setName((String) openingData.get("name"));
                    opening.setMoves((String) openingData.get("moves"));

                    // Normalize FEN for lookup (position + side to move only)
                    String normalizedFen = normalizeFen(fen);
                    
                    String openingJson = objectMapper.writeValueAsString(opening);
                    openingsToLoad.put(normalizedFen, openingJson);
                    totalLoaded++;
                }

                log.info("Loaded {} openings from {}", fileOpenings.size(), fileName);

            } catch (IOException e) {
                log.error("Error loading opening file {}: {}", fileName, e.getMessage());
            }
        }

        // Bulk load all openings into Redis Hash
        if (!openingsToLoad.isEmpty()) {
            redisTemplate.opsForHash().putAll(redisKey, openingsToLoad);
            log.info("Successfully loaded {} chess openings into Redis", totalLoaded);
        } else {
            log.warn("No openings were loaded into Redis");
        }
    }

    @Override
    public ChessOpening findOpeningByFen(String fen) {
        if (fen == null || fen.isEmpty()) {
            return null;
        }

        try {
            String normalizedFen = normalizeFen(fen);
            Object result = redisTemplate.opsForHash().get(redisKey, normalizedFen);
            
            if (result != null) {
                return objectMapper.readValue(result.toString(), ChessOpening.class);
            }
        } catch (Exception e) {
            log.error("Error looking up opening for FEN {}: {}", fen, e.getMessage());
        }

        return null;
    }

    @Override
    public boolean isOpeningsLoaded() {
        return redisTemplate.hasKey(redisKey) && getOpeningsCount() > 0;
    }

    @Override
    public long getOpeningsCount() {
        Long size = redisTemplate.opsForHash().size(redisKey);
        return size != null ? size : 0;
    }

    /**
     * Normalize FEN to only include position and side to move.
     * This handles transpositions (same position reached via different move orders).
     * 
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
