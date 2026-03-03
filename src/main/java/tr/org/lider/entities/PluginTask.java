package tr.org.lider.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import tr.org.lider.ldap.DNType;
import tr.org.lider.ldap.LdapEntry;

@Entity
@Table(name = "c_plugin_task")
public class PluginTask implements Serializable {

	private static final long serialVersionUID = -8885880361026360284L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;
	
	@Column(name = "name")
	private String name;

	@Column(name = "page")
	private String page;
	
	@Column(name = "description")
	private String description;
	
	
	@Column(name = "commandId")
	private String commandId;
	
	@Column(name = "is_multi")
	private Boolean isMulti;
	
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "plugin_id", nullable = false)
	private PluginImpl plugin; // unidirectional
	
	@Column(name = "state")
	private int state;

    @Column(name = "role")
    private String role;
	
	@Transient
	private DNType dnType;
	
	@Transient
	private List<String> dnList;
	
	
	@Transient
	private List<LdapEntry> entryList;
	
	
	@Transient
	private String cronExpression;
	
	
	@Transient
	private Map<String, Object> parameterMap;
	
	@Transient
	private Date activationDate;
	
	@Transient
	private boolean taskParts;
	
	public PluginTask() {
		
	}

	public PluginTask(String name, String page, String description, String command_id, Boolean is_multi, PluginImpl plugin,
			Integer state, String role) {
		this.name = name;
		this.page = page;
		this.description = description;
		this.commandId = command_id;
		this.isMulti = is_multi;
		this.plugin = plugin;
		this.state = state;
        this.role = role;
	}

    public void updateFrom(PluginTask source) {
        this.name = source.getName();
        this.page = source.getPage();
        this.description = source.getDescription();
        this.commandId = source.getCommandId();
        this.isMulti = source.getIsMulti();
        this.plugin = source.getPlugin();
        this.state = source.getState();
        this.role = source.getRole();
    }

	public Long getId() {
		return id;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public PluginImpl getPlugin() {
		return plugin;
	}

	public void setPlugin(PluginImpl plugin) {
		this.plugin = plugin;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	@Override
	public String toString() {
		try {
	        return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(this);
	    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}
	
	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}

	public DNType getDnType() {
		return dnType;
	}

	public void setDnType(DNType dnType) {
		this.dnType = dnType;
	}

	public List<String> getDnList() {
		return dnList;
	}

	public void setDnList(List<String> dnList) {
		this.dnList = dnList;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public List<LdapEntry> getEntryList() {
		return entryList;
	}

	public void setEntryList(List<LdapEntry> entryList) {
		this.entryList = entryList;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public Boolean getIsMulti() {
		return isMulti;
	}

	public void setIsMulti(Boolean isMulti) {
		this.isMulti = isMulti;
	}
	
	public boolean isTaskParts() {
		return taskParts;
	}

	public void setTaskParts(boolean taskParts) {
		this.taskParts = taskParts;
	}

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
	
}
