package tr.org.lider.messaging.messages;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tr.org.lider.messaging.enums.LiderMessageType;

@JsonIgnoreProperties(ignoreUnknown = true, value = { "recipient" })
public class ExecuteScheduledTaskMessageImpl implements IExecuteTaskMessage{
	
	private static final long serialVersionUID = -8169781079859133876L;

	private LiderMessageType type = LiderMessageType.EXECUTE_TASK;

	private String task;

	private String recipient;

	private Date timestamp;

	private FileServerConf fileServerConf;

	public ExecuteScheduledTaskMessageImpl(String task, String recipient, Date timestamp, FileServerConf fileServerConf) {
		this.task = task;
		this.recipient = recipient;
		this.timestamp = timestamp;
		this.fileServerConf = fileServerConf;
	}

	@Override
	public LiderMessageType getType() {
		return type;
	}

	public void setType(LiderMessageType type) {
		this.type = type;
	}

	@Override
	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	@Override
	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public FileServerConf getFileServerConf() {
		return fileServerConf;
	}

	public void setFileServerConf(FileServerConf fileServerConf) {
		this.fileServerConf = fileServerConf;
	}

}
