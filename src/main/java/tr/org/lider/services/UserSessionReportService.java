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
				if(dn != null && dn.isEmpty()){
					result = userSessionRepository.findByUserSessionUsernameAndDn(username,dn,startDate,endDate, pageable);
				}
				else {
					result = userSessionRepository.findByUserSessionAndCreateDateGreaterThanAndCreateDateLessThan(username,startDate,endDate, pageable);
				}
				
			}
			
			result = userSessionRepository.findByUserSessionLoginAll(pageable);
		
		}
		else if(sessionType.equals("LOGOUT")) {
			
			result = userSessionRepository.findByUserSessionLogoutAll(pageable);
		}
		else {
		  if (startDate.isPresent() && endDate.isPresent()) {
				result = userSessionRepository.findByUserSessionIdAndCreateDateGreaterThanAndCreateDateLessThan(startDate, endDate, pageable);
		  } 
		  else if(username != null && !username.isEmpty()) {
			result = userSessionRepository.findByUserSession(username, pageable);
				}
		  else if(dn != null && !dn.isEmpty()) {
			  result = userSessionRepository.findByUserSessionByDn(dn, pageable);
		  }else {
			  result = userSessionRepository.findByUserSessionAll(pageable);
		  }
		}
				
		
		
		return result;
	}
	
	public Page<Map<String, Object>> getLastActivityByUserIdDescLimitTen(String username) {
		PageRequest pageable = PageRequest.of(1 - 1, 10, Sort.by("createDate").descending());
		return userSessionRepository.findOrderByCreateDateDesc10ByUserId(username, pageable);
		
	}
	

}
