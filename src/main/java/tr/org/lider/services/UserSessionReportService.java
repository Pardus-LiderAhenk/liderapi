package tr.org.lider.services;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tr.org.lider.entities.UserSessionImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import tr.org.lider.repositories.UserSessionCriteriaBuilder;
import tr.org.lider.repositories.UserSessionRepository;

@Service
public class UserSessionReportService {
	
	@Autowired
	private UserSessionRepository  userSessionRepository;

	
	public Long count() {
		return userSessionRepository.count();
		
		
	}
	
	public Page<Map<String, Object>> getUserSessionByFilter(int pageNumber, int pageSize, String sessionType, String username, String dn, Optional<Date> startDate, Optional<Date> endDate) {
		PageRequest pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("createDate").descending());
		Page<Map<String, Object>> result = null;

		if (sessionType.equals("LOGIN")) {
			if(username != null && !username.isEmpty()) {
				result = userSessionRepository.findByUserSessionAndCreateDateGreaterThanAndCreateDateLessThan(username,startDate,endDate, pageable);
			}
			
		
		}
		else if(sessionType.equals("LOGOUT")) {
			
		}
		else {
		  if (startDate.isPresent() && endDate.isPresent()) {
				result = userSessionRepository.findByUserSessionIdAndCreateDateGreaterThanAndCreateDateLessThan(startDate, endDate, pageable);
		  } 
		  else {
			result = userSessionRepository.findByUserSession(username, pageable);
				}
		}
				
		
		
		return result;
	}
	
	public Page<UserSessionImpl> getLastActivityByUserIdDescLimitTen(String username) {
		PageRequest pageable = PageRequest.of(1 - 1, 10, Sort.by("createDate").descending());
		//return userSessionRepository.findOrderByCreateDateDesc10ByUserId(username, pageable);
		return null;
	}
	

}
