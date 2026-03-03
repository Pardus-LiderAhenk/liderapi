package tr.org.lider.security;

import tr.org.lider.models.ConfigParams;

/**
 * JWT response paramters class
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 * 
 */

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username = "";
    private User user;
    private ConfigParams configParams;
    private Boolean isTwoFactorEnabled;
	private Integer otpExpiryDuration;
    private String refreshToken;

    public JwtResponse(String token, String username, String refreshToken) {
        this.token = token;
        this.username = username;
        this.refreshToken = refreshToken;
    }

    public JwtResponse(String token, User user, ConfigParams configParams) {
        this.token = token;
        this.user = user;
        this.configParams = configParams;
    }

    public JwtResponse(Boolean isTwoFactorEnabled, Integer otpExpiryDuration) {
		this.isTwoFactorEnabled = isTwoFactorEnabled;
		this.otpExpiryDuration = otpExpiryDuration;
	}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ConfigParams getConfigParams() {
        return configParams;
    }

    public void setConfigParams(ConfigParams configParams) {
        this.configParams = configParams;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

     public Boolean getIsTwoFactorEnabled() {
        return this.isTwoFactorEnabled;
    }

	public void setIsTwoFactorEnabled(Boolean isTwoFactorEnabled) {
		this.isTwoFactorEnabled = isTwoFactorEnabled;
	}

    public Integer getOtpExpiryDuration() {
        return otpExpiryDuration;
    }

    public void setOtpExpiryDuration(Integer otpExpiryDuration) {
        this.otpExpiryDuration = otpExpiryDuration;
    }
}