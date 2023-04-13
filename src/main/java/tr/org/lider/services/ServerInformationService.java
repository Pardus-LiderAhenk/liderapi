package tr.org.lider.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import tr.org.lider.repositories.ServerInformationRepository;

@Service
public class ServerInformationService<ServerInformationImpl> {
	
	@Autowired
	ServerInformationRepository serverInformationRepository;
	
	//public List<ServerInformationImpl> findAllServerInformations(){
		//return serverInformationRepository.findAll();
		
	//}

}
