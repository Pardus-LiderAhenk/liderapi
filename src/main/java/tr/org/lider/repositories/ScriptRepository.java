package tr.org.lider.repositories;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import tr.org.lider.entities.ScriptTemplate;

public interface ScriptRepository extends BaseJpaRepository<ScriptTemplate, Long>{
	Page<ScriptTemplate> findByDeletedOrderByCreateDateDesc(Pageable pageable, Boolean deleted);

	List<ScriptTemplate> findAllByDeleted(boolean deleted);

   @Query(value = "SELECT s FROM ScriptTemplate s "
         + "WHERE s.deleted = :deleted "
         + "AND (:scriptName IS NULL OR :scriptName = '' OR LOWER(s.label) LIKE LOWER(CONCAT('%', :scriptName, '%'))) "
         + "ORDER BY s.createDate DESC")
   Page<ScriptTemplate> findByLabelContainingIgnoreCaseAndDeletedOrderByCreateDateDesc(
         Pageable pageable,
         @Param("scriptName") String scriptName,
         @Param("deleted") Boolean deleted);
}