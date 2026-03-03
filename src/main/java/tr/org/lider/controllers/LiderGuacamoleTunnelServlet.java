//package tr.org.lider.controllers;
//
//import java.net.InetSocketAddress;
//import java.net.Socket;
//import java.util.Date;
//
//import org.apache.guacamole.GuacamoleException;
//import org.apache.guacamole.net.GuacamoleSocket;
//import org.apache.guacamole.net.GuacamoleTunnel;
//import org.apache.guacamole.net.InetGuacamoleSocket;
//import org.apache.guacamole.net.SimpleGuacamoleTunnel;
//import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
//import org.apache.guacamole.protocol.GuacamoleConfiguration;
//import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.context.request.*;
//
//import tr.org.lider.entities.OperationLogImpl;
//import tr.org.lider.entities.OperationType;
//import tr.org.lider.repositories.OperationLogRepository;
//import tr.org.lider.services.AuthenticationService;
//
//@RestController
//public class LiderGuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet {
//
//    private static final long serialVersionUID = 1L;
//    Logger logger = LoggerFactory.getLogger(LiderGuacamoleTunnelServlet.class);
//
//    @Value("${lider.guacamole.guacd.host:localhost}")
//    private String guacdHost;
//
//    @Value("${lider.guacamole.guacd.port:4822}")
//    private int guacdPort;
//
//    private static String PROTOCOL = "";
//    private static String HOST = "";
//    private static String PORT = "";
//    private static String USERNAME = "";
//    private static String PASSWORD = "";
//    private static String LIDERUSER = "";
//
//    private final OperationLogRepository operationLogRepository;
//
//    public LiderGuacamoleTunnelServlet(OperationLogRepository operationLogRepository) {
//        this.operationLogRepository = operationLogRepository;
//    }
//
//    @Override
//    protected GuacamoleTunnel doConnect(javax.servlet.http.HttpServletRequest request) throws GuacamoleException {
//
//        try {
//            if (isEmpty(PROTOCOL) || isEmpty(HOST) || isEmpty(PORT) || isEmpty(PASSWORD)) {
//                logger.error("Missing connection parameters: protocol={}, host={}, port={}", PROTOCOL, HOST, PORT);
//                return null;
//            }
//
//            String cleanHost = HOST.replace("'", "").trim();
//            if (cleanHost.isEmpty()) {
//                logger.error("Host value invalid: '{}'", HOST);
//                return null;
//            }
//
//            try {
//                Integer.parseInt(PORT.trim());
//            } catch (Exception ex) {
//                logger.error("Invalid port: {}", PORT);
//                return null;
//            }
//
//            logger.info("Starting Remote Connection HOST={} PORT={} PROTOCOL={}", cleanHost, PORT, PROTOCOL);
//            GuacamoleConfiguration config = new GuacamoleConfiguration();
//            config.setProtocol(PROTOCOL.trim());
//
//            if (PROTOCOL.equalsIgnoreCase("ssh")) {
//                if (isEmpty(USERNAME)) {
//                    logger.error("SSH username is empty!");
//                    return null;
//                }
//                config.setParameter("hostname", cleanHost);
//                config.setParameter("port", PORT.trim());
//                config.setParameter("username", USERNAME.trim());
//                config.setParameter("password", PASSWORD);
//                config.setParameter("lideruser", LIDERUSER);
//            }
//            else if (PROTOCOL.equalsIgnoreCase("vnc")) {
//
//                config.setParameter("hostname", cleanHost);
//                config.setParameter("port", PORT.trim());
//                config.setParameter("password", PASSWORD);
//                config.setParameter("lideruser", LIDERUSER);
//
//                // VNC stability settings
//                config.setParameter("color-depth", "16");
//                config.setParameter("swap-red-blue", "false");
//                config.setParameter("cursor", "remote");
//                config.setParameter("read-only", "false");
//                config.setParameter("force-lossless", "false");
//
//                config.setParameter("autoretry", "3");
//                config.setParameter("encodings", "tight zrle ultra copyrect hextile zlib corre rre raw");
//                logger.info("VNC → hostname={}", cleanHost);
//
//            }
//            else if (PROTOCOL.equalsIgnoreCase("rdp")) {
//
//                if (isEmpty(USERNAME)) {
//                    logger.error("RDP username empty!");
//                    return null;
//                }
//                config.setProtocol("rdp");
//                config.setParameter("hostname", cleanHost);
//                config.setParameter("port", isEmpty(PORT) ? "3389" : PORT.trim());
//                config.setParameter("username", USERNAME.trim());
//                config.setParameter("password", PASSWORD);
//                config.setParameter("security", "any");
//                config.setParameter("ignore-cert", "true");
//                config.setParameter("enable-gfx", "false");
//                config.setParameter("enable-gfx-h264", "false");
//                config.setParameter("enable-gfx-progressive", "false");
//                config.setParameter("resize-method", "display-update");
//                config.setParameter("disable-wallpaper", "true");
//                config.setParameter("disable-theming", "true");
//                config.setParameter("disable-menu-animations", "true");
//                config.setParameter("connection-timeout", "60000");
//                config.setParameter("lideruser", LIDERUSER);
//            }
//            else {
//                logger.error("Unsupported protocol: {}", PROTOCOL);
//                return null;
//            }
//            GuacamoleSocket socket =
//                    new ConfiguredGuacamoleSocket(new InetGuacamoleSocket(guacdHost, guacdPort), config);
//            GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);
//
//            logger.info("Remote Access Tunnel Created → Host={} Protocol={} User={} Lider={}",
//                    cleanHost, PROTOCOL, USERNAME, LIDERUSER);
//            saveLog(cleanHost);
//            return tunnel;
//
//        } catch (Exception e) {
//            logger.error("Error creating tunnel: {}", e.getMessage(), e);
//            return null;
//        }
//    }
//
//    private void saveLog(String host) {
//        try {
//            OperationLogImpl log = new OperationLogImpl();
//            log.setCreateDate(new Date());
//            log.setCrudType(OperationType.EXECUTE_TASK);
//            log.setLogMessage("Connected to device with IP " + host + " using " + PROTOCOL.toUpperCase());
//            log.setRequestData(null);
//            log.setTaskId(null);
//            log.setProfileId(null);
//            log.setPolicyId(null);
//
//            if (AuthenticationService.isLogged()) {
//                log.setUserId(AuthenticationService.getUserName());
//            } else {
//                log.setUserId(LIDERUSER);
//            }
//
//            jakarta.servlet.http.HttpServletRequest httpServletRequest =
//                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//            log.setRequestIp(httpServletRequest.getRemoteAddr());
//            operationLogRepository.save(log);
//        } catch (Exception ex) {
//            logger.error("Error saving remote access log: {}", ex.getMessage());
//        }
//    }
//
//    @RequestMapping(path = "tunnel", method = {RequestMethod.POST, RequestMethod.GET})
//    protected void handleTunnelRequest(
//            javax.servlet.http.HttpServletRequest request,
//            javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException {
//        super.handleTunnelRequest(request, response);
//    }
//
//    @RequestMapping(value = "/sendremote", method = {RequestMethod.POST})
//    public ResponseEntity<String> getRemote(
//            @RequestParam("protocol") String protocol,
//            @RequestParam("host") String host,
//            @RequestParam("port") String port,
//            @RequestParam("username") String username,
//            @RequestParam("password") String password,
//            @RequestParam("lideruser") String lideruser) {
//
//        PROTOCOL = protocol;
//        HOST = host;
//        PORT = port;
//        USERNAME = username;
//        PASSWORD = password;
//        LIDERUSER = lideruser;
//
//        logger.info("Remote Params Saved → PROTOCOL={} HOST={} PORT={} USER={}", protocol, host, port, username);
//        return new ResponseEntity<>("OK", HttpStatus.OK);
//    }
//
//    @RequestMapping(value = "/remote", method = {RequestMethod.GET, RequestMethod.POST})
//    public String getRemote() {
//        return "guac";
//    }
//
//    @RequestMapping(value = "/checkhost", method = {RequestMethod.GET, RequestMethod.POST})
//    public ResponseEntity<String> getAvailableHostAddress(
//            @RequestParam("host") String hostStr,
//            @RequestParam("port") String port) {
//
//        String clean = hostStr.replace("'", "");
//        String[] arr = clean.contains(",") ? clean.split(",") : new String[]{clean};
//
//        for (String h : arr) {
//            if (checkIpPortAvailable(h.trim(), port)) {
//                return ResponseEntity.ok(h.trim());
//            }
//        }
//        return ResponseEntity.ok("");
//    }
//
//    private boolean checkIpPortAvailable(String ip, String port) {
//        try (Socket socket = new Socket()) {
//            socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 1000);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    private boolean isEmpty(String s) {
//        return s == null || s.trim().isEmpty();
//    }
//}
//package tr.org.lider.controllers;
//
//import java.net.InetSocketAddress;
//import java.net.Socket;
//import java.util.Date;
//
//import org.apache.guacamole.GuacamoleException;
//import org.apache.guacamole.net.GuacamoleSocket;
//import org.apache.guacamole.net.GuacamoleTunnel;
//import org.apache.guacamole.net.InetGuacamoleSocket;
//import org.apache.guacamole.net.SimpleGuacamoleTunnel;
//import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
//import org.apache.guacamole.protocol.GuacamoleConfiguration;
//import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.context.request.*;
//
//import tr.org.lider.entities.OperationLogImpl;
//import tr.org.lider.entities.OperationType;
//import tr.org.lider.repositories.OperationLogRepository;
//import tr.org.lider.services.AuthenticationService;
//
//@RestController
//public class LiderGuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet {
//
//    private static final long serialVersionUID = 1L;
//    Logger logger = LoggerFactory.getLogger(LiderGuacamoleTunnelServlet.class);
//
//    @Value("${lider.guacamole.guacd.host:localhost}")
//    private String guacdHost;
//
//    @Value("${lider.guacamole.guacd.port:4822}")
//    private int guacdPort;
//
//    private static String PROTOCOL = "";
//    private static String HOST = "";
//    private static String PORT = "";
//    private static String USERNAME = "";
//    private static String PASSWORD = "";
//    private static String LIDERUSER = "";
//
//    private final OperationLogRepository operationLogRepository;
//
//    public LiderGuacamoleTunnelServlet(OperationLogRepository operationLogRepository) {
//        this.operationLogRepository = operationLogRepository;
//    }
//
//    @Override
//    protected GuacamoleTunnel doConnect(javax.servlet.http.HttpServletRequest request) throws GuacamoleException {
//
//        try {
//            if (isEmpty(PROTOCOL) || isEmpty(HOST) || isEmpty(PORT) || isEmpty(PASSWORD)) {
//                logger.error("Missing connection parameters: protocol={}, host={}, port={}", PROTOCOL, HOST, PORT);
//                return null;
//            }
//
//            String cleanHost = HOST.replace("'", "").trim();
//            if (cleanHost.isEmpty()) {
//                logger.error("Host value invalid: '{}'", HOST);
//                return null;
//            }
//
//            try {
//                Integer.parseInt(PORT.trim());
//            } catch (Exception ex) {
//                logger.error("Invalid port: {}", PORT);
//                return null;
//            }
//
//            logger.info("Starting Remote Connection HOST={} PORT={} PROTOCOL={}", cleanHost, PORT, PROTOCOL);
//            GuacamoleConfiguration config = new GuacamoleConfiguration();
//            config.setProtocol(PROTOCOL.trim());
//
//            if (PROTOCOL.equalsIgnoreCase("ssh")) {
//                if (isEmpty(USERNAME)) {
//                    logger.error("SSH username is empty!");
//                    return null;
//                }
//                config.setParameter("hostname", cleanHost);
//                config.setParameter("port", PORT.trim());
//                config.setParameter("username", USERNAME.trim());
//                config.setParameter("password", PASSWORD);
//                config.setParameter("lideruser", LIDERUSER);
//            }
//            else if (PROTOCOL.equalsIgnoreCase("vnc")) {
//
//                config.setParameter("hostname", cleanHost);
//                config.setParameter("port", PORT.trim());
//                config.setParameter("password", PASSWORD);
//                config.setParameter("lideruser", LIDERUSER);
//
//                // VNC stability settings
//                config.setParameter("color-depth", "16");
//                config.setParameter("swap-red-blue", "false");
//                config.setParameter("cursor", "remote");
//                config.setParameter("read-only", "false");
//                config.setParameter("force-lossless", "false");
//
//                config.setParameter("autoretry", "3");
//                config.setParameter("encodings", "tight zrle ultra copyrect hextile zlib corre rre raw");
//                logger.info("VNC → hostname={}", cleanHost);
//
//            }
//            else if (PROTOCOL.equalsIgnoreCase("rdp")) {
//
//                if (isEmpty(USERNAME)) {
//                    logger.error("RDP username empty!");
//                    return null;
//                }
//                config.setProtocol("rdp");
//                config.setParameter("hostname", cleanHost);
//                config.setParameter("port", isEmpty(PORT) ? "3389" : PORT.trim());
//                config.setParameter("username", USERNAME.trim());
//                config.setParameter("password", PASSWORD);
//                config.setParameter("security", "any");
//                config.setParameter("ignore-cert", "true");
//                config.setParameter("enable-gfx", "false");
//                config.setParameter("enable-gfx-h264", "false");
//                config.setParameter("enable-gfx-progressive", "false");
//                config.setParameter("resize-method", "display-update");
//                config.setParameter("disable-wallpaper", "true");
//                config.setParameter("disable-theming", "true");
//                config.setParameter("disable-menu-animations", "true");
//                config.setParameter("connection-timeout", "60000");
//                config.setParameter("lideruser", LIDERUSER);
//            }
//            else {
//                logger.error("Unsupported protocol: {}", PROTOCOL);
//                return null;
//            }
//            GuacamoleSocket socket =
//                    new ConfiguredGuacamoleSocket(new InetGuacamoleSocket(guacdHost, guacdPort), config);
//            GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);
//
//            logger.info("Remote Access Tunnel Created → Host={} Protocol={} User={} Lider={}",
//                    cleanHost, PROTOCOL, USERNAME, LIDERUSER);
//            saveLog(cleanHost);
//            return tunnel;
//
//        } catch (Exception e) {
//            logger.error("Error creating tunnel: {}", e.getMessage(), e);
//            return null;
//        }
//    }
//
//    private void saveLog(String host) {
//        try {
//            OperationLogImpl log = new OperationLogImpl();
//            log.setCreateDate(new Date());
//            log.setCrudType(OperationType.EXECUTE_TASK);
//            log.setLogMessage("Connected to device with IP " + host + " using " + PROTOCOL.toUpperCase());
//            log.setRequestData(null);
//            log.setTaskId(null);
//            log.setProfileId(null);
//            log.setPolicyId(null);
//
//            if (AuthenticationService.isLogged()) {
//                log.setUserId(AuthenticationService.getUserName());
//            } else {
//                log.setUserId(LIDERUSER);
//            }
//
//            jakarta.servlet.http.HttpServletRequest httpServletRequest =
//                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//            log.setRequestIp(httpServletRequest.getRemoteAddr());
//            operationLogRepository.save(log);
//        } catch (Exception ex) {
//            logger.error("Error saving remote access log: {}", ex.getMessage());
//        }
//    }
//
//    @RequestMapping(path = "tunnel", method = {RequestMethod.POST, RequestMethod.GET})
//    protected void handleTunnelRequest(
//            javax.servlet.http.HttpServletRequest request,
//            javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException {
//        super.handleTunnelRequest(request, response);
//    }
//
//    @RequestMapping(value = "/sendremote", method = {RequestMethod.POST})
//    public ResponseEntity<String> getRemote(
//            @RequestParam("protocol") String protocol,
//            @RequestParam("host") String host,
//            @RequestParam("port") String port,
//            @RequestParam("username") String username,
//            @RequestParam("password") String password,
//            @RequestParam("lideruser") String lideruser) {
//
//        PROTOCOL = protocol;
//        HOST = host;
//        PORT = port;
//        USERNAME = username;
//        PASSWORD = password;
//        LIDERUSER = lideruser;
//
//        logger.info("Remote Params Saved → PROTOCOL={} HOST={} PORT={} USER={}", protocol, host, port, username);
//        return new ResponseEntity<>("OK", HttpStatus.OK);
//    }
//
//    @RequestMapping(value = "/remote", method = {RequestMethod.GET, RequestMethod.POST})
//    public String getRemote() {
//        return "guac";
//    }
//
//    @RequestMapping(value = "/checkhost", method = {RequestMethod.GET, RequestMethod.POST})
//    public ResponseEntity<String> getAvailableHostAddress(
//            @RequestParam("host") String hostStr,
//            @RequestParam("port") String port) {
//
//        String clean = hostStr.replace("'", "");
//        String[] arr = clean.contains(",") ? clean.split(",") : new String[]{clean};
//
//        for (String h : arr) {
//            if (checkIpPortAvailable(h.trim(), port)) {
//                return ResponseEntity.ok(h.trim());
//            }
//        }
//        return ResponseEntity.ok("");
//    }
//
//    private boolean checkIpPortAvailable(String ip, String port) {
//        try (Socket socket = new Socket()) {
//            socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 1000);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    private boolean isEmpty(String s) {
//        return s == null || s.trim().isEmpty();
//    }
//}
