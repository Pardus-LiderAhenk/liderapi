package tr.org.lider;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class LiderCronJob {
	
	@Scheduled(cron = "0 6 17 * * ?")
    public void dailyCronJob() {
        System.out.println("Her akşam çalışan cron job çalıştı!");
    }

}
