/*
*
*    Copyright © 2015-2016 Tübitak ULAKBIM
*
*    This file is part of Lider Ahenk.
*
*    Lider Ahenk is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    Lider Ahenk is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with Lider Ahenk.  If not, see <http://www.gnu.org/licenses/>.
*/
package tr.org.lider.messaging.messages;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tr.org.lider.messaging.enums.AgentMessageType;
import tr.org.lider.messaging.enums.ContentType;
import tr.org.lider.messaging.enums.StatusCode;

/**
 * Default implementation for {@link IPolicyStatusMessage}. This message is sent
 * <b>from agent to Lider</b> in order to notify Lider about policy
 * result/status.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyStatusMessageImpl implements IPolicyStatusMessage {

	private static final long serialVersionUID = 8345802637309377501L;

	private AgentMessageType type;

	private String policyVersion;

	private Long commandExecutionId;

	private StatusCode responseCode;

	private String responseMessage;

	private Map<String, Object> responseData;

	private ContentType contentType;

	private String from;

	private Date timestamp;

	@Override
	public AgentMessageType getType() {
		return type;
	}

	public void setType(AgentMessageType type) {
		this.type = type;
	}

	@Override
	public String getPolicyVersion() {
		return policyVersion;
	}

	public void setPolicyVersion(String policyVersion) {
		this.policyVersion = policyVersion;
	}

	@Override
	public Long getCommandExecutionId() {
		return commandExecutionId;
	}

	public void setCommandExecutionId(Long commandExecutionId) {
		this.commandExecutionId = commandExecutionId;
	}

	@Override
	public StatusCode getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(StatusCode responseCode) {
		this.responseCode = responseCode;
	}

	@Override
	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	@Override
	public Map<String, Object> getResponseData() {
		return responseData;
	}

	public void setResponseData(Map<String, Object> responseData) {
		this.responseData = responseData;
	}

	@Override
	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	@Override
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

}
