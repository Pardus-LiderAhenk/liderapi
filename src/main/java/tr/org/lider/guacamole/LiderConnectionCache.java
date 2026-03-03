package tr.org.lider.guacamole;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import tr.org.lider.services.AuthenticationService;

import javax.cache.Cache;
import javax.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LiderConnectionCache {

    private final Logger logger = LoggerFactory.getLogger(LiderConnectionCache.class);
    private final CacheManager cacheManager;

    private static final String CACHE_NAME = "guacamoleConnections";


    public String storeConnection(GuacamoleConnectionInfo info) {

        if (!AuthenticationService.isLogged()) {
            throw new AccessDeniedException("Authentication is required.");
        }

        String loggedInUser = AuthenticationService.getUserName();
        if (!loggedInUser.equals(info.getLiderUser())) {
            throw new AccessDeniedException("You can only store connections for your own user.");
        }

        Cache<String, GuacamoleConnectionInfo> cache =
                cacheManager.getCache(CACHE_NAME, String.class, GuacamoleConnectionInfo.class);

        if (cache == null) {
            throw new IllegalStateException("Cache not available.");
        }

        String tunnelToken = UUID.randomUUID().toString();
        info.setExpiresAt(Instant.now().plusSeconds(30));

        cache.put(tunnelToken, info);

        logger.info("Tunnel token created. User: '{}', ExpiresAt: '{}'",
                info.getHost(),
                info.getLiderUser(),
                info.getExpiresAt());
        return tunnelToken;
    }

    /**
     * Retrieves connection info using the encrypted connection ID as the key.
     */
    public GuacamoleConnectionInfo getInfo(String connectionId) {
        if (connectionId == null) return null;

        Cache<String, GuacamoleConnectionInfo> cache = cacheManager.getCache(CACHE_NAME, String.class, GuacamoleConnectionInfo.class);
        if (cache == null) {
            logger.error("Cache '{}' not found!", CACHE_NAME);
            return null;
        }

        GuacamoleConnectionInfo connectionInfo = cache.get(connectionId);
        if (connectionInfo == null) {
            logger.warn("No active connection found for ID: {}", connectionId);
            return null;
        }
        return connectionInfo;
    }

    /**
     * Removes the connection from the cache using the encrypted connection ID.
     */
    public Boolean removeConnectionById(String connectionId) {
        if (connectionId == null) {
            return false;
        }
        Cache<String, GuacamoleConnectionInfo> cache = cacheManager.getCache(CACHE_NAME, String.class, GuacamoleConnectionInfo.class);

        if (cache != null) {
            boolean isRemoved = cache.remove(connectionId);
            if (isRemoved) {
                logger.info("Guacamole connection removed from cache for ID: {}", connectionId);
            } else {
                logger.warn("No connection found in cache to remove for ID: {}", connectionId);
            }
            return isRemoved;
        }

        logger.error("Cache '{}' not found!", CACHE_NAME);
        return false;
    }
}