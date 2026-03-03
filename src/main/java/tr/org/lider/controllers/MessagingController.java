package tr.org.lider.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.annotation.Secured;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.messaging.messages.SessionInfo;
import tr.org.lider.security.User;
import tr.org.lider.services.ConfigurationService;
import tr.org.lider.services.XMPPPrebindService;
import tr.org.lider.constant.RoleConstants;

@Secured({RoleConstants.ROLE_ADMIN})
@RestController
@RequestMapping("/api/messaging")
@Tag(name="Message Server İnfo",description="Message Server Rest Service")
public class MessagingController {
	
	@Autowired
	private XMPPPrebindService xmppPrebindService;
	
	@Autowired
	private ConfigurationService configurationService;

	@Operation(summary = "Gets messaging server info", description = "", tags = { "message-server-info-service" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns messaging server info"),
			  @ApiResponse(responseCode = "417",description = "Could not get message server info. Unexpected error occured",
		  		 content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value="/get-messaging-server-info")
	public ResponseEntity<?> getMessagingServerInfo() {

//		ConfigParams  configParams = configurationService.getConfigParams();
//		return ResponseEntity.ok(new Object[] {getMessageServiceInfo(AuthenticationService.getUser()), configParams});
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(null);
	}
	
	private User getMessageServiceInfo(User user) {
		SessionInfo sessionInfo= xmppPrebindService.getSession(user.getName(), user.getPassword());
		user.setJID(sessionInfo.getJid());
		user.setRID(sessionInfo.getRid());
		user.setSID(sessionInfo.getSid());
		return user;
		
	}
}
