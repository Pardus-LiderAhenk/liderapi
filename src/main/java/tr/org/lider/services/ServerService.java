package tr.org.lider.services;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tr.org.lider.entities.OperationType;
import tr.org.lider.entities.ServerImpl;
import tr.org.lider.entities.ServerInformationImpl;
import tr.org.lider.repositories.ServerInformationRepository;
import tr.org.lider.repositories.ServerRepository;

@Service
public class ServerService {
	
	@Autowired
	private ServerRepository serverRepository;
	
	@Autowired
	private ServerInformationRepository serverInformationRepository;
	
	@Autowired
	private OperationLogService operationLogService;
	
	

	
	public ServerImpl add(ServerImpl server) {
		
		ServerImpl savedServer = serverRepository.save(server);
		return serverRepository.save(savedServer);
	}
	
	public ServerImpl delete(Long id,ServerImpl server) {
		
		ServerImpl savedServer = serverRepository.save(server);
		try {
			operationLogService.saveOperationLog(OperationType.DELETE, "Sunucu silindi",null,null,null,savedServer.getId());
			
		} catch (Exception e) {
			e.printStackTrace();		
		}
		return savedServer;	
	}
	
	public ServerInformationImpl save(String result, ServerImpl server) throws JsonProcessingException {
		
	
		ObjectMapper mapper = new ObjectMapper();
		
		List<Map<String, Object>> listOfMaps = mapper.readValue(result, new TypeReference<List<Map<String, Object>>>() {});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("computer_name").toString())))
		.forEach(nameMap -> {
			ServerInformationImpl serverInf = new ServerInformationImpl(server, "computer_name", nameMap.get("computer_name").toString());
			serverInformationRepository.save(serverInf);
			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("hostname").toString())))
		.forEach(nameMap -> {
			ServerInformationImpl serverInf = new ServerInformationImpl(server, "hostname", nameMap.get("hostname").toString());
			serverInformationRepository.save(serverInf);
			
		});
	
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("mac_addr").toString())))
		.forEach(nameMap -> {
			ServerInformationImpl serverInf = new ServerInformationImpl(server, "mac_addr", nameMap.get("mac_addr").toString());
			serverInformationRepository.save(serverInf);
			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("os_name").toString())))
		.forEach(nameMap -> {
			ServerInformationImpl serverInf = new ServerInformationImpl(server, "os_name", nameMap.get("os_name").toString());
			serverInformationRepository.save(serverInf);
			
		});
		
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("os_version").toString())))
		.forEach(nameMap -> {
			ServerInformationImpl serverInf = new ServerInformationImpl(server, "os_version", nameMap.get("os_version").toString());
			serverInformationRepository.save(serverInf);
			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("disk_total").toString())))
		.forEach(nameMap -> {
			ServerInformationImpl serverInf = new ServerInformationImpl(server, "disk_total", nameMap.get("disk_total").toString());
			serverInformationRepository.save(serverInf);
			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("machine_disk").toString())))
		.forEach(nameMap -> {
			ServerInformationImpl serverInf = new ServerInformationImpl(server, "machine_disk", nameMap.get("machine_disk").toString());
			serverInformationRepository.save(serverInf);
			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("memory_free").toString())))
		.forEach(nameMap -> {
			ServerInformationImpl serverInf = new ServerInformationImpl(server, "memory_free", nameMap.get("memory_free").toString());
			serverInformationRepository.save(serverInf);
			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("memory_total").toString())))
		.forEach(nameMap -> {
			ServerInformationImpl serverInf = new ServerInformationImpl(server, "memory_total", nameMap.get("memory_total").toString());
			serverInformationRepository.save(serverInf);
			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("physical_memory").toString())))
		.forEach(nameMap -> {
			ServerInformationImpl serverInf = new ServerInformationImpl(server, "physical_memory", nameMap.get("physical_memory").toString());
			serverInformationRepository.save(serverInf);
			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("total_disk_empty").toString())))
		.forEach(nameMap -> {
			ServerInformationImpl serverInf = new ServerInformationImpl(server, "total_disk_empty", nameMap.get("total_disk_empty").toString());
			serverInformationRepository.save(serverInf);
			
		});
	
		return null;	
	}
	
	public List<ServerImpl> list(String serverId ){
		
		
		return null;
		
		
	}
	
	public Optional<ServerImpl> findServerByID(Long serverId) {
        return serverRepository.findById(serverId);
	}
	
	public List<ServerImpl> findServerAll() {
        return serverRepository.findAll();
	}
}

