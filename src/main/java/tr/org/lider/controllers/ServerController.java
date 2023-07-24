package tr.org.lider.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.constant.LiderConstants;
import tr.org.lider.entities.AgentImpl;
import tr.org.lider.entities.ServerImpl;
import tr.org.lider.entities.ServerInformationImpl;
import tr.org.lider.repositories.ServerInformationRepository;
import tr.org.lider.repositories.ServerRepository;
import tr.org.lider.services.RemoteSshService;
import tr.org.lider.services.ServerInformationService;
import tr.org.lider.services.ServerService;

@RestController
@RequestMapping("/api/server")
@Tag(name = "Server", description = "Server Rest Service")
public class ServerController {
	
	private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ServerService serverService;
	
	@Autowired
	private RemoteSshService sshService;
	
	@Autowired
	private ServerRepository serverRepository;
	
	@Autowired
	private ServerInformationService serverInformationService;
	
	@Autowired
	private  ServerInformationRepository serverInformationRepository;
	
	
	@Operation()
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Added server. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not add server. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })	
	@PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServerImpl> serverAdd(@RequestBody ServerImpl server){
		sshService.setHost(server.getIp());
		sshService.setUser(server.getUser());
		sshService.setPassword(server.getPassword());
		try {
			serverService.add(server);
			String result = sshService.executeCommand(LiderConstants.ServerInformation.OSQUERY_QUERY);
			String[] passwordSplit = result.split("\\[");
			result = "[" + passwordSplit[passwordSplit.length-1];
			
			serverService.save(result,server);
			
			return null;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;		
//		return ResponseEntity
//				.status(HttpStatus.OK)
//				.body(serverService.save(result,server));
	}
	
	
	@Operation(summary = "Check server connection", description = "", tags = { "connection" })
    @ApiResponses(value = { 
              @ApiResponse(responseCode = "200", description = "Server is available"),
              @ApiResponse(responseCode = "417", description = "Can not reach server. Unexpected error occurred", 
                content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/check-connection")
    public ResponseEntity<Boolean> isServerReachable(
    		@RequestParam (value = "hostname", required = true) String hostname,
            @RequestParam (value = "password", required = true) String password,
            @RequestParam (value = "username", required = true) String username) {

        try {
        JSch jsch = new JSch();

        Session session = jsch.getSession(username, hostname, LiderConstants.ServerInformation.SSH_PORT);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        session.disconnect();
        return ResponseEntity
              .status(HttpStatus.OK)
              .body(true); // Connection successful
        } catch (Exception e) {
            return ResponseEntity
                  .status(HttpStatus.BAD_REQUEST)
                  .body(false); 
        }
	}
	
	
	@Operation(summary = "Server List", description = "", tags = { "" })
    @ApiResponses(value = { 
              @ApiResponse(responseCode = "200", description = "Retrieved  server list"),
              @ApiResponse(responseCode = "417", description = "Can not get server list. Unexpected error occurred", 
                content = @Content(schema = @Schema(implementation = String.class))) })
    @GetMapping(value = "/list")
    public  ResponseEntity<List<ServerImpl>> getServerList() throws Exception {
		List<ServerImpl> serverList = serverService.findServerAll();
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
				
				server.setStatus(true);
				serverRepository.save(server);
			}
			else {
				server.setStatus(false);
				serverRepository.save(server);
				System.out.println("hata yazılacak");
			}
				
	}
		HttpHeaders headers = new HttpHeaders();
		if(!serverList.isEmpty()) {
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(serverList);
		}
		else {
			return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.headers(headers)
					.build();
	}

	
	}
	
	@Operation(summary = "Server delete", description = "", tags = {""})
	@ApiResponses(value= {
			 @ApiResponse(responseCode = "200", description = "" ),
			 @ApiResponse(responseCode = "417", description = "",
			 content = @Content(schema = @Schema(implementation = String.class)))
	 })
	@DeleteMapping(value = "/delete/id/{id}")
	public ResponseEntity<ServerImpl> deleteServer(@PathVariable Long id){
				 
		try {
			//Optional<ServerImpl> existingPolicyException = policyExceptionService.findPolicyExceptionByID(id);
			if(!serverService.findServerByID(id).isPresent()) {
				logger.error("Server to delete {} but id not found!", id);
	        	HttpHeaders headers = new HttpHeaders();
	        	headers.add("message", "Server id not found !");
	    		return ResponseEntity
	    				.status(HttpStatus.NOT_FOUND)
	    				.headers(headers)
	    				.build();
			}
			serverService.delete(id);
					
		} catch (DataAccessException e) {
			logger.error("Error delete server: " + e.getCause().getMessage());
			HttpHeaders headers = new HttpHeaders();
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(null);
	}
	
	@Operation(summary = "Server detail List", description = "", tags = { "" })
    @ApiResponses(value = { 
              @ApiResponse(responseCode = "200", description = ""),
              @ApiResponse(responseCode = "417", description = "Unexpected error occurred", 
                content = @Content(schema = @Schema(implementation = String.class))) })
    @PostMapping(value = "/detail/id/{id}")
    public  ResponseEntity<ServerImpl> getServerDetailList(@PathVariable Long serverId) {
		logger.debug("Server id:  {} ", serverId);

		Optional<ServerImpl> serverDetailList = serverService.findServerByID(serverId);
		HttpHeaders headers = new HttpHeaders();
		if(serverDetailList.isPresent()) {
			return new ResponseEntity<ServerImpl>(serverDetailList.get(), HttpStatus.OK);
		}
		else {
			return ResponseEntity
    				.status(HttpStatus.NOT_FOUND)
    				.headers(headers)
    				.build();
		}
	}
	
//	@Operation(summary = "Get details server informtaion", description = "", tags = { "script" })
//	@ApiResponses(value = { 
//			  @ApiResponse(responseCode = "200", description = "Returns details of selected server. Successful"),
//			  @ApiResponse(responseCode = "417", description = "Could not get details of selected server. Unexpected error occurred", 
//			    content = @Content(schema = @Schema(implementation = String.class))) })
//	@PostMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<ServerImpl> findServerById(@RequestParam (value = "serverId") String serverId) {
//		List<ServerImpl> server = serverService.findServerByIdList(serverId);
//		if (server != null && server.size() > 0) {
//			return ResponseEntity
//					.status(HttpStatus.OK)
//					.body(server.get(0));
//					
//		} else {
//			HttpHeaders headers = new HttpHeaders();
//    		return ResponseEntity
//    				.status(HttpStatus.EXPECTATION_FAILED)
//    				.headers(headers)
//    				.build();
//		}
//	}
	
	
}
