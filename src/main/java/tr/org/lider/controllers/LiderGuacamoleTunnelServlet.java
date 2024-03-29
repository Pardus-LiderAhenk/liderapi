package tr.org.lider.controllers;

import java.net.InetSocketAddress;
import java.net.Socket;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import tr.org.lider.entities.OperationType;
import tr.org.lider.services.OperationLogService;

@Controller
public class LiderGuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Logger logger = LoggerFactory.getLogger(LiderGuacamoleTunnelServlet.class);

	private static String PROTOCOL = "";
	private static String HOST = "";
	private static String PORT = "";
	private static String USERNAME = "";
	private static String PASSWORD = "";
	private static String DOMAIN = "";

	@Autowired
	private OperationLogService operationLogService;

	@Override
	protected GuacamoleTunnel doConnect(HttpServletRequest request) {

		// Create our configuration
//    		PROTOCOL="rdp";
//    		HOST="rdp";
//    		PORT="rdp";
//    		PASSWORD="rdp";
//    		HOST="192.168.56.108";
//    		PROTOCOL="vnc";
//    		PORT = "5900";
//    		PASSWORD ="123";

		if (PROTOCOL != "" && HOST != "" && PORT != "" && PASSWORD != "") {

			logger.info("Starting Remote Connection HOST: " + HOST + " PORT: " + PORT + " PROTOCOL" + PROTOCOL);
			
			String host = HOST.replace("'", "");
			
			if (host != null) {
				try {
					GuacamoleConfiguration config = new GuacamoleConfiguration();
					config.setProtocol(PROTOCOL);
					if (PROTOCOL.equals("ssh")) {
						config.setParameter("hostname", host.trim());
						config.setParameter("port", PORT);
						config.setParameter("password", PASSWORD);
						config.setParameter("username", USERNAME);
					} else if (PROTOCOL.equals("vnc")) {

						config.setParameter("hostname", host.trim());
						logger.info("Connection remote to host: " + host.trim());
						logger.info("Connection remote to password: " + PASSWORD);
						config.setParameter("port", PORT);
						config.setParameter("password", PASSWORD);
					} else if (PROTOCOL.equals("rdp")) {

						config.setParameter("hostname", HOST);
						config.setParameter("port", "3389");
						config.setParameter("username", USERNAME);
						config.setParameter("password", PASSWORD);
						config.setParameter("domain", DOMAIN);
						config.setParameter("ignore-cert", "true");

					}
					// Connect to guacd
					GuacamoleSocket socket = new ConfiguredGuacamoleSocket(new InetGuacamoleSocket("localhost", 4822),
							config);
					// Return a new tunnel which uses the connected socket
					GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);

					System.out.println("Uzak Erişim Görevi Çalıştırıldı. Host:" + HOST + " Protokol: " + PROTOCOL
							+ " User:" + USERNAME);

					operationLogService.saveOperationLog(OperationType.EXECUTE_TASK,
							"Uzak Erişim Görevi Çalıştırıldı. Host:" + HOST + " Protokol: " + PROTOCOL + " User:"
									+ USERNAME,
							null);

					return tunnel;

				} catch (Exception e) {
					logger.error(" Error occured when cretaing remote tunnel. Error:" + e.getMessage());
					e.printStackTrace();
					return null;
				}
			} else {
				return null;
			}

		} else {
			return null;
		}

	}

	private boolean checkIpPortAvailable(String ip, String port) {

		Socket socket = null;
		InetSocketAddress address = new InetSocketAddress(ip, Integer.parseInt(port));
		try {
			socket = new Socket();
			socket.connect(address, 1000);
			socket.close();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	@Override
	@RequestMapping(path = "tunnel", method = { RequestMethod.POST, RequestMethod.GET })
	protected void handleTunnelRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		super.handleTunnelRequest(request, response);
	}

	@RequestMapping(value = "/sendremote", method = { RequestMethod.POST })
	public ResponseEntity<String> getRemote(@RequestParam(value = "protocol") String protocol,
			@RequestParam(value = "host") String host, @RequestParam(value = "port") String port,
			@RequestParam(value = "username") String username, @RequestParam(value = "password") String password) {

		LiderGuacamoleTunnelServlet.PROTOCOL = protocol;
		LiderGuacamoleTunnelServlet.HOST = host;
		LiderGuacamoleTunnelServlet.PORT = port;
		LiderGuacamoleTunnelServlet.USERNAME = username;
		LiderGuacamoleTunnelServlet.PASSWORD = password;
		return new ResponseEntity<String>("OK", HttpStatus.OK);
	}

	@RequestMapping(value = "/remote", method = { RequestMethod.GET, RequestMethod.POST })
	public String getRemote() {
		return "guac";
	}
	
	@RequestMapping(value = "/checkhost", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<String> getAvailableHostAddress(@RequestParam(value = "host") String hostStr,@RequestParam(value = "port") String port) {
		String[] hostArr = null;
		String host = null;
		if (hostStr.contains(",")) {
			hostArr = hostStr.replace("'", "").split(",");
		} else {
			hostArr = new String[1];
			hostArr[0] = hostStr.replace("'", "");
		}

		for (String availablehost : hostArr) {
			if (checkIpPortAvailable(availablehost.trim(), port)) {
				host = availablehost;
			}
		}
		
		return new ResponseEntity<String>(host, HttpStatus.OK);
	}
}
