package tr.org.lider.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import tr.org.lider.entities.ServerImpl;
import tr.org.lider.models.SshUserInfo;
import tr.org.lider.repositories.ServerInformationRepository;
import tr.org.lider.services.RemoteSshService;
import tr.org.lider.services.ServerInformationService;
import tr.org.lider.services.ServerService;

@RestController
@RequestMapping("/api/server")
@Tag(name = "Server", description = "Server Rest Service")
public class ServerController {

	@Autowired
	private ServerService serverService;
	
	@Autowired
	private RemoteSshService sshService;
	
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
//				.body(serverService.add(server));
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
    @PostMapping(value = "/list")
    public ResponseEntity<HashMap<String, Object>> getServerList() {
		HashMap<String, Object> resultMap = new HashMap<>();
        try {
        
        return ResponseEntity
              .status(HttpStatus.OK)
              .body(resultMap); // Connection successful
        } catch (Exception e) {
            return ResponseEntity
                  .status(HttpStatus.BAD_REQUEST)
                  .body(null); 
        }
	}
}
