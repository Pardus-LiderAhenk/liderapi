package tr.org.lider.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name = "Server")
public class ServerImpl implements Serializable {

	private static final long serialVersionUID = 7634830693350923198L;

	@Id
	@GeneratedValue
	@Column(name = "SERVER_ID", unique = true,nullable = false)
	private Long id;
	
	@Column(name = "IP")
	private String ip;
	
	@Column(name = "HOSTNAME")
	private String hostname;

	@Column(name = "DESCRIPTION")
	private String description;
	
	@Column(name = "USER")
	private String user;
	
	@Column(name = "STATUS")
	private String status;
	
	@Column(name = "PASSWORD")
	private String password;
	
	@OneToMany(mappedBy = "server", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private Set<ServerInformationImpl> properties = new HashSet<ServerInformationImpl>(0); // bidirectional

	@OneToMany(mappedBy = "server", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = false)
	@OrderBy("createDate DESC")

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public ServerImpl() {
		
	}
	
	public ServerImpl(Long id, String description, String hostname, String ip, 
			String password, String status, String user) {
		
		this.id = id;
		this.description = description;
		this.hostname = hostname;
		this.ip = ip;
		this.password = password;
		this.status = status;
		this.user = user;
	}
	
	public ServerImpl(ServerImpl server) {
		this.id = server.id;
		this.description = server.description;
		this.hostname = server.hostname;
		this.ip = server.ip;
		this.password = server.password;
		this.status = server.status;
		this.user = server.user;
	}

	public String toString() {
		return "ServerImpl [id=" + id + ", description=" + description + ", hostname=" + hostname + ", ip=" + ip + " , "
				+ "password=" + password + ", status=" + status + ", user=" + user + "]";
				}
}
