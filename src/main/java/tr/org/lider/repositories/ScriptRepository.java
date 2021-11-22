package tr.org.lider.repositories;
import java.util.List;
import tr.org.lider.entities.ScriptTemplate;


public interface ScriptRepository extends BaseJpaRepository<ScriptTemplate, Long>{
	List<ScriptTemplate> findByDeletedOrderByCreateDateDesc(Boolean deleted);
}
