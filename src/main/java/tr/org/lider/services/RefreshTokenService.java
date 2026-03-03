package tr.org.lider.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tr.org.lider.entities.RefreshTokenImpl;
import tr.org.lider.repositories.RefreshTokenRepository;
import java.util.Date;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${refresh.token.expiry.seconds:86400}") // default: 24 hours
    private long tokenExpirySeconds;


    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshTokenImpl createRefreshToken(String userName) {
        RefreshTokenImpl refreshToken = new RefreshTokenImpl();
        refreshToken.setUsername(userName);
        refreshToken.setToken(UUID.randomUUID().toString());

        long expiryInMillis = tokenExpirySeconds * 1000;
        refreshToken.setExpiryDate(new Date(System.currentTimeMillis() + expiryInMillis));

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshTokenImpl findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    /**
     * Delete all refresh tokens for a specific user
     * @param username the username to delete tokens for
     */
    public void deleteAllRefreshTokensForUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Attempted to delete refresh tokens with null or empty username");
            return;
        }
        try {
            refreshTokenRepository.deleteByUsername(username);
            logger.info("Successfully deleted all refresh tokens for user: {}", username);
        } catch (Exception e) {
            logger.error("Error deleting refresh tokens for user {}: {}", username, e.getMessage());
            throw e; 
        }
    }

    public boolean validateRefreshToken(RefreshTokenImpl refreshToken) {
        if (refreshToken == null) {
            return false;
        }
        
        // Check if token is expired
        if (refreshToken.getExpiryDate().before(new Date())) {
            deleteRefreshToken(refreshToken.getToken());
            return false;
        }
        
        return true;
    }



} 