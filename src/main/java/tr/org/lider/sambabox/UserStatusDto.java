package tr.org.lider.sambabox;

import java.io.Serializable;

public class UserStatusDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8998370735383486616L;
	
	
	private String username;
	private Boolean status;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Boolean getStatus() {
		return status;
	}
	public void setStatus(Boolean status) {
		this.status = status;
	}

}
