package tr.org.lider.repositories;

import java.util.List;

import tr.org.lider.entities.ServerImpl;

public interface ServerRepository extends BaseJpaRepository<ServerImpl, Long> {

	//Page<ServerImpl> findByDeletedOrderByCreateDateDesc(Pageable pageable, Boolean deleted);

	List<ServerImpl> findById(String serverId);
}
