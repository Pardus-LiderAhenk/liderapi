package tr.org.lider.guacamole;

import org.apache.guacamole.net.GuacamoleTunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LiderTunnelManager {
    private static final Logger logger = LoggerFactory.getLogger(LiderTunnelManager.class);
    private static final Map<String, GuacamoleTunnel> tunnels = new ConcurrentHashMap<>();

    public static void registerTunnel(GuacamoleTunnel tunnel) {
        tunnels.put(tunnel.getUUID().toString(), tunnel);
        logger.info("Tunnel registered: {}", tunnel.getUUID());
    }

    public static GuacamoleTunnel getTunnel(String uuid) {
        return tunnels.get(uuid);
    }

    public static void removeTunnel(String uuid) {
        if (tunnels.remove(uuid) != null) {
            logger.info("Tunnel removed: {}", uuid);
        }
    }
}