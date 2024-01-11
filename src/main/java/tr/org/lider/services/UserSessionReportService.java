package tr.org.lider.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import tr.org.lider.entities.SessionEvent;
import tr.org.lider.repositories.UserSessionRepository;
import tr.org.lider.utils.IUserSessionReport;

@Service
public class UserSessionReportService {
	
	@Autowired
	private UserSessionRepository  userSessionRepository;

	public Long count() {
		return userSessionRepository.count();
	}
	
	public Page<IUserSessionReport> getUserSessionByFilter(int  pageNumber, int pageSize, String sessionType, String username,String hostname,Date startDate,Date endDate) {
		PageRequest pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("createDate").descending());
		Page<IUserSessionReport> results  = null;
		
		int sessionTypeId = 0;
		if (sessionType.equals("LOGIN")) {
			sessionTypeId = SessionEvent.LOGIN.getId();
		} else if (sessionType.equals("LOGOUT")) {
			sessionTypeId = SessionEvent.LOGOUT.getId();
		}
		results = userSessionRepository.findUserSession(sessionTypeId,username,hostname,startDate, endDate, pageable);
		return results;
	}

}
