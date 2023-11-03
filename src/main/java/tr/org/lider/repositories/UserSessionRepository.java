package tr.org.lider.repositories;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;

import tr.org.lider.entities.UserSessionImpl;

public interface UserSessionRepository extends BaseJpaRepository<UserSessionImpl, Long> {

	
	@Query(value = "SELECT  NEW map(s.id as usr_id, s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s JOIN AgentImpl a ON s.agent=a.id WHERE s.username LIKE %?1% AND s.sessionEvent = 1 AND s.createDate BETWEEN ?2 AND ?3")
	Page<Map<String, Object>> findByUserSessionAndCreateDateGreaterThanAndCreateDateLessThan(String username, Optional<Date> startDate, Optional<Date> endDate,Pageable pageable);


	@Query(value = "SELECT NEW map(s.id as usr_id, s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s LEFT JOIN AgentImpl a ON s.agent = a.id WHERE s.username LIKE %?1%")
	Page<Map<String, Object>> findByUserSession(String username, Pageable pageable);

	
	@Query(value = "SELECT  NEW map(s.id as usr_id, s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s  JOIN AgentImpl a ON s.agent=a.id WHERE s.createDate BETWEEN ?1 AND ?2 ")
	Page<Map<String, Object>> findByUserSessionIdAndCreateDateGreaterThanAndCreateDateLessThan(Optional<Date> startDate, Optional<Date> endDate, Pageable pageable);

	@Query(value = "SELECT NEW map(s.id as usr_id, s.username as username, a.hostname as hostname, s.sessionEvent as sessionEvent, a.ipAddresses as ipAddresses, a.macAddresses as macAddresses, s.createDate as createDate) FROM UserSessionImpl s JOIN  AgentImpl a ON s.agent=a.id WHERE s.username LIKE %?1%")
	Page<Map<String, Object>> findOrderByCreateDateDesc10ByUserId(String username, Pageable pageable);

}
