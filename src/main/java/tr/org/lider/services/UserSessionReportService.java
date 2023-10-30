package tr.org.lider.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tr.org.lider.entities.UserSessionImpl;
import org.springframework.data.domain.Page;

import tr.org.lider.repositories.UserSessionCriteriaBuilder;

@Service
public class UserSessionReportService {
	
	@Autowired
	UserSessionCriteriaBuilder userSessionCB;
	
	public Page<UserSessionImpl> findAllUserFiltered(
			int pageNumber, 
			int pageSize,
			String sessionType, 
			Optional<String> username,
			Optional<String> clientName,
			Optional<Date> startDate, 
			Optional<Date> endDate){
		
		
		Page<UserSessionImpl> users = userSessionCB.filterUserSession(
				pageNumber, pageSize,sessionType, username,clientName, startDate, endDate);
		
		return users;
		
	}
	

}
