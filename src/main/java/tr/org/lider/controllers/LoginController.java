package tr.org.lider.controllers;

import java.util.List;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import tr.org.lider.constant.LiderConstants;
import tr.org.lider.entities.OperationType;
import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.messaging.messages.SessionInfo;
import tr.org.lider.security.User;
import tr.org.lider.services.CommandService;
import tr.org.lider.services.ConfigurationService;
import tr.org.lider.services.OperationLogService;
import tr.org.lider.services.XMPPPrebindService;

/**
 * 
 * @author M. Edip YILDIZ
 */
@Controller
public class LoginController {
	
	Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private LDAPServiceImpl ldapService;
	
	@Autowired
	private CommandService commandService;
	
	@Autowired
	private BuildProperties buildProperties;

	@Autowired
	private XMPPPrebindService xmppPrebindService;
	
	@Autowired
	private OperationLogService operationLogService; 
	
	
	@RequestMapping(value = "/",method = {RequestMethod.GET, RequestMethod.POST})
	public String getMainPage(Model model, Authentication authentication) {
		try {
			User user = (User)authentication.getPrincipal();
			logger.info("User logged as ldap dn " + user.getDn());
			logger.info("User has authorities: " + user.getAuthorities());
			model.addAttribute("user", user);
			model.addAttribute("userName", user.getName());
			
			model.addAttribute("password", user.getPassword());
			model.addAttribute("userNameJid", user.getName() + "@" + configurationService.getXmppServiceName());
			logger.info("User jid : " + user.getName() + "@" + configurationService.getXmppServiceName());
			model.addAttribute("xmppHost", configurationService.getXmppHost());
			model.addAttribute("roleNames", user.getRoles());
			logger.info("User roles : " + user.getRoles());
			
			String version=buildProperties.getVersion();
			model.addAttribute("liderVersion", version);
			
			SessionInfo sessionInfo= xmppPrebindService.getSession(user.getName(), user.getPassword());
		    model.addAttribute("SID", sessionInfo.getSid());
		    model.addAttribute("RID", sessionInfo.getRid());
		    model.addAttribute("JID", sessionInfo.getJid());
		    model.addAttribute("userDomainType", configurationService.getDomainType());
		    
		    operationLogService.saveOperationLog(OperationType.LOGIN,"Lider Arayüze Giriş Yapıldı.",null);
		    logger.info("Getting prebind sessionInfo SID {} RID {} JID {} ", sessionInfo.getSid(),sessionInfo.getRid(),sessionInfo.getJid());
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//get count of users
		int countOfLDAPUsers = 0;
		int countOfComputers = 0;
		try {
			List<LdapEntry> ldapUserList = ldapService.findSubEntries(configurationService.getLdapRootDn(), 
					"(objectclass=pardusAccount)", new String[] { "*" }, SearchScope.SUBTREE);
			List<LdapEntry> ldapComputerList = ldapService.findSubEntries(configurationService.getLdapRootDn(), 
					"(objectclass=pardusDevice)", new String[] { "*" }, SearchScope.SUBTREE);
			countOfLDAPUsers = ldapUserList.size();
			countOfComputers = ldapComputerList.size();
		} catch (LdapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute("totalUserNumber", countOfLDAPUsers);
		model.addAttribute("totalComputerNumber", countOfComputers);
		
		//sent task total number
		model.addAttribute("totalSentTaskNumber", commandService.getTotalCountOfSentTasks());
		return LiderConstants.Pages.MAIN_PAGE;
	}
	
	@RequestMapping(value = "/perform_logout")
	public String logout(Model model, Authentication authentication) {
		operationLogService.saveOperationLog(OperationType.LOGOUT,"Lider Arayüzden Çıkıldı.",null);
		return "login";
	}
	
	@RequestMapping(value = "/login")
	public String login(Model model, Authentication authentication) {

		if(configurationService.isConfigurationDone()) {
			return "login";
		} else {
			return "config";
		}
	}
	
	@RequestMapping(value = "/changeLanguage", method = {RequestMethod.POST})
	@ResponseBody
	public Boolean changeLanguage(@RequestParam String langa1799b6ac27611eab3de0242ac130004, Model model, Authentication authentication) throws LdapException {
		User user = (User)authentication.getPrincipal();
		ldapService.updateEntryRemoveAttribute(user.getDn(), "preferredLanguage");
		ldapService.updateEntryAddAtribute(user.getDn(), "preferredLanguage", langa1799b6ac27611eab3de0242ac130004);
		return true;
	}
}