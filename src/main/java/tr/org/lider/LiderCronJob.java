package tr.org.lider;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import tr.org.lider.entities.AgentImpl;
import tr.org.lider.entities.AgentStatus;
import tr.org.lider.messaging.messages.XMPPClientImpl;
import tr.org.lider.repositories.AgentRepository;
import tr.org.lider.services.ConfigurationService;

@Component
@EnableScheduling
public class LiderCronJob {
	
	@Autowired
	private  AgentRepository agentRepository;
	
	@Autowired
	private XMPPClientImpl xmppClientImpl;
	
	@Autowired
	private ConfigurationService configurationService;

	
	@Scheduled(cron = "0 12 17 * * ?")
    public void dailyCronJob() {
		if(configurationService.getMachineEventStatus() == true) {
			Date today = new Date();
			List<AgentImpl> agentsEventDate =  agentRepository.findAll();
		
			for(AgentImpl agent : agentsEventDate) {
			
				if(!(xmppClientImpl.isRecipientOnline(agent.getJid()))) {

				Date eventDate = agent.getEventDate();
				if (eventDate != null) {
					LocalDate todayLocalDate = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					LocalDate dbEventDate = eventDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					
					long daysDifference = ChronoUnit.DAYS.between(todayLocalDate, dbEventDate);
					System.out.println(daysDifference);

					if (daysDifference > configurationService.getMachineEventDay()) {
					
						agent.setAgentStatus(AgentStatus.Active);
						agentRepository.save(agent);
					} 
					else {
						agent.setAgentStatus(AgentStatus.Passive);
						agentRepository.save(agent);
					}
				} 
				else {
					System.out.println("Event date is null for agent: " + agent.getId());
				}
				}
			}
		
        System.out.println("Her akşam çalışan cron job çalıştı!");
		}
    }
	

}
