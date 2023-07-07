package tr.org.lider.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Server_Information")
public class ServerInformationImpl implements Serializable {
	
	private static final long serialVersionUID = 7634830693350923198L;

	@Id
	@GeneratedValue
	@Column(name = "SERVER_INFORMATION_ID", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SERVER_ID", nullable = false)
	private ServerImpl server; // bidirectional

	@Column(name = "PROPERTY_NAME", nullable = false)
	private String propertyName;

	@Column(name = "PROPERTY_VALUE", columnDefinition = "TEXT", nullable = false, length = 65535)
	private String propertyValue;
	
	public ServerInformationImpl() {
		// TODO Auto-generated constructor stub
	}
	
	public ServerInformationImpl(Long id, ServerImpl server, String propertyName, String propertyValue) {
		this.id = id;
		this.server = server;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}
	
	public ServerInformationImpl(ServerImpl server, String propertyName, String propertyValue) {
		this.server = server;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}
	

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ServerImpl getServer() {
		return server;
	}

	public void setServer(ServerImpl server) {
		this.server = server;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	public String toString() {
		return "ServerImpl [id=" + id + ", propertyName=" + propertyName + ", propertyValue=" + propertyValue +"]";
		
	}
}
