package tr.org.lider.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import tr.org.lider.entities.CommandImpl;
import tr.org.lider.repositories.ExecutedTaskCriteriaBuilder;
import tr.org.lider.security.User;
import tr.org.lider.constant.RoleConstants;

@Service
public class ExecutedTaskReportService {
	
	@Autowired
	ExecutedTaskCriteriaBuilder executedTaskCB;

	@Autowired
	UserService userService;
	
	public Page<CommandImpl> findAllCommandsFiltered(int pageNumber, int pageSize, Optional<String> taskCommand,
			Optional<Date> startDate, Optional<Date> endDate) {

		String username = AuthenticationService.getUserName();

		User userDetails = userService.loadUserByUsername(username);

		if (userDetails.getRoles().contains(RoleConstants.ROLE_ADMIN)) {
			username = null; // Admin can see all tasks
		}

		Page<CommandImpl> commands = executedTaskCB.filterCommands(
				pageNumber, pageSize, taskCommand, startDate, endDate, username);

		return commands;
	}
}
