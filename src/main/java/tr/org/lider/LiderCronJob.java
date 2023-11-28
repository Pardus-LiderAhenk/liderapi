package tr.org.lider;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import tr.org.lider.entities.AgentImpl;
import tr.org.lider.entities.AgentStatus;
import tr.org.lider.repositories.AgentRepository;

@Component
@EnableScheduling
public class LiderCronJob {
	
	@Autowired
	private  AgentRepository agentRepository;
	
	private XMPPTCPConnection connection;

	
	@Scheduled(cron = "0 19 23 * * ?")
    public void dailyCronJob() {
		
		Date today = new Date();

		List<AgentImpl> agentsEventDate =  agentRepository.findAll();
		//Roster roster = Roster.getInstanceFor(connection);
		
		for(AgentImpl agent : agentsEventDate) {
			//Presence presence = roster.getPresence(agent.getJid());
			//System.out.println(presence);

				Date eventDate = agent.getEventDate();
				if (eventDate != null) {
					LocalDate todayLocalDate = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					LocalDate dbEventDate = eventDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					
					long daysDifference = ChronoUnit.DAYS.between(todayLocalDate, dbEventDate);
					System.out.println(daysDifference);

					if (daysDifference > -120) {
					
						agent.setAgentStatus(AgentStatus.Active);
						agentRepository.save(agent);
						System.out.println("Event date for agent is greater than 120 days: " + agent.getId());
					} 
					else {
						agent.setAgentStatus(AgentStatus.Passive);
						agentRepository.save(agent);
						System.out.println("Event date for agent is less than 120 days: " + agent.getId());
					}
				} 
				else {
					System.out.println("Event date is null for agent: " + agent.getId());
				}
			}
		
			
		
		
        System.out.println("Her akşam çalışan cron job çalıştı!");
    }

}
