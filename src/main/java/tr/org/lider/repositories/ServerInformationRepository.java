package tr.org.lider.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import tr.org.lider.entities.ServerInformationImpl;

public interface ServerInformationRepository extends BaseJpaRepository<ServerInformationImpl, Long>{

	//@Query(value = "SELECT * FROM ServerInformationImpl WHERE id")
	//List<ServerInformationImpl> findAll();
	
}
