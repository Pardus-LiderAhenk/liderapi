package tr.org.lider.repositories;

import java.util.List;

import tr.org.lider.entities.ServerImpl;

public interface ServerRepository extends BaseJpaRepository<ServerImpl, Long> {

	List<ServerImpl> findById(String serverId);
}
