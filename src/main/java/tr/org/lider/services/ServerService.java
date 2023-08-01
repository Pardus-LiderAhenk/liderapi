package tr.org.lider.services;


import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tr.org.lider.constant.LiderConstants;
import tr.org.lider.entities.OperationType;
import tr.org.lider.entities.ServerImpl;
import tr.org.lider.entities.ServerInformationImpl;
import tr.org.lider.repositories.ServerRepository;

@Service
public class ServerService {
	
	@Autowired
	private ServerRepository serverRepository;
	
	
	@Autowired
	private OperationLogService operationLogService;
	
	@Autowired
	private RemoteSshService sshService;
	
	
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
	
	public ServerImpl save(ServerImpl server) {
		return serverRepository.save(server);
	}
	
	public ServerImpl save(String result, ServerImpl server) throws JsonProcessingException {
		
	
		ObjectMapper mapper = new ObjectMapper();
		
		List<Map<String, Object>> listOfMaps = mapper.readValue(result, new TypeReference<List<Map<String, Object>>>() {});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("computer_name").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "computer_name", nameMap.get("computer_name").toString()));
			
		});
		
	
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("mac_addr").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "mac_addr", nameMap.get("mac_addr").toString()));


			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("os_name").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "os_name", nameMap.get("os_name").toString()));

			
		});
		
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("os_version").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "os_version", nameMap.get("os_version").toString()));

			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("disk_total").toString())))
		.forEach(nameMap -> {
			
			server.addProperty(new ServerInformationImpl(server, "disk_total", nameMap.get("disk_total").toString()));

		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("machine_disk").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "machine_disk", nameMap.get("machine_disk").toString()));

			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("memory_free").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "memory_free", nameMap.get("memory_free").toString()));

			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("memory_total").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "memory_total", nameMap.get("memory_total").toString()));

			
		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("physical_memory").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "physical_memory", nameMap.get("physical_memory").toString()));

		});
		
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("total_disk_empty").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "total_disk_empty", nameMap.get("total_disk_empty").toString()));

			
		});
	
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("uptime_days").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "uptime_days", nameMap.get("uptime_days").toString()));

			
		});
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("uptime_hours").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "uptime_hours", nameMap.get("uptime_hours").toString()));

			
		});
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("uptime_minutes").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "uptime_minutes", nameMap.get("uptime_minutes").toString()));
		});
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("cpu_user").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "cpu_user", nameMap.get("cpu_user").toString()));
		});
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("cpu_system").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "cpu_system", nameMap.get("cpu_system").toString()));
		});
		listOfMaps.stream()
		.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("cpu_idle").toString())))
		.forEach(nameMap -> {
			server.addProperty(new ServerInformationImpl(server, "cpu_idle", nameMap.get("cpu_idle").toString()));
		});
		server.setStatus(true);
		return serverRepository.save(server);
		
	}
	
	public ServerImpl update(ServerImpl server) {
		//server.setModifyDate(new Date());
		ServerImpl savedserver = serverRepository.save(server);
		try {
			operationLogService.saveOperationLog(OperationType.UPDATE, "Server  güncellendi.", null);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return savedserver;
	}
	
	
	public List<ServerImpl> findServerByIdList(String serverId ){
		
		return serverRepository.findById(serverId);
		
	}
	
	public Optional<ServerImpl> findServerByID(Long serverId) {
        return serverRepository.findById(serverId);
	}
	
	public void delete(Long id) {
		serverRepository.deleteById(id);

	}
	
	public List<ServerImpl> findServerAll() {
        return serverRepository.findAll();
	}
	
	public List<ServerImpl> serverList() throws Throwable{
		List<ServerImpl> serverList = findServerAll();
		String updateResult;
		int i = 0;
		for(i = 0 ; i< serverList.size(); i++) {
			ServerImpl server = serverList.get(i);
			sshService.setHost(serverList.get(i).getIp());
			sshService.setUser(serverList.get(i).getUser());
			sshService.setPassword(serverList.get(i).getPassword());
			
			updateResult = sshService.executeCommand(LiderConstants.ServerInformation.OSQUERY_QUERY);		
			if (updateResult != null) {
				String[] passwordSplit = updateResult.split("\\[");
				updateResult = "[" + passwordSplit[passwordSplit.length-1];
				
				ObjectMapper mapper = new ObjectMapper();
				
				List<Map<String, Object>> updateResults = mapper.readValue(updateResult, new TypeReference<List<Map<String, Object>>>() {});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("computer_name").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("computer_name")) {
							if (!prop.getPropertyValue().equals(nameMap.get("computer_name").toString())) {
								prop.setPropertyValue(nameMap.get("computer_name").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("mac_addr").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("mac_addr")) {
							if (!prop.getPropertyValue().equals(nameMap.get("mac_addr").toString())) {
								prop.setPropertyValue(nameMap.get("mac_addr").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("os_name").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("os_name")) {
							if (!prop.getPropertyValue().equals(nameMap.get("os_name").toString())) {
								prop.setPropertyValue(nameMap.get("os_name").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("os_version").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("os_version")) {
							if (!prop.getPropertyValue().equals(nameMap.get("os_version").toString())) {
								prop.setPropertyValue(nameMap.get("os_version").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("disk_total").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("disk_total")) {
							if (!prop.getPropertyValue().equals(nameMap.get("disk_total").toString())) {
								prop.setPropertyValue(nameMap.get("disk_total").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("machine_disk").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("machine_disk")) {
							if (!prop.getPropertyValue().equals(nameMap.get("machine_disk").toString())) {
								prop.setPropertyValue(nameMap.get("machine_disk").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("memory_free").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("memory_free")) {
							if (!prop.getPropertyValue().equals(nameMap.get("memory_free").toString())) {
								prop.setPropertyValue(nameMap.get("memory_free").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("memory_total").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("memory_total")) {
							if (!prop.getPropertyValue().equals(nameMap.get("memory_total").toString())) {
								prop.setPropertyValue(nameMap.get("memory_total").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("physical_memory").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("physical_memory")) {
							if (!prop.getPropertyValue().equals(nameMap.get("physical_memory").toString())) {
								prop.setPropertyValue(nameMap.get("physical_memory").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("total_disk_empty").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("total_disk_empty")) {
							if (!prop.getPropertyValue().equals(nameMap.get("total_disk_empty").toString())) {
								prop.setPropertyValue(nameMap.get("total_disk_empty").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("uptime_days").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("uptime_days")) {
							if (!prop.getPropertyValue().equals(nameMap.get("uptime_days").toString())) {
								prop.setPropertyValue(nameMap.get("uptime_days").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("uptime_hours").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("uptime_hours")) {
							if (!prop.getPropertyValue().equals(nameMap.get("uptime_hours").toString())) {
								prop.setPropertyValue(nameMap.get("uptime_hours").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("uptime_minutes").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("uptime_minutes")) {
							if (!prop.getPropertyValue().equals(nameMap.get("uptime_minutes").toString())) {
								prop.setPropertyValue(nameMap.get("uptime_minutes").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("cpu_user").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("cpu_user")) {
							if (!prop.getPropertyValue().equals(nameMap.get("cpu_user").toString())) {
								prop.setPropertyValue(nameMap.get("cpu_user").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("cpu_system").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("cpu_system")) {
							if (!prop.getPropertyValue().equals(nameMap.get("cpu_system").toString())) {
								prop.setPropertyValue(nameMap.get("cpu_system").toString());
							}
						}
					}
				});
				
				updateResults.stream()
				.filter(nameMap -> !(StringUtils.isEmpty(nameMap.get("cpu_idle").toString())))
				.forEach(nameMap -> {

					for (ServerInformationImpl prop : server.getProperties()) {
						if (prop.getPropertyName().equals("cpu_idle")) {
							if (!prop.getPropertyValue().equals(nameMap.get("cpu_idle").toString())) {
								prop.setPropertyValue(nameMap.get("cpu_idle").toString());
							}
						}
					}
				});
				
				
				server.setStatus(true);
				serverRepository.save(server);
			}
			else {
				server.setStatus(false);
				serverRepository.save(server);
				System.out.println("hata yazılacak");
			}
		}
		return serverList;
	}
}

