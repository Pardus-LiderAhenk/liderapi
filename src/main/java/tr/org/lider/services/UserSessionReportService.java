package tr.org.lider.services;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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
					if(startDate != null && startDate.isPresent()) {
						result = userSessionRepository.findByUserSessionLoginUsernameAndDnAndDate(username,dn,startDate,endDate, pageable);
					}
					result = userSessionRepository.findByUserSessionLoginUsernameAndDn(username,dn,pageable);
				}
				else {
					result = userSessionRepository.findByUserSessionLoginUsernameAndCreateDateGreaterThan(username,startDate,endDate, pageable);
				}
			}
			
			result = userSessionRepository.findByUserSessionLoginAll(pageable);
		}
		else if(sessionType.equals("LOGOUT")) {
			if(username != null && !username.isEmpty()) {
				if(dn != null && dn.isEmpty()){
					if(startDate != null && startDate.isPresent()) {
						result = userSessionRepository.findByUserSessionLogoutUsernameAndDn(username,dn,startDate,endDate, pageable);
					}
					result = userSessionRepository.findByUserSessionLogoutDnAndCreateDateGreaterThan(username,dn,pageable);
				}
				else {
					result = userSessionRepository.findByUserSessionLogoutUsername(username,pageable);
					
				}				
			}
			
			result = userSessionRepository.findByUserSessionLogoutAll(pageable);
		}
		else {
		  if (startDate.isPresent() && endDate.isPresent()) {
			  if(username != null && !username.isEmpty()) {
				  
				  if(dn != null && !dn.isEmpty()) {
					  
					  result = userSessionRepository.findByUserSessionUserAndDnAndDate(username,dn,startDate,endDate, pageable);
				  }
				  else {
						result = userSessionRepository.findByUserSessionAndDate(username,startDate,endDate, pageable);

				  }
			  }
			  else {
				  result = userSessionRepository.findByUserSessionIdAndCreateDateGreaterThanAndCreateDateLessThan(startDate, endDate, pageable);
			  }
			  
		  } 
		 
		  else if(dn != null && !dn.isEmpty()) {
			  result = userSessionRepository.findByUserSessionUserAndDn(dn, pageable);
		  }
		  else {
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
