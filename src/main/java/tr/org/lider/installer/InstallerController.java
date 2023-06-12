package tr.org.lider.installer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import tr.org.lider.entities.ConkyTemplate;
import tr.org.lider.ldap.LdapEntry;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import tr.org.lider.services.RemoteSshService;


@RestController
@RequestMapping("/installed")
public class InstallerController {

	@RequestMapping(method=RequestMethod.GET)
	public String getStatus() {
		
		return "oki";
	}
	
	@Operation(summary = "Gets command result", description = "", tags = { "ansible" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns users list. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not get users list. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/ansible")
	public ResponseEntity<List<String>> getUsers() {
		
	  String hostname = "192.168.56.136";
      String username = "test";
      String password = "1";
	
      List<String> result = new ArrayList<String>();
      
      RemoteSshService tuncay = new RemoteSshService(hostname, username, password);
      try {
//			String result = executor.executeCommand("ansible-playbook  /home/test/test-project/playbook-pingtest.yaml -i /home/test/test-project/inventory.txt");
//			String result = null;
			result.add(tuncay.executeCommand("ls -l /tmp/"));

			System.err.println("Command output: " + result);
		} catch (Exception e1) {
			System.out.println("ÇALIŞMADIII");
			e1.printStackTrace();
		}
		
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(result);
				
	}
	
//	@Operation(summary = "Check server connection", description = "", tags = { "ping" })
//	@ApiResponses(value = { 
//			  @ApiResponse(responseCode = "200", description = "Server is available"),
//			  @ApiResponse(responseCode = "417", description = "Can not reach server. Unexpected error occurred", 
//			    content = @Content(schema = @Schema(implementation = String.class))) })
//	@PostMapping(value = "/ping")
//	public ResponseEntity<Boolean> isServerReachable(@RequestParam (value = "host", required = true) String host) {	
//		
//		
////	  String hostname = "192.168.56.136";
//      
//      try (Socket socket = new Socket()) {
//          InetSocketAddress address = new InetSocketAddress(host, 22);
//          socket.connect(address, 5000);
//          return ResponseEntity
//  				.status(HttpStatus.OK)
//  				.body(true); // Connection successful
//      } catch (IOException e) {
//    	  return ResponseEntity
//  				.status(HttpStatus.OK)
//  				.body(false); // Connection failed
//      }
      
      

				
      @Operation(summary = "Check server connection", description = "", tags = { "connection" })
      @ApiResponses(value = { 
  			  @ApiResponse(responseCode = "200", description = "Server is available"),
  			  @ApiResponse(responseCode = "417", description = "Can not reach server. Unexpected error occurred", 
  			    content = @Content(schema = @Schema(implementation = String.class))) })
      @PostMapping(value = "/check-connection")
      public ResponseEntity<Boolean> isServerReachable(@RequestParam (value = "host", required = true) String host,
    		  										   @RequestParam (value = "password", required = true) String password,
    		  										   @RequestParam (value = "username", required = true) String username) {	
	
    	  try {
          JSch jsch = new JSch();
          Session session = jsch.getSession(username, host, 22);
          session.setPassword(password);
          session.setConfig("StrictHostKeyChecking", "no");
          session.connect();
          session.disconnect();
          return ResponseEntity
				.status(HttpStatus.OK)
				.body(true); // Connection successful
          } catch (Exception e) {
        	  return ResponseEntity
    				.status(HttpStatus.OK)
    				.body(false); // Connection successful
          }
	}
	
	
	
	
	
	
	
	
	
	
	
	
}