package tr.org.lider.kafka;


import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerMethods {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	
	@Scheduled(cron = "0/3 * * * * *", zone = "Europe/Istanbul")
	public void sendMessage() {
		//messagingTemplate.convertAndSend("/topic/lider-response", new Greeting("Hello, " + (new Date()) + "!")); 
	}
}