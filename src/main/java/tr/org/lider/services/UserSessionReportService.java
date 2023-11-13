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
			if((startDate != null && startDate.isPresent()) && (endDate != null && endDate.isPresent())){
				if((username != null && !username.isEmpty()) && (dn != null && !dn.isEmpty())) {
					result = userSessionRepository.findByUserSessionLoginHostnameAndUsernameAndDate(username,dn,startDate,endDate,pageable);
				}
				else if(username != null && !username.isEmpty()) {
					result = userSessionRepository.findByUserSessionLoginUsernameAndDate(username,startDate,endDate, pageable);
				}
				else if(dn != null && !dn.isEmpty()) {
					result = userSessionRepository.findByUserSessionLoginHostnameAndDate(dn,startDate,endDate,pageable);
				}
				else {
					result = userSessionRepository.findByUserSessionLoginDate(startDate,endDate, pageable);
				}
			}
			else if((username != null && !username.isEmpty()) && (dn != null && !dn.isEmpty())) {
				result = userSessionRepository.findByUserSessionLoginUsernameAndHostname(username,dn,pageable);
			}
			else if(username != null && !username.isEmpty()){
				result = userSessionRepository.findByUserSessionLoginUsername(username,pageable);
			}
			else if(dn != null && !dn.isEmpty()){
				result = userSessionRepository.findByUserSessionLoginHostname(dn,pageable);
			}
			else {
				result = userSessionRepository.findByUserSessionLoginAll(pageable);
			}
			
		}
		
		else if (sessionType.equals("LOGOUT")) {
			if((startDate != null && startDate.isPresent()) && (endDate != null && endDate.isPresent())){
				if((username != null && !username.isEmpty()) && (dn != null && !dn.isEmpty())) {
					result = userSessionRepository.findByUserSessionLoginHostnameAndUsernameAndDate(username,dn,startDate,endDate,pageable);
				}
				else if(username != null && !username.isEmpty()) {
					result = userSessionRepository.findByUserSessionLogoutUsernameAndDate(username,startDate,endDate, pageable);
				}
				else if(dn != null && !dn.isEmpty()) {
					result = userSessionRepository.findByUserSessionLogoutHostnameAndDate(dn,startDate,endDate,pageable);
				}
				else {
					result = userSessionRepository.findByUserSessionLogoutDate(startDate,endDate, pageable);
				}
			}
			else if((username != null && !username.isEmpty()) && (dn != null && !dn.isEmpty())) {
				result = userSessionRepository.findByUserSessionLogoutUsernameAndHostname(username,dn,pageable);
			}
			else if(username != null && !username.isEmpty()){
				result = userSessionRepository.findByUserSessionLogoutUsername(username,pageable);
			}
			else if(dn != null && !dn.isEmpty()){
				result = userSessionRepository.findByUserSessionLogoutHostname(dn,pageable);
			}
			else {
				result = userSessionRepository.findByUserSessionLogoutAll(pageable);
			}
			
		}
		
		else {
		 
			if((startDate != null && startDate.isPresent()) && (endDate != null && endDate.isPresent())){
				if((username != null && !username.isEmpty()) && (dn != null && !dn.isEmpty())) {
					result = userSessionRepository.findByUserSessionAllHostnameAndUsernameAndDate(username,dn,startDate,endDate,pageable);
				}
				else if(username != null && !username.isEmpty()) {
					result = userSessionRepository.findByUserSessionAllUsernameAndDate(username,startDate,endDate, pageable);
				}
				else if(dn != null && !dn.isEmpty()) {
					result = userSessionRepository.findByUserSessionAllHostnameAndDate(dn,startDate,endDate,pageable);
				}
				else {
					result = userSessionRepository.findByUserSessionAllDate(startDate,endDate, pageable);
				}
			}
			else if((username != null && !username.isEmpty()) && (dn != null && !dn.isEmpty())) {
				result = userSessionRepository.findByUserSessionAllUsernameAndHostname(username,dn,pageable);
			}
			else if(username != null && !username.isEmpty()){
				result = userSessionRepository.findByUserSessionAllUsername(username,pageable);
			}
			else if(dn != null && !dn.isEmpty()){
				result = userSessionRepository.findByUserSessionAllHostname(dn,pageable);
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
