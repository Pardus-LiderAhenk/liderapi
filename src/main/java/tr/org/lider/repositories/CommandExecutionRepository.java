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
	
//	@Query("SELECT * FROM CommandExecutionImpl cex WHERE command_send = 0 ORDER BY create_date LIMIT 10")
//	List<CommandExecutionImpl> findCommandExecutionByTaskSendCommand();
	
	@Query("SELECT cex FROM CommandExecutionImpl cex " +
		       "WHERE cex.commandSend = 0 AND cex.uid IN :targetList " +
		       "ORDER BY cex.createDate")
	List<Object[]> findCommandExecutionByUidList(@Param("targetList") List<LdapEntry> targetList);
			//, @Param("uidListSize") int uidListSize);
	
	void deleteByDn(String dn);
}
