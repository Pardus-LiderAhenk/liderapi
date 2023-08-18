package tr.org.lider.controllers;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import tr.org.lider.entities.RegistrationTemplateImpl;
import tr.org.lider.entities.ServerImpl;
import tr.org.lider.services.RemoteSshService;
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
		
	
	@Operation()
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Added server. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not add server. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })	
	@PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServerImpl> serverAdd(@RequestBody ServerImpl server) throws Exception{
		sshService.setHost(server.getIp());
		sshService.setUser(server.getUser());
		sshService.setPassword(server.getPassword());
		HttpHeaders headers = new HttpHeaders();
		
		try {
			
			if(serverService.isServerReachable(server.getIp(), server.getPassword(), server.getUser())== true) {
				serverService.add(server);
				String result = sshService.executeCommand(LiderConstants.ServerInformation.OSQUERY_QUERY);
				String[] passwordSplit = result.split("\\[");
				result = "[" + passwordSplit[passwordSplit.length-1];
				return ResponseEntity
		              .status(HttpStatus.OK)
		              .body(serverService.save(result,server));	
			}
		
				return ResponseEntity
						.status(HttpStatus.OK)
						.body(serverService.add(server));
	
		} 
		catch (Exception e) {
			   e.printStackTrace();
			   return ResponseEntity
		                  .status(HttpStatus.BAD_REQUEST)
		                  .build();
		}
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
    public  ResponseEntity<List<ServerImpl>> getServerList() throws Throwable {
		
		HttpHeaders headers = new HttpHeaders();
		try {
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(serverService.serverList());
		}
		catch (Exception e) {
	
			return ResponseEntity
				.status(HttpStatus.EXPECTATION_FAILED)
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
					
		} 
		catch (DataAccessException e) {
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
	
	@Operation(summary = "Update server ", description = "", tags = { "" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "", 
			    content = { @Content(schema = @Schema(implementation = RegistrationTemplateImpl.class)) }),
			  @ApiResponse(responseCode = "417", description = "server id not found !", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServerImpl> updateServer(@RequestBody ServerImpl server) {
		try {
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(serverService.update(server));
		} 
		catch (DataAccessException e) {
			logger.error("Error updated server: " + e.getCause().getMessage());
			HttpHeaders headers = new HttpHeaders();
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}		
	
	}
	
	@Operation(summary = "Get server data", description = "", tags = { "" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "", 
			    content = { @Content(schema = @Schema(implementation = RegistrationTemplateImpl.class)) }),
			  @ApiResponse(responseCode = "417", description = "server id not found !", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@GetMapping(value = "/get-data", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ServerImpl>> getServerData() {
		try {
			
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(serverService.findServerAll());
		} 
		catch (DataAccessException e) {
			logger.error("Error updated server: " + e.getCause().getMessage());
			HttpHeaders headers = new HttpHeaders();
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}		
	
	}
	
}	
