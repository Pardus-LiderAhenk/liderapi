package tr.org.lider.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "server" })
@Entity
@Table(name = "Server_Information")
public class ServerInformationImpl implements Serializable {
	
	private static final long serialVersionUID = 7634830693350923198L;

	@Id
	@GeneratedValue
	@Column(name = "SERVER_INFORMATION_ID", unique = true, nullable = false)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_DATE", nullable = false)
	@CreationTimestamp
	@JsonFormat(pattern="dd/MM/yyyy HH:mm:ss", timezone="Europe/Istanbul")
	private Date createDate;

	@ManyToOne(fetch = FetchType.LAZY)
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
	
	public ServerInformationImpl(Long id,ServerImpl server, String propertyName, String propertyValue,Date createDate) {
		this.id = id;
		this.server = server;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
		this.createDate = createDate;
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
	
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	
	@Override
	public String toString() {
		return "ServerInformationImpl [id=" + id + ", propertyName=" + propertyName + ", propertyValue=" + propertyValue + ", createDate=" + createDate +"]";
		
	}
}
