package tr.org.lider.repositories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import tr.org.lider.entities.ScriptTemplate;

public interface ScriptRepository extends BaseJpaRepository<ScriptTemplate, Long>{
	Page<ScriptTemplate> findByDeletedOrderByCreateDateDesc(Pageable pageable, Boolean deleted);
}