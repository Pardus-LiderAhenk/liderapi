package tr.org.lider.guacamole;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.guacamole.GuacamoleClientException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.io.GuacamoleReader;
import org.apache.guacamole.io.GuacamoleWriter;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;


/**
 * Guacamole Tunnel Servlet compatible with Spring Boot 3 (Jakarta EE).
 * Uses jakarta. Servlet instead of javax.servlet.
 */

public abstract class JakartaGuacamoleHTTPTunnelServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int BUFFER_SIZE = 8192;


    protected abstract GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        handleTunnelRequest(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        handleTunnelRequest(request, response);
    }

    private void handleTunnelRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String query = request.getQueryString();
        if (query.contains("?connect")) {
            query = "connect";
        }

        int idx = query.indexOf('?');
        if (idx != -1) {
            query = query.substring(idx + 1);
        }
        try {
            if (query == null) {
                throw new GuacamoleClientException("No query string provided.");
            }

            if ("connect".equals(query)) {
                GuacamoleTunnel tunnel = doConnect(request);
                if (tunnel != null) {
                    LiderTunnelManager.registerTunnel(tunnel);
                    try {
                        response.getWriter().write(tunnel.getUUID().toString());
                    } catch (IOException e) {
                        throw new GuacamoleException("Output error while writing tunnel UUID", e);
                    }
                } else {
                    throw new GuacamoleException("No tunnel created.");
                }
            }
            else if (query.startsWith("read:")) {
                String restUuid = query.substring(5);
                String uuid = restUuid.split(":")[0];
                GuacamoleTunnel tunnel = LiderTunnelManager.getTunnel(uuid);
                if (tunnel != null) {
                    doRead(response, tunnel);
                } else {
                    throw new GuacamoleClientException("Invalid tunnel UUID: " + uuid);
                }
            }
            else if (query.startsWith("write:")) {
                String restUuid = query.substring(6);
                String uuid = restUuid.split(":")[0]; // SADECE UUID
                GuacamoleTunnel tunnel = LiderTunnelManager.getTunnel(uuid);
                if (tunnel != null) {
                    doWrite(request, tunnel);
                } else {
                    throw new GuacamoleClientException("Invalid tunnel UUID: " + uuid);
                }
            }
            else {
                throw new GuacamoleClientException("Invalid query: " + query);
            }

        } catch (GuacamoleException e) {
            logger.error("Guacamole error", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Internal server error", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void doRead(HttpServletResponse response, GuacamoleTunnel tunnel) throws GuacamoleException, IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Cache-Control", "no-cache");

        GuacamoleReader reader = tunnel.acquireReader();
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));

            while (tunnel.isOpen()) {
                char[] instruction = reader.read();
                if (instruction == null) {
                    break;
                }
                writer.write(instruction);
                writer.flush();
            }
        } catch (Exception e) {
        } finally {
            tunnel.releaseReader();
            if (!tunnel.isOpen()) {
                LiderTunnelManager.removeTunnel(tunnel.getUUID().toString());
            }
        }
    }

    private void doWrite(HttpServletRequest request, GuacamoleTunnel tunnel) throws GuacamoleException, IOException {
        GuacamoleWriter writer = tunnel.acquireWriter();
        try {
            Reader inputReader = new InputStreamReader(request.getInputStream(), "UTF-8");
            char[] buffer = new char[BUFFER_SIZE];
            int length;

            while (tunnel.isOpen() && (length = inputReader.read(buffer)) != -1) {
                writer.write(buffer, 0, length);
            }
        } catch (Exception e) {
        } finally {
            tunnel.releaseWriter();
        }
    }
}