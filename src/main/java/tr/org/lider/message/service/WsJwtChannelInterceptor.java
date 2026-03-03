package tr.org.lider.message.service;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import tr.org.lider.security.JwtProvider;

/**
 * Intercepts STOMP CONNECT to validate JWT and set authenticated user.
 * Rejects connection when token is missing or invalid.
 * Uses only JwtProvider to avoid circular dependency with UserService.
 * @author <a href="mailto:haydar.urdogan@tubitak.gov.tr">Haydar Urdoğan</a>
 */
@Component

@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WsJwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WsJwtChannelInterceptor.class);

    private final JwtProvider jwtProvider;

    public WsJwtChannelInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    @Nullable
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractBearerToken(accessor);
            if (token == null || token.isBlank()) {
                logger.warn("WebSocket CONNECT rejected: missing Authorization token");
                return null;
            }
            if (!jwtProvider.validateJwtToken(token)) {
                logger.warn("WebSocket CONNECT rejected: invalid or expired JWT");
                return null;
            }
            String username = jwtProvider.getUserNameFromJwtToken(token);
            Principal principal = () -> username;
            accessor.setUser(principal);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // Only allow subscribing to own task channel: /liderws/task/{username}
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/liderws/task/")) {
                String principalName = accessor.getUser() != null ? accessor.getUser().getName() : null;
                String subscribedUser = destination.substring("/liderws/task/".length());
                if (principalName == null || !principalName.equals(subscribedUser)) {
                    logger.warn("WebSocket SUBSCRIBE rejected: user {} may not subscribe to {}", principalName, destination);
                    return null;
                }
            }
        }

        return message;
    }

    private String extractBearerToken(StompHeaderAccessor accessor) {
        List<String> auth = accessor.getNativeHeader("Authorization");
        if (auth == null || auth.isEmpty()) {
            return null;
        }
        String value = auth.get(0);
        if (value != null && value.startsWith("Bearer ")) {
            return value.substring(7).trim();
        }
        return null;
    }
}