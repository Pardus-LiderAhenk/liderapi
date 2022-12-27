package tr.org.lider.kafka;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tr.org.lider.message.service.IMessagingService;
import tr.org.lider.messaging.messages.GetPoliciesMessageImpl;
import tr.org.lider.messaging.messages.IExecutePoliciesMessage;
import tr.org.lider.messaging.messages.ILiderMessage;
import tr.org.lider.messaging.messages.TaskStatusMessageImpl;
import tr.org.lider.messaging.messages.UserSessionMessageImpl;
import tr.org.lider.messaging.subscribers.IPolicySubscriber;
import tr.org.lider.messaging.subscribers.ITaskStatusSubscriber;
import tr.org.lider.messaging.subscribers.IUserSessionSubscriber;

@Service
@ConditionalOnProperty(prefix = "lider", name = "messaging", havingValue = "kafka")
public class MessageConsumer {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private List<ITaskStatusSubscriber> taskStatusSubscribers;
	
	@Autowired
	private IUserSessionSubscriber userSessionSubscriber;
	
	@Autowired
	private List<IPolicySubscriber> subscribers;
	
	@Autowired
	private IMessagingService messagingService;
	
    @KafkaListener(topics = "LOGIN")
    public void consumeSession(ConsumerRecord<String, String> payload){
		ObjectMapper mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm"));

		// Construct message
		UserSessionMessageImpl message;
		try {
			message = mapper.readValue(payload.value(), UserSessionMessageImpl.class);
			message.setFrom(payload.key());

			if (userSessionSubscriber != null) {
				ILiderMessage  responseMessage = userSessionSubscriber.messageReceived(message);
				
//				if (responseMessage != null) {
//					client.sendMessage(new ObjectMapper().writeValueAsString(responseMessage), msg.getFrom());
//				}
//				
//				logger.debug("Notified subscriber => {}", subscriber);
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    @KafkaListener(topics = "LOGOUT")
    public void consumeSessionLogout(ConsumerRecord<String, String> payload){
		ObjectMapper mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm"));

		// Construct message
		UserSessionMessageImpl message;
		try {
			message = mapper.readValue(payload.value(), UserSessionMessageImpl.class);
			message.setFrom(payload.key());

			if (userSessionSubscriber != null) {
				ILiderMessage  responseMessage = userSessionSubscriber.messageReceived(message);
				
//				if (responseMessage != null) {
//					client.sendMessage(new ObjectMapper().writeValueAsString(responseMessage), msg.getFrom());
//				}
//				
//				logger.debug("Notified subscriber => {}", subscriber);
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    @KafkaListener(topics = "REGISTER")
    public void consumeRegisterRequest(ConsumerRecord<String, String> payload){
    	logger.info("Consumer group 1 topic: registration key: {}", payload.key());
    	logger.info("Consumer group 1 topic: registration Headers: {}", payload.headers());
    	logger.info("Consumer group 1 topic: registration Partion: {}", payload.partition());
    	logger.info("Consumer group 1 topic: registration Order: {}", payload.value());
//    	WSMessage message = new WSMessage();
//    	message.setMessage("REGISTRATION REQUEST RECEIVED");
//    	messagingTemplate.convertAndSend("/topic", message); 
    }
    
    @KafkaListener(topics = "UNREGISTER")
    public void consumeUnregisterRequest(ConsumerRecord<String, String> payload){
    	logger.info("Consumer group 1 topic: registration key: {}", payload.key());
    	logger.info("Consumer group 1 topic: registration Headers: {}", payload.headers());
    	logger.info("Consumer group 1 topic: registration Partion: {}", payload.partition());
    	logger.info("Consumer group 1 topic: registration Order: {}", payload.value());
//    	WSMessage message = new WSMessage();
//    	message.setMessage("REGISTRATION REQUEST RECEIVED");
//    	messagingTemplate.convertAndSend("/topic", message); 
    }
    
    @KafkaListener(topics = "TASK_STATUS")
    public void consumeTaskResponse(ConsumerRecord<String, String> responsePayload){
    	logger.info("Consumer group 1 topic: task key: {}", responsePayload.key());
    	logger.info("Consumer group 1 topic: task Headers: {}", responsePayload.headers());
    	logger.info("Consumer group 1 topic: task Partion: {}", responsePayload.partition());
    	logger.info("Consumer group 1 topic: task Order: {}", responsePayload.value());
    	
		ObjectMapper mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm"));

		TaskStatusMessageImpl message = null;
		try {
			message = mapper.readValue(responsePayload.value(), TaskStatusMessageImpl.class);
			message.setFrom(responsePayload.key());
			
	    	for (ITaskStatusSubscriber subscriber : taskStatusSubscribers) {
				try {
					subscriber.messageReceived(message);
				} catch (Exception e) {
					logger.error("Subscriber could not handle message: ", e);
				}
				logger.debug("Notified subscriber => {}", subscriber);
			}
	    	
		} catch (JsonMappingException e) {
			logger.error("Error occured while converting json string to TaskStatusMessageImpl. Error: " + e.getMessage());
		} catch (JsonProcessingException e) {
			logger.error("Error occured while converting json string to TaskStatusMessageImpl. Error: " + e.getMessage());
		}
		
//    	WSMessage message = new WSMessage();
//    	message.setMessage("TASK RESPONSE RECEIVED");
//    	messagingTemplate.convertAndSend("/topic", message); 
    }
    
    @KafkaListener(topics = "GET_POLICIES")
    public void consumePolicyRequest(ConsumerRecord<String, String> payload){
    	logger.info("Consumer group 1 topic: task key: {}", payload.key());
    	logger.info("Consumer group 1 topic: task Headers: {}", payload.headers());
    	logger.info("Consumer group 1 topic: task Partion: {}", payload.partition());
    	logger.info("Consumer group 1 topic: task Order: {}", payload.value());
    	
    	ObjectMapper mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm"));

		GetPoliciesMessageImpl message = null;
		try {
			message = mapper.readValue(payload.value(), GetPoliciesMessageImpl.class);
			message.setFrom(payload.key());
			
			IExecutePoliciesMessage executePoliciesMessageImpl = null;
			for (IPolicySubscriber subscriber : subscribers) {
				try {
					executePoliciesMessageImpl = subscriber.messageReceived(message);
					//if user has policy send policy message
//			    	if(executePoliciesMessageImpl != null) {
//			    		
//			    		messagingService.sendMessage(new ObjectMapper().writeValueAsString(executePoliciesMessageImpl));
//			    	}
				} catch (Exception e) {
					logger.error("Subscriber could not handle message: ", e);
				}
				logger.debug("Notified subscriber => {}", subscriber);
			}
			

	    	
		} catch (JsonMappingException e) {
			logger.error("Error occured while converting json string to TaskStatusMessageImpl. Error: " + e.getMessage());
		} catch (JsonProcessingException e) {
			logger.error("Error occured while converting json string to TaskStatusMessageImpl. Error: " + e.getMessage());
		}
    	
//    	Map<String, String[]> agentPolicyList = message.getPolicyList();
//    	String userUid = payload.key();
//    	
//    	List<LdapEntry> groupsOfUser = findGroups(userDn);
//
//		// Find policy for user groups.
//		// (User policy can be related to either user entry or group entries
//		// which ever is the latest)
//		List<Object[]> resultList=new ArrayList<>();
//		for (LdapEntry ldapEntry : groupsOfUser) {
//			List<Object[]> result = policyDao.findPoliciesByGroupDn(ldapEntry.getDistinguishedName());
//			resultList.addAll(result);
//		}
//		PolicyImpl userPolicy = null;
//		Long userCommandExecutionId = null;
//		CommandImpl command = null;
//		ExecutePoliciesMessageImpl response = new ExecutePoliciesMessageImpl();
//		response.setUsername(userUid);
//		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//		if(resultList != null && !resultList.isEmpty()) {
//			for (int i = 0; i < resultList.size(); i++) {
//				userPolicy = (PolicyImpl) resultList.get(i)[0];
//				userCommandExecutionId = ((CommandExecutionImpl) resultList.get(i)[1]).getId();
//				command = ((CommandImpl) resultList.get(i)[2]);
//				
//				boolean sendUserPolicy = userPolicy != null 
//						&& userPolicy.getPolicyVersion() != null;
//				if(agentPolicyList != null && agentPolicyList.size() > 0) {
//					
//					if(agentPolicyList.containsKey(String.valueOf(userPolicy.getId()))) {
//						Date assignDateOfAgentDB = formatter.parse(agentPolicyList.get(String.valueOf(userPolicy.getId()))[1]);
//						LocalDateTime ldtAgentDBDate = LocalDateTime.ofInstant(assignDateOfAgentDB.toInstant(), ZoneId.systemDefault());
//						LocalDateTime ldtAssignDate = LocalDateTime.ofInstant(command.getCreateDate().toInstant(), ZoneId.systemDefault());
//						ldtAgentDBDate = ldtAgentDBDate.withNano(0);
//						ldtAssignDate = ldtAssignDate.withNano(0);
//					
//						if(agentPolicyList.get(String.valueOf(userPolicy.getId()))[0].equals(userPolicy.getPolicyVersion())
//								&& ldtAgentDBDate.isEqual(ldtAssignDate)) {
//							sendUserPolicy = false;
//						}
//					}
//				}
// 
//				// Check if one of the plugins use file transfer
//				boolean usesFileTransfer = false;
//				if (sendUserPolicy) {
//					for (ProfileImpl profile : userPolicy.getProfiles()) {
//						if (profile.getPlugin() != null && profile.getPlugin().isUsesFileTransfer()) {
//							usesFileTransfer = true;
//							break;
//						}
//					}
//				}
//				
//				if(sendUserPolicy) {
//					ExecutePolicyImpl policy = new ExecutePolicyImpl();
//					policy.setPolicyID(userPolicy.getId());
//					policy.setAgentCommandExecutionId(null);
//					policy.setAgentPolicyExpirationDate(null);
//					policy.setAgentPolicyProfiles(null);
//					policy.setAgentPolicyVersion(null);
//					policy.setFileServerConf(usesFileTransfer ? configurationService.getFileServerConf(agentUid) : null);
//					policy.setUserCommandExecutionId(userCommandExecutionId);
//					policy.setUsername(userUid);
//					policy.setUserPolicyExpirationDate(sendUserPolicy ? command.getExpirationDate() : null);
//					policy.setUserPolicyProfiles(sendUserPolicy ? new ArrayList<ProfileImpl>(userPolicy.getProfiles()) : null);
//					policy.setUserPolicyVersion(userPolicy != null ? userPolicy.getPolicyVersion() : null);
//					policy.setIsDeleted(false);
//					LocalDateTime ldtAssignDate = LocalDateTime.ofInstant(command.getCreateDate().toInstant(), ZoneId.systemDefault());
//					ldtAssignDate = ldtAssignDate.withNano(0);
//					policy.setAssignDate(Date.from(ldtAssignDate.atZone(ZoneId.systemDefault()).toInstant()));
//					response.getExecutePolicyList().add(policy);
//				}
//			}
//		}
//		// check if one or more policies in database of agent is deleted or unassigned on lider server
//		// if deleted send deleted flag to ahenk to delete policy in ahenk db
//		if(agentPolicyList != null && agentPolicyList.size() > 0) {
//			for (String policyID : agentPolicyList.keySet())  
//	        { 
//	            // search  for value 
//	            boolean isPolicyDeleted = true;
//	    		if(resultList != null && !resultList.isEmpty()) {
//	    			for (int i = 0; i < resultList.size(); i++) {
//	    				userPolicy = (PolicyImpl) resultList.get(i)[0];
//	    				if(String.valueOf(userPolicy.getId()).equals(policyID)) {
//	    					isPolicyDeleted = false;
//	    					break;
//	    				}
//	    			}
//	    		}
//				
//	    		if(isPolicyDeleted) {
//	    			ExecutePolicyImpl policy = new ExecutePolicyImpl();
//					policy.setPolicyID(Long.valueOf(policyID));
//					policy.setAgentCommandExecutionId(null);
//					policy.setAgentPolicyExpirationDate(null);
//					policy.setAgentPolicyProfiles(null);
//					policy.setAgentPolicyVersion(null);
//					policy.setFileServerConf(null);
//					policy.setUserCommandExecutionId(null);
//					policy.setUsername(userUid);
//					policy.setUserPolicyExpirationDate(null);
//					policy.setUserPolicyProfiles(null);
//					policy.setUserPolicyVersion(null);
//					policy.setIsDeleted(true);
//					response.getExecutePolicyList().add(policy);
//	    		}
//	        } 
//		}
    	
    	
//    	WSMessage message = new WSMessage();
//    	message.setMessage("POLICY REQUEST RECEIVED");
//    	messagingTemplate.convertAndSend("/topic", message); 
    }
    
    @KafkaListener(topics = "policy-response")
    public void consumePolicyResponse(ConsumerRecord<String, String> payload){
    	logger.info("Consumer group 1 topic: task key: {}", payload.key());
    	logger.info("Consumer group 1 topic: task Headers: {}", payload.headers());
    	logger.info("Consumer group 1 topic: task Partion: {}", payload.partition());
    	logger.info("Consumer group 1 topic: task Order: {}", payload.value());
    	
//    	WSMessage message = new WSMessage();
//    	message.setMessage("POLICY REQUEST RECEIVED");
//    	messagingTemplate.convertAndSend("/topic", message); 
    }
}
