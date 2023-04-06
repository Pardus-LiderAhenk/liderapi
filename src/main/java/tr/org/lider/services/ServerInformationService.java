package tr.org.lider.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tr.org.lider.repositories.ServerInformationRepository;

@Service
public class ServerInformationService {
	
	@Autowired
	ServerInformationRepository serverInformationRepository;
	

}
