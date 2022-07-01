package tr.org.lider.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tr.org.lider.services.ScheduledTaskService;


/**
 *  Scheduled task as cancel or updated
 */

@Secured({"ROLE_ADMIN", "ROLE_COMPUTERS" })
@RestController()
@RequestMapping("/lider/scheduledTask")
public class UpdateScheduledTask {
	Logger logger = LoggerFactory.getLogger(TaskController.class);
	
	@Autowired
	public ScheduledTaskService scheduledTaskService;
	
	@RequestMapping(value = "/update", method = { RequestMethod.POST })
	public ResponseEntity<?> updateScheduledTask(@RequestParam (value = "id", required=false) Long id,
			@RequestParam (value = "cronExpression", required=false) String cronExpression) {
		
		logger.info("Request received. URL: '/lider/scheduledTask/update'");
		ResponseEntity<?> response = scheduledTaskService.updateScheduledTask(id, cronExpression);
		return response;
	}
	
	
	@RequestMapping(value = "/cancel", method = { RequestMethod.POST })
	public ResponseEntity<?> cancelScheduledTask(@RequestParam (value = "id", required=false) Long id) {
		
		logger.info("Request received. URL: '/lider/scheduledTask/update'");
		ResponseEntity<?> response = scheduledTaskService.cancelScheduledTask(id);
		return response;
	}
	
	
	
}
