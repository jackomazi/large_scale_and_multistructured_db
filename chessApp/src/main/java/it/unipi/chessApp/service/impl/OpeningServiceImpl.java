package it.unipi.chessApp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class OpeningServiceImpl implements OpeningService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chess.openings.data-path}")
    private String openingsDataPath;

    private static final String KEY_PREFIX = "chess:opening:";
    private static final String LOADED_FLAG_KEY = "chess:openings:loaded";
    private static final String[] ECO_FILES = {"ecoA.json", "ecoB.json", "ecoC.json", "ecoD.json", "ecoE.json"};

    @Override
    public void loadOpeningsToRedis() {
        log.info("Starting to load chess openings into Redis...");
        
        int totalLoaded = 0;

        for (String fileName : ECO_FILES) {
            try {
                File file = new File(openingsDataPath, fileName);
                if (!file.exists()) {
                    log.warn("Opening file not found: {}", file.getAbsolutePath());
                    continue;
                }

                JsonNode root = objectMapper.readTree(file);
                int fileCount = 0;
                var fields = root.fields();
                
                while (fields.hasNext()) {
                    var entry = fields.next();
                    String fen = entry.getKey();
                    JsonNode data = entry.getValue();

                    ChessOpening opening = new ChessOpening(
                        data.path("eco").asText(),
                        data.path("name").asText(),
                        data.path("moves").asText(),
                        fen
                    );

                    String normalizedFen = normalizeFen(fen);
                    String openingJson = objectMapper.writeValueAsString(opening);
                    redisTemplate.opsForValue().set(KEY_PREFIX + normalizedFen, openingJson);
                    fileCount++;
                }

                totalLoaded += fileCount;
                log.info("Loaded {} openings from {}", fileCount, fileName);

            } catch (IOException e) {
                log.error("Error loading opening file {}: {}", fileName, e.getMessage());
            }
        }

        if (totalLoaded > 0) {
            redisTemplate.opsForValue().set(LOADED_FLAG_KEY, String.valueOf(totalLoaded));
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
            String result = redisTemplate.opsForValue().get(KEY_PREFIX + normalizedFen);
            
            if (result != null) {
                return objectMapper.readValue(result, ChessOpening.class);
            }
        } catch (Exception e) {
            log.error("Error looking up opening for FEN {}: {}", fen, e.getMessage());
        }

        return null;
    }

    @Override
    public boolean isOpeningsLoaded() {
        return redisTemplate.hasKey(LOADED_FLAG_KEY);
    }

    @Override
    public long getOpeningsCount() {
        String count = redisTemplate.opsForValue().get(LOADED_FLAG_KEY);
        return count != null ? Long.parseLong(count) : 0;
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
