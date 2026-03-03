package tr.org.lider.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import tr.org.lider.dto.ScheduledTaskDTO;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.repositories.ScheduledTaskCriteriaBuilder;
import tr.org.lider.security.User;
import tr.org.lider.constant.RoleConstants;

@Service
public class ScheduledTaskReportService {
	
	@Autowired
	ScheduledTaskCriteriaBuilder scheduledTaskCB;

	@Autowired
	UserService userService;
	
	public Page<CommandImpl> findAllCommandsFiltered(ScheduledTaskDTO scheduledTaskDTO) {

		String username = AuthenticationService.getUserName();

		User userDetails = userService.loadUserByUsername(username);

		if (userDetails.getRoles().contains(RoleConstants.ROLE_ADMIN)) {
			username = null; // Admin can see all tasks
		}
		else {
			scheduledTaskDTO.setUsername(username);
		}

		Page<CommandImpl> commands = scheduledTaskCB.filterCommands(scheduledTaskDTO);
		return commands;
	}
}
