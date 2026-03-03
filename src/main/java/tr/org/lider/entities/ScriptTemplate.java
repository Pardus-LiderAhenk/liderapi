package tr.org.lider.entities;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class for script templates.
 * 
 */
@Entity
@Table(name = "P_SCRIPT_TEMPLATE")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScriptTemplate implements Serializable {

	private static final long serialVersionUID = 5867302652909954893L;

	@Id
	@GeneratedValue
	@Column(name = "ID", unique = true, nullable = false)
	private Long id;

	@Column(name = "SCRIPT_TYPE", length = 1, nullable = false)
	private Integer scriptType;

	@Column(name = "LABEL", nullable = false, length = 255)
	private String label;

	@Lob
	@Column(name = "CONTENTS", nullable = false)
	private String contents;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_DATE", nullable = false, updatable = false)
	@CreationTimestamp
	@JsonFormat(pattern="dd/MM/yyyy HH:mm:ss", timezone="Europe/Istanbul")
	private Date createDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "MODIFY_DATE")
	@JsonFormat(pattern="dd/MM/yyyy HH:mm:ss", timezone="Europe/Istanbul")
	private Date modifyDate;
	
	@Column(name = "DELETED")
	private boolean deleted;

	@Column(name = "CREATED_BY", length = 255)
	private String createdBy;

	@Column(name = "IS_PUBLISHED")
	private Boolean isPublished;

	public ScriptTemplate(ScriptType scriptType, String label, String contents, Date createDate, Date modifyDate, Boolean deleted, String createdBy, Boolean isPublished) {
		setScriptType(scriptType);
		this.label = label;
		this.contents = contents;
		this.createDate = createDate;
		this.modifyDate = modifyDate;
		this.deleted = deleted;
		this.createdBy = createdBy;
		this.isPublished = isPublished;
	}

	public ScriptType getScriptType() {
		return ScriptType.getType(scriptType);
	}

	public void setScriptType(ScriptType scriptType) {
		if (scriptType == null) {
			this.scriptType = null;
		} else {
			this.scriptType = scriptType.getId();
		}
	}
}