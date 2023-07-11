package tr.org.lider.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import tr.org.lider.constant.LiderConstants.Pages;
import tr.org.lider.entities.AgentImpl;
import tr.org.lider.entities.ServerImpl;
import tr.org.lider.models.SshUserInfo;
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
		sshService.setHost(server.getHostname());
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
    public  ResponseEntity<List<ServerImpl>> getServerList() {
		List<ServerImpl> serverList = serverService.findServerAll();
		HttpHeaders headers = new HttpHeaders();
		if(!serverList.isEmpty()) {
//			return new ResponseEntity<ServerImpl>((ServerImpl) serverList, HttpStatus.OK);
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
	
//	@Operation(summary = "Server update list", description = "", tags = { "" })
//    @ApiResponses(value = { 
//              @ApiResponse(responseCode = "200", description = "Retrieved update server list"),
//              @ApiResponse(responseCode = "417", description = "Can not update server list. Unexpected error occurred", 
//                content = @Content(schema = @Schema(implementation = String.class))) })
//    @PostMapping(value = "/server/update/id/{id}")
//    public ResponseEntity<ServerImpl> updateServerList(@PathVariable Long serverId) {		
//		logger.debug("Server id:  {} ", serverId);
//		Optional<ServerImpl> server = serverService.findServerByID(serverId);
//		HttpHeaders headers = new HttpHeaders();
//		if(server.isPresent()) {
//			return new ResponseEntity<ServerImpl>(server.get(), HttpStatus.OK);
//		}
//		else {
//			return ResponseEntity
//    				.status(HttpStatus.NOT_FOUND)
//    				.headers(headers)
//    				.build();
//		}
	
	
}
