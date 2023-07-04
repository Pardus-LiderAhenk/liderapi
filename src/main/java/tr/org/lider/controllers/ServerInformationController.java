package tr.org.lider.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.constant.LiderConstants;
import tr.org.lider.services.RemoteSshService;
import tr.org.lider.services.ServerInformationService;

@RestController
@RequestMapping("/api/server-information")
@Tag(name="Server-Information", description = "Server Information Rest Service")
public class ServerInformationController {	
	
	@Autowired
	private RemoteSshService sshService;
	
	@Autowired
	private ServerInformationService serverInformationService;
	
	@Operation()
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Execute server information. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not execute server information. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })	
	@PostMapping(value = "/execute-command", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> executeSshCommand(
			@RequestParam (value = "hostname", required = true) String hostname,
            @RequestParam (value = "password", required = true) String password,
            @RequestParam (value = "username", required = true) String username){
		
//		String command = "echo 'select name as os_name , version as os_version, null as machine_disk, null as disk_total, "
//				+ "null as  total_disk_empty, null as memory_total, null as memory_free, null as  hostname, "
//				+ "null as  physical_memory, null as  computer_name from os_version union select device as machine_disk, blocks_size as disk_total,  blocks_free as total_disk_empty , "
//				+ "null as os_name, null as os_version, null as memory_total, null as memory_free, null as  hostname, "
//				+ "null as  physical_memory, null as  computer_name from mounts union select memory_total, memory_free , "
//				+ "null as os_name, null as os_version,   null as machine_disk, null as disk_total, null as  total_disk_empty, "
//				+ "null as  hostname, null as  physical_memory, "
//				+ "null as  computer_name from memory_info union select hostname, "
//				+ "physical_memory, computer_name, null as os_name, null as os_version,  "
//				+ " null as machine_disk, null as disk_total, null as  total_disk_empty, null as memory_total, null as memory_free from system_info;' | osqueryi --json";
		try {
			sshService.setHost(hostname);
	    	sshService.setUser(username);
	    	sshService.setPassword(password);
	    	
			String result = sshService.executeCommand(LiderConstants.ServerInformation.OSQUERY_QUERY);
			HashMap<Object, Object> serverInformationValue = new HashMap<>();
			
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(result);
			
		} catch (Exception e) {
			e.printStackTrace();
			HttpHeaders headers = new HttpHeaders();
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		
	}
}
