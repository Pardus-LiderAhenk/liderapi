package tr.org.lider.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tr.org.lider.entities.CommandExecutionImpl;
import tr.org.lider.ldap.LdapEntry;

/**
 *
 */
public interface CommandExecutionRepository extends BaseJpaRepository<CommandExecutionImpl, Long>{
	
	
	@Query("SELECT DISTINCT ce FROM CommandImpl c INNER JOIN "
			+ "c.commandExecutions ce INNER JOIN c.task t WHERE ce.uid = ?1 AND t.id = ?2")
	List<CommandExecutionImpl> findCommandExecutionByTaskAndUid(String uid, Long taskId);
	
	List<CommandExecutionImpl> findCommandExecutionByCommandId(Long commandId);
	
	@Transactional
	@Modifying(clearAutomatically = true)
    @Query("UPDATE CommandExecutionImpl cex SET cex.dn = :newDN WHERE cex.dn = :currentDN")
    int updateAgentDN(@Param("currentDN") String currentDN, @Param("newDN") String newDN);
	
	@Transactional
	@Modifying(clearAutomatically = true)
    @Query("UPDATE CommandExecutionImpl cex SET cex.dn = :newDN, cex.uid = :newHostname WHERE cex.dn = :currentDN")
    int updateAgentDNAndUID(@Param("currentDN") String currentDN, @Param("newDN") String newDN ,@Param("newHostname") String newHostname);
	
	void deleteByDn(String dn);
	
	@Query("SELECT cex FROM TaskImpl t "
		    + "LEFT JOIN CommandImpl c ON (t.id = c.task.id) "
		    + "LEFT JOIN CommandExecutionImpl cex ON (c.id = cex.command.id) "
		    + "WHERE t.id = :taskId AND t.taskParts = 1 AND cex.commandSend = 0 "
		    + "ORDER BY t.createDate ASC")
	List<CommandExecutionImpl> findCommandExecution(@Param("taskId") Long taskId);
}
