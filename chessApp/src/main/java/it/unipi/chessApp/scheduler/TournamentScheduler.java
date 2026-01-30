package it.unipi.chessApp.scheduler;

import it.unipi.chessApp.model.Tournament;
import it.unipi.chessApp.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TournamentScheduler {

    private final TournamentRepository tournamentRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String TOURNAMENT_PREFIX = "chess:tournament:";
    private static final String TOURNAMENT_SUBSCRIBERS_SUFFIX = ":subscribers";
    private static final String TOURNAMENT_DATA_SUFFIX = ":data";
    private static final String TOURNAMENT_GAME_COUNT_PATTERN = "chess:tournament:%s:player:*:games";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Scheduled task that runs once a day to check for tournaments that have finished.
     * When a tournament finishes:
     * 1. Updates tournament status to "finished" in MongoDB
     * 2. Deletes the Redis subscribers set and game count keys
     * Note: Neo4j PARTICIPATED relationships are created when games are played (in TournamentServiceImpl.bufferTournamentGame)
     */
    @Scheduled(fixedRate = 86400000) // Run once a day
    public void finishExpiredTournaments() {
        try {
            String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            List<Tournament> expiredTournaments = tournamentRepository.findActiveTournamentsToFinish(now);

            for (Tournament tournament : expiredTournaments) {
                finishTournament(tournament);
            }

            if (!expiredTournaments.isEmpty()) {
                log.info("Finished {} tournaments", expiredTournaments.size());
            }
        } catch (Exception e) {
            log.error("Error in tournament scheduler: {}", e.getMessage(), e);
        }
    }

    private void finishTournament(Tournament tournament) {
        try {
            // Update status to "finished" in MongoDB
            tournament.setStatus("finished");
            tournamentRepository.save(tournament);

            log.info("Tournament {} ({}) has been finished", tournament.getId(), tournament.getName());

            // Delete Redis subscribers set
            String subscribersKey = TOURNAMENT_PREFIX + tournament.getId() + TOURNAMENT_SUBSCRIBERS_SUFFIX;
            redisTemplate.delete(subscribersKey);

            // Delete Redis tournament data key
            String dataKey = TOURNAMENT_PREFIX + tournament.getId() + TOURNAMENT_DATA_SUFFIX;
            redisTemplate.delete(dataKey);

            // Delete all game count keys for this tournament
            String gameCountPattern = String.format(TOURNAMENT_GAME_COUNT_PATTERN, tournament.getId());
            Set<String> gameCountKeys = redisTemplate.keys(gameCountPattern);
            if (gameCountKeys != null && !gameCountKeys.isEmpty()) {
                redisTemplate.delete(gameCountKeys);
                log.info("Deleted {} game count keys for tournament {}", gameCountKeys.size(), tournament.getId());
            }

        } catch (Exception e) {
            log.error("Error finishing tournament {}: {}", tournament.getId(), e.getMessage(), e);
        }
    }
}
