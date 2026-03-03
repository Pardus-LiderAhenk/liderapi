package tr.org.lider.guacamole;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import tr.org.lider.constant.RoleConstants;
import tr.org.lider.services.AuthenticationService;

import java.net.InetSocketAddress;
import java.net.Socket;

@Secured({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_REMOTE_ACCESS})
@RestController
@RequestMapping("/api/guacamole")
public class LiderGuacamoleController {

    private static final Logger logger = LoggerFactory.getLogger(LiderGuacamoleController.class);
    private final LiderConnectionCache connectionCache;

    public LiderGuacamoleController(LiderConnectionCache connectionCache) {
        this.connectionCache = connectionCache;
    }

    @PostMapping("/sendremote")
    public ResponseEntity<?> prepareConnection(
            @RequestParam("protocol") String protocol,
            @RequestParam("host") String host,
            @RequestParam("port") String port,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request) {

        try {
            String lideruser = AuthenticationService.getUserName();
            GuacamoleConnectionInfo info = new GuacamoleConnectionInfo();
            info.setProtocol(protocol);
            info.setHost(host);
            info.setPort(port);
            info.setUsername(username);
            info.setPassword(password);
            info.setLiderUser(lideruser);

            String connectionId = connectionCache.storeConnection(info);
            logger.info("Remote connection prepared for liderUser: {}, host: {}, connectionId: {}",
                    lideruser, host, connectionId);

            return ResponseEntity.ok(connectionId);

        } catch (Exception e) {
            logger.error("Error preparing connection for liderUser: {}", AuthenticationService.getUserName(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Connection preparation failed: " + e.getMessage());
        }
    }

    @PostMapping("/close-connection")
    public ResponseEntity<?> closeConnection(@RequestParam("connectionId") String connectionId) {
        try {
            boolean removed = connectionCache.removeConnectionById(connectionId);
            if (removed) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Connection not found for ID: " + connectionId);
            }

        } catch (Exception e) {
            logger.error("Error closing connection for ID: {}", connectionId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to close connection");
        }
    }

    @RequestMapping(value = "/checkhost", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> checkHost(
            @RequestParam("host") String hostStr,
            @RequestParam("port") String port) {

        try {
            if (hostStr == null || hostStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Host cannot be empty");
            }

            if (port == null || !port.matches("\\d+")) {
                return ResponseEntity.badRequest().body("Invalid port");
            }

            String clean = hostStr.replace("'", "").replace("\"", "");
            String[] arr = clean.contains(",") ? clean.split(",") : new String[]{clean};

            for (String h : arr) {
                String trimmed = h.trim();
                if (!trimmed.isEmpty() && checkIpPortAvailable(trimmed, port)) {
                    logger.debug("Host check successful: {}:{}", trimmed, port);
                    return ResponseEntity.ok(trimmed);
                }
            }

            logger.warn("No available host found in: {}", hostStr);
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No available host found");

        } catch (Exception e) {
            logger.error("Error checking host: {}", hostStr, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Host check failed");
        }
    }

    private boolean checkIpPortAvailable(String ip, String portStr) {
        try (Socket socket = new Socket()) {
            int port = Integer.parseInt(portStr);

            if (port < 1 || port > 65535) {
                logger.warn("Invalid port number: {}", port);
                return false;
            }

            if (ip.matches(".*[;|&`$].*")) {
                logger.warn("Potentially malicious IP detected: {}", ip);
                return false;
            }

            socket.connect(new InetSocketAddress(ip, port), 1000);
            return true;

        } catch (Exception e) {
            logger.debug("Host {}:{} not available: {}", ip, portStr, e.getMessage());
            return false;
        }
    }
}