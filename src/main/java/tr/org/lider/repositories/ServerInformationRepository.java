package tr.org.lider.repositories;

import tr.org.lider.entities.ServerInformationImpl;

public interface ServerInformationRepository extends BaseJpaRepository<ServerInformationImpl, Long>{

	//List<ServerInformationImpl> getServer(String id);
	
	//@Query(value = "SELECT property_value as property_value * FROM server_information"
	//				+ "where property_name= :name " , nativeQuery = true)
	
	//List<String> getPropertyValueByName(@Param("name") String name);
}
