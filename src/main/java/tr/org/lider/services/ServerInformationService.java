package tr.org.lider.services;

import org.bouncycastle.asn1.x509.qualified.TypeOfBiometricData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;

import tr.org.lider.constant.LiderConstants;
import tr.org.lider.entities.ServerImpl;
import tr.org.lider.repositories.ServerInformationRepository;
import tr.org.lider.repositories.ServerRepository;

@Service
public class ServerInformationService<ServerInformationImpl> {
	
	@Autowired
	ServerInformationRepository serverInformationRepository;
	
	
	public List<ServerInformationImpl> findServerById(String id) {
        return null;
        		//serverInformationRepository.getServer(id);
	}
	


}
