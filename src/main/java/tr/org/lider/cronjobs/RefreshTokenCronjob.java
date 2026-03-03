package tr.org.lider.cronjobs;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import tr.org.lider.entities.RefreshTokenImpl;
import tr.org.lider.repositories.RefreshTokenRepository;
import tr.org.lider.services.ConfigurationService;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Component
public class RefreshTokenCronjob {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TaskScheduler taskScheduler;
    private final ConfigurationService configurationService;

    @Value("${refresh.token.cleanup.batch-size:1000}")
    private int batchSize;

    @Value("${refresh.token.cleanup.cron:0 0 0 * * *}") // default: 24 hours
    private String jobTime;

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenCronjob.class);

    @PostConstruct
    public void scheduleTokenCleanup() {
        String cron = jobTime;
        logger.info("Scheduling refresh token cleanup with cron: {}", cron);

        taskScheduler.schedule(this::cleanupExpiredTokens, new CronTrigger(cron));
    }

    @Transactional
    public void cleanupExpiredTokens() {
        long startTime = System.currentTimeMillis();
        int totalDeleted = 0;
        int batchCount = 0;

        try {
            Date now = new Date();
            boolean hasMore = true;

            while (hasMore) {
                List<RefreshTokenImpl> expiredTokens = refreshTokenRepository
                        .findByExpiryDateBefore(now, PageRequest.of(0, batchSize))
                        .getContent();

                if (expiredTokens.isEmpty()) {
                    hasMore = false;
                } else {
                    expiredTokens.forEach(token -> refreshTokenRepository.deleteByToken(token.getToken()));
                    totalDeleted += expiredTokens.size();
                    batchCount++;

                    if (batchCount % 10 == 0) {
                        logger.info("Cleanup in progress - Processed {} batches, {} tokens deleted",
                                batchCount, totalDeleted);
                    }
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            double tokensPerSecond = (totalDeleted * 1000.0) / duration;

            logger.info("Cleanup completed - Duration: {} ms, Tokens deleted: {}, Batches processed: {}, Average rate: {} tokens/second",
                duration, totalDeleted, batchCount, String.format("%.2f", tokensPerSecond));


        } catch (Exception e) {
            logger.error("Error during refresh token cleanup: {}", e.getMessage(), e);
            throw e;
        }
    }
}
