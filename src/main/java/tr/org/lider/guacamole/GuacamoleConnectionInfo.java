package tr.org.lider.guacamole;

import lombok.Data;

import java.time.Instant;

@Data
public class GuacamoleConnectionInfo {
    private String protocol;
    private String host;
    private String port;
    private String username;
    private String password;
    private String liderUser;
    private Instant expiresAt;
}