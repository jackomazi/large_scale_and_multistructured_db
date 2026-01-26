package it.unipi.chessApp.config;

import it.unipi.chessApp.service.OpeningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpeningDataLoader implements ApplicationRunner {

    private final OpeningService openingService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing chess opening database...");
        
        try {
            // Check if openings are already loaded (e.g., from a previous run with persistent Redis)
            if (openingService.isOpeningsLoaded()) {
                log.info("Chess openings already loaded in Redis ({} openings)", 
                         openingService.getOpeningsCount());
                return;
            }

            // Load openings from JSON files
            openingService.loadOpeningsToRedis();
            
            log.info("Chess opening database initialization complete. Total openings: {}", 
                     openingService.getOpeningsCount());
                     
        } catch (Exception e) {
            log.error("Failed to load chess openings: {}", e.getMessage(), e);
            // Don't fail startup - the application can still work without opening detection
        }
    }
}
