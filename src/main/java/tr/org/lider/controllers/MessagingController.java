package tr.org.lider.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import tr.org.lider.messaging.messages.SessionInfo;
import tr.org.lider.models.ConfigParams;
import tr.org.lider.security.User;
import tr.org.lider.services.AuthenticationService;
import tr.org.lider.services.ConfigurationService;
import tr.org.lider.services.XMPPPrebindService;

@RestController
@RequestMapping("/api/messaging")
public class MessagingController {
	
	@Autowired
	private XMPPPrebindService xmppPrebindService;
	
	@Autowired
	private ConfigurationService configurationService;

	@RequestMapping(value="/getMessagingServerInfo", method=RequestMethod.POST)
	public ResponseEntity<?> getMessagingServerInfo() {
		
		ConfigParams  configParams = configurationService.getConfigParams();
		return ResponseEntity.ok(new Object[] {getMessageServiceInfo(AuthenticationService.getUser()), configParams});
	}
	
	private User getMessageServiceInfo(User user) {
		SessionInfo sessionInfo= xmppPrebindService.getSession(user.getName(), user.getPassword());
		user.setJID(sessionInfo.getJid());
		user.setRID(sessionInfo.getRid());
		user.setSID(sessionInfo.getSid());
		return user;
		
	}
}
