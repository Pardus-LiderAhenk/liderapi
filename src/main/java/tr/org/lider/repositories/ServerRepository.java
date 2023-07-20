package tr.org.lider.repositories;

import tr.org.lider.entities.ServerImpl;

public interface ServerRepository extends BaseJpaRepository<ServerImpl, Long> {

	//Page<ServerImpl> findByDeletedOrderByCreateDateDesc(Pageable pageable, Boolean deleted);

	//List<ServerImpl> findAllByDeleted(boolean deleted);
}
