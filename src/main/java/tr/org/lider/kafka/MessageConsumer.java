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

import tr.org.lider.messaging.messages.TaskStatusMessageImpl;
import tr.org.lider.messaging.subscribers.ITaskStatusSubscriber;
import tr.org.lider.repositories.AgentRepository;
import tr.org.lider.repositories.CommandExecutionRepository;
import tr.org.lider.repositories.CommandExecutionResultRepository;
import tr.org.lider.services.ConfigurationService;

@Service
@ConditionalOnProperty(prefix = "lider", name = "messaging", havingValue = "kafka")
public class MessageConsumer {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AgentRepository agentRepository;

	@Autowired
	private CommandExecutionRepository commanExecutionRepository;

	@Autowired
	private CommandExecutionResultRepository commandExecutionResultRepository;

	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private List<ITaskStatusSubscriber> taskStatusSubscribers;
	
    @KafkaListener(topics = "session", groupId = "consumer-group-1")
    public void consumeSession(ConsumerRecord<String, String> payload){
    	logger.info("Consumer group 1 session key: {}", payload.key());
    	logger.info("Consumer group 1 session Headers: {}", payload.headers());
    	logger.info("Consumer group 1 session Partion: {}", payload.partition());
    	logger.info("Consumer group 1 session Order: {}", payload.value());
    	
//    	WSMessage message = new WSMessage();
//    	message.setMessage("SESSION REQUEST RECEIVED");
//    	messagingTemplate.convertAndSend("/topic", message); 
    }
    
    @KafkaListener(topics = "registration-request", groupId = "consumer-group-1")
    public void consumeRegistration(ConsumerRecord<String, String> payload){
    	logger.info("Consumer group 1 topic: registration key: {}", payload.key());
    	logger.info("Consumer group 1 topic: registration Headers: {}", payload.headers());
    	logger.info("Consumer group 1 topic: registration Partion: {}", payload.partition());
    	logger.info("Consumer group 1 topic: registration Order: {}", payload.value());
    	
//    	WSMessage message = new WSMessage();
//    	message.setMessage("REGISTRATION REQUEST RECEIVED");
//    	messagingTemplate.convertAndSend("/topic", message); 
    }
    
    @KafkaListener(topics = "task-response", groupId = "consumer-group-1")
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
    
    @KafkaListener(topics = "policy-response", groupId = "consumer-group-1")
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
