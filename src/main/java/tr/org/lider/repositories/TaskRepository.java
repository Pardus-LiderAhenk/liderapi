package tr.org.lider.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tr.org.lider.entities.TaskImpl;

public interface TaskRepository extends BaseJpaRepository<TaskImpl, Integer>{

	@Query(nativeQuery = true,
			value = "SELECT p.description, COUNT(*) as quantity FROM c_task AS t "
					+ "LEFT OUTER JOIN c_plugin AS p ON t.plugin_id = p.plugin_id "
					+ "WHERE t.task_id IN "
					+ "(SELECT c.task_id FROM c_command AS c where c.command_owner_uid = :username) "
					+ "GROUP BY p.description "
					+ "ORDER BY quantity DESC;")
	List<Object[]> findExecutedTaskWithCount(@Param("username") String username); 
	
	@Query(nativeQuery = true,
			value = "SELECT * FROM c_task t "
					+"WHERE t.id = :taskId ")
	List<TaskImpl> findByTask(@Param("taskId") Long taskId);
}
