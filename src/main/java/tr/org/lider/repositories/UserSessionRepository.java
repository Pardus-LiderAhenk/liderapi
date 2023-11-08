package tr.org.lider.repositories;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;

import tr.org.lider.entities.UserSessionImpl;

public interface UserSessionRepository extends BaseJpaRepository<UserSessionImpl, Long> {

	@Query(value = "SELECT  NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent=a.id WHERE s.username LIKE %?1% AND a.hostname LIKE %?2% AND s.sessionEvent = 1 AND s.createDate BETWEEN ?3 AND ?4")
	Page<Map<String, Object>> findByUserSessionLoginUsernameAndDnAndDate(String username,String dn, Optional<Date> startDate, Optional<Date> endDate,Pageable pageable);
	
	@Query(value = "SELECT  NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent=a.id WHERE s.username LIKE %?1% AND a.hostname LIKE %?2% AND s.sessionEvent = 1")
	Page<Map<String, Object>> findByUserSessionLoginUsernameAndDn(String username,String dn,Pageable pageable);
	
	@Query(value = "SELECT  NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent=a.id WHERE s.username LIKE %?1% AND s.sessionEvent = 1 AND s.createDate BETWEEN ?2 AND ?3")
	Page<Map<String, Object>> findByUserSessionLoginUsernameAndCreateDateGreaterThan(String username, Optional<Date> startDate, Optional<Date> endDate,Pageable pageable);
	
	@Query(value = "SELECT  NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent=a.id WHERE s.sessionEvent = 1")
	Page<Map<String, Object>> findByUserSessionLoginAll(Pageable pageable);
	
	@Query(value = "SELECT  NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent=a.id WHERE s.username LIKE %?1% AND a.hostname LIKE %?2% AND s.sessionEvent = 2 AND s.createDate BETWEEN ?3 AND ?4")
	Page<Map<String, Object>> findByUserSessionLogoutUsernameAndDn(String username,String dn, Optional<Date> startDate, Optional<Date> endDate,Pageable pageable);
	
	@Query(value = "SELECT  NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent=a.id WHERE s.username LIKE %?1% AND s.sessionEvent = 2 AND s.createDate BETWEEN ?2 AND ?3")
	Page<Map<String, Object>> findByUserSessionLogoutUsernameAndCreateDateGreaterThan(String username, Optional<Date> startDate, Optional<Date> endDate,Pageable pageable);
	
	@Query(value = "SELECT  NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent=a.id WHERE s.username LIKE %?1% AND a.hostname LIKE %?2% AND s.sessionEvent = 2")
	Page<Map<String, Object>> findByUserSessionLogoutDnAndCreateDateGreaterThan(String username,String dn,Pageable pageable);
	
	@Query(value = "SELECT  NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent=a.id WHERE s.username LIKE %?1% AND s.sessionEvent = 2 ")
	Page<Map<String, Object>> findByUserSessionLogoutUsername(String username,Pageable pageable);
	
	@Query(value = "SELECT  NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent=a.id WHERE s.sessionEvent = 2")
	Page<Map<String, Object>> findByUserSessionLogoutAll(Pageable pageable);
	
	@Query(value = "SELECT NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent = a.id WHERE s.username LIKE %?1% AND s.createDate BETWEEN ?2 AND ?3")
	Page<Map<String, Object>> findByUserSessionAndDate(String username,Optional<Date> startDate,Optional<Date> endDate,  Pageable pageable);
	
	@Query(value = "SELECT NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent = a.id WHERE s.username LIKE %?1% AND a.hostname LIKE %?2% AND s.createDate BETWEEN ?3 AND ?4")
	Page<Map<String, Object>> findByUserSessionUserAndDnAndDate(String username,String dn,Optional<Date> startDate,Optional<Date> endDate, Pageable pageable);

	@Query(value = "SELECT NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent = a.id WHERE  a.hostname LIKE %?1%")
	Page<Map<String, Object>> findByUserSessionUserAndDn(String dn, Pageable pageable);

	@Query(value = "SELECT  NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent=a.id WHERE s.createDate BETWEEN ?1 AND ?2 ")
	Page<Map<String, Object>> findByUserSessionIdAndCreateDateGreaterThanAndCreateDateLessThan(Optional<Date> startDate, Optional<Date> endDate, Pageable pageable);

	@Query(value = "SELECT NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN  AgentImpl a ON s.agent=a.id WHERE s.username LIKE %?1%")
	Page<Map<String, Object>> findOrderByCreateDateDesc10ByUserId(String username, Pageable pageable);
	
	@Query(value = "SELECT NEW map(s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN  AgentImpl a ON s.agent=a.id")
	Page<Map<String, Object>> findByUserSessionAll(Pageable pageable);
	

}
