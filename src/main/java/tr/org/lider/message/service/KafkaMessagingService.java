package tr.org.lider.message.service;

import javax.annotation.PostConstruct;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import tr.org.lider.kafka.MessageProducer;
import tr.org.lider.messaging.messages.ILiderMessage;

@Service
@ConditionalOnProperty(prefix = "lider", name = "messaging", havingValue = "kafka")
public class KafkaMessagingService implements IMessagingService {

	//private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private MessageProducer messageProducer;
	
	@PostConstruct
	@Override
	public void init() {
		System.err.println("");
	}
	
	@Override
	public void messageReceived(ILiderMessage message) {
		
	}

	@Override
	public void sendMessage(ILiderMessage message) {
		messageProducer.sendMessage(message);
	}

	@Override
	public void sendMessage(String message, String jid) throws NotConnectedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendChatMessage(String message, String jid) throws NotConnectedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRecipientOnline(String jid) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getFullJid(String jid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addClientToRoster(String jid) {
		// TODO Auto-generated method stub
		
	}



}
