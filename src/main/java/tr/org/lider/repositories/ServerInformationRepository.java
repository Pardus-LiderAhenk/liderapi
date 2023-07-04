package tr.org.lider.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tr.org.lider.entities.ServerInformationImpl;

public interface ServerInformationRepository extends BaseJpaRepository<ServerInformationImpl, Long>{

	//List<ServerInformationImpl> getServer(String id);
	
	//@Query(value = "SELECT property_value as property_value * FROM server_information"
	//				+ "where property_name= :name " , nativeQuery = true)
	
	//List<String> getPropertyValueByName(@Param("name") String name);
}
