package tr.org.lider.kafka;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tr.org.lider.messaging.messages.ILiderMessage;

@Service
@ConditionalOnProperty(prefix = "lider", name = "messaging", havingValue = "kafka")
public class MessageProducer {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public void sendMessage(ILiderMessage message){
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm"));

			String msgStr = mapper.writeValueAsString(message);
			String jid = message.getRecipient();
			
	        logger.info("Payload sent: {}",  msgStr);
	        kafkaTemplate.send(jid, msgStr);
		} catch (JsonProcessingException e) {
			logger.error("Error occured while converting object to JSON string. Error message: {}", e.getMessage());
		}
    }
    
}
