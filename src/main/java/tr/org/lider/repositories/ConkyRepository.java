package tr.org.lider.repositories;

import java.util.List;

import tr.org.lider.entities.ConkyTemplate;

public interface ConkyRepository extends BaseJpaRepository<ConkyTemplate, Long>{
	List<ConkyTemplate> findByDeletedOrderByCreateDateDesc(Boolean deleted);

}
