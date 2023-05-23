package tr.org.lider.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import tr.org.lider.entities.ServerImpl;

public interface ServerRepository extends BaseJpaRepository<ServerImpl, Long> {

	Page<ServerImpl> findByDeletedOrderByCreateDateDesc(Pageable pageable, Boolean deleted);

	List<ServerImpl> findAllByDeleted(boolean deleted);
}
