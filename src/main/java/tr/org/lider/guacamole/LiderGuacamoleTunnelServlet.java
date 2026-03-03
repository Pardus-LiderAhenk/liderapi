package tr.org.lider.guacamole;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import tr.org.lider.entities.OperationLogImpl;
import tr.org.lider.entities.OperationType;
import tr.org.lider.repositories.OperationLogRepository;
import tr.org.lider.services.AuthenticationService;

import java.time.Instant;
import java.util.Date;


@Component
public class LiderGuacamoleTunnelServlet extends JakartaGuacamoleHTTPTunnelServlet {

    private final Logger logger = LoggerFactory.getLogger(LiderGuacamoleTunnelServlet.class);
    private final LiderConnectionCache connectionCache;
    private final OperationLogRepository operationLogRepository;

    @Value("${lider.guacamole.guacd.host:localhost}")
    private String guacdHost;

    @Value("${lider.guacamole.guacd.port:4822}")
    private int guacdPort;

    private static final String CACHE_NAME = "guacamoleConnections";

    public LiderGuacamoleTunnelServlet(LiderConnectionCache connectionCache, OperationLogRepository operationLogRepository) {
        this.connectionCache = connectionCache;
        this.operationLogRepository = operationLogRepository;
    }

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException {
        String authId = request.getParameter("authId");

        if (authId != null && authId.contains("?")) {
            authId = authId.substring(0, authId.indexOf('?'));
        }

        GuacamoleConnectionInfo info = connectionCache.getInfo(authId);
        if (info == null) {
            throw new IllegalStateException("Cache not available.");
        }

        if (info == null) {
            throw new AccessDeniedException("Invalid tunnel token.");
        }
        if (info.getExpiresAt() != null && info.getExpiresAt().isBefore(Instant.now())) {
            connectionCache.removeConnectionById(authId);
            throw new AccessDeniedException("Tunnel token expired.");
        }
        connectionCache.removeConnectionById(authId);

        if (info == null) {
            logger.error("Invalid or expired auth token");
            throw new GuacamoleException("Connection token invalid or expired.");
        }

        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol(info.getProtocol());

        if ("ssh".equals(info.getProtocol())) {
            if (isEmpty(info.getUsername())) {
                logger.error("SSH username is empty!");
                return null;
            }
            config.setParameter("hostname", info.getHost());
            config.setParameter("port", isEmpty(info.getPort()) ? "22" : info.getPort());
            config.setParameter("username", info.getUsername());
            config.setParameter("password", info.getPassword());
            config.setParameter("lideruser", info.getLiderUser());
            //config.setParameter("ignore-host-key", "true");

        }
        else if ("rdp".equals(info.getProtocol())) {
            config.setParameter("hostname", info.getHost());
            config.setParameter("port", isEmpty(info.getPort()) ? "3389" : info.getPort());
            config.setParameter("username", info.getUsername());
            config.setParameter("password", info.getPassword());
            config.setParameter("security", "any");
            config.setParameter("ignore-cert", "true");
            config.setParameter("enable-gfx", "false");
            config.setParameter("enable-gfx-h264", "false");
            config.setParameter("enable-gfx-progressive", "false");

//            config.setParameter("resize-method", "display-update");
            config.setParameter("disable-wallpaper", "true");
            config.setParameter("disable-theming", "true");
            config.setParameter("disable-menu-animations", "true");
            config.setParameter("connection-timeout", "10000");

            config.setParameter("force-encryption", "true");
            config.setParameter("color-depth", "16"); // 32 yerine 16 bit hızı artırır

            config.setParameter("disable-bitmap-caching", "true");
            config.setParameter("disable-offscreen-caching", "true");
            config.setParameter("disable-glyph-caching", "true");
            config.setParameter("enable-font-smoothing", "true"); // Yazıların okunabilirliği için

            config.setParameter("lideruser", info.getLiderUser());
        }

        else if ("vnc".equals(info.getProtocol())) {
            config.setParameter("hostname", info.getHost());
            config.setParameter("port", info.getPort());
            config.setParameter("password", info.getPassword());
            config.setParameter("lideruser", info.getLiderUser());

            config.setParameter("color-depth", "16");
            config.setParameter("swap-red-blue", "false");
            config.setParameter("cursor", "remote");
            config.setParameter("read-only", "false");
            config.setParameter("force-lossless", "false");
            config.setParameter("autoretry", "3");
            config.setParameter("encodings", "tight zrle ultra copyrect hextile zlib corre rre raw");
        }
        else {
            logger.error("Unsupported protocol: {}", info.getProtocol());
            return null;
        }

        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket(guacdHost, guacdPort),
                config
        );

        return new SimpleGuacamoleTunnel(socket);
    }

    private void saveLog(String host) {
        try {
            OperationLogImpl log = new OperationLogImpl();
            log.setCreateDate(new Date());
            log.setCrudType(OperationType.EXECUTE_TASK);
            log.setLogMessage("Connected to device with IP " + host + " using " + "PROTOCOL.toUpperCase()");
            log.setRequestData(null);
            log.setTaskId(null);
            log.setProfileId(null);
            log.setPolicyId(null);

            if (AuthenticationService.isLogged()) {
                log.setUserId(AuthenticationService.getUserName());
            } else {
                log.setUserId("info.getLiderUser()");
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest httpServletRequest = attributes.getRequest();
                log.setRequestIp(httpServletRequest.getRemoteAddr());
            }

            operationLogRepository.save(log);
        } catch (Exception ex) {
            logger.error("Error saving remote access log: {}", ex.getMessage());
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}