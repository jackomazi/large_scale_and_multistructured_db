package it.unipi.chessApp.scheduler;

import it.unipi.chessApp.model.Tournament;
import it.unipi.chessApp.model.TournamentPlayer;
import it.unipi.chessApp.model.User;
import it.unipi.chessApp.repository.TournamentRepository;
import it.unipi.chessApp.repository.UserRepository;
import it.unipi.chessApp.service.Neo4jService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TournamentScheduler {

    private final TournamentRepository tournamentRepository;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final Neo4jService neo4jService;

    private static final String TOURNAMENT_SUBSCRIBERS_PREFIX = "chess:tournament:";
    private static final String TOURNAMENT_SUBSCRIBERS_SUFFIX = ":subscribers";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Scheduled task that runs once a day to check for tournaments that have finished.
     * When a tournament finishes:
     * 1. Updates tournament status to "finished" in MongoDB
     * 2. Deletes the Redis subscribers set
     * 3. Copy participant data to Neo4j
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

            // Delete Redis subscribers set
            String subscribersKey = TOURNAMENT_SUBSCRIBERS_PREFIX + tournament.getId() + TOURNAMENT_SUBSCRIBERS_SUFFIX;
            redisTemplate.delete(subscribersKey);

            log.info("Tournament {} ({}) has been finished", tournament.getId(), tournament.getName());

            // Create PARTICIPATED relationships in Neo4j for each player
            if (tournament.getPlayers() != null) {
                for (TournamentPlayer player : tournament.getPlayers()) {
                    try {
                        // Look up user's mongo ID from username
                        User user = userRepository.findByUsername(player.getUsername()).orElse(null);
                        if (user != null) {
                            neo4jService.participateTournament(user.getId(), tournament.getId());
                            log.debug("Created PARTICIPATED relationship for user {} in tournament {}",
                                     player.getUsername(), tournament.getId());
                        } else {
                            log.warn("User not found for username: {}", player.getUsername());
                        }
                    } catch (Exception e) {
                        log.error("Error creating PARTICIPATED relationship for user {}: {}",
                                 player.getUsername(), e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error finishing tournament {}: {}", tournament.getId(), e.getMessage(), e);
        }
    }
}
