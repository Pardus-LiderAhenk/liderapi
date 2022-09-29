package tr.org.lider.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tr.org.lider.entities.OperationType;
import tr.org.lider.entities.RoleImpl;
import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.ldap.OLCAccessRule;
import tr.org.lider.messaging.enums.DomainType;
import tr.org.lider.messaging.enums.Protocol;
import tr.org.lider.messaging.messages.XMPPClientImpl;
import tr.org.lider.models.ConfigParams;
import tr.org.lider.models.RegistrationTemplateType;
import tr.org.lider.security.CustomPasswordEncoder;
import tr.org.lider.services.AuthenticationService;
import tr.org.lider.services.ConfigurationService;
import tr.org.lider.services.OperationLogService;
import tr.org.lider.services.RoleService;
import tr.org.lider.messaging.enums.SudoRoleType;

/**
 * This controller is used for showing and updating all settings for lider
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 * 
 */

@Secured({"ROLE_ADMIN", "ROLE_SERVER_SETTINGS", "ROLE_CONSOLE_ACCESS_SETTINGS" })
@RestController
@RequestMapping("lider/settings")
public class SettingsController {

	Logger logger = LoggerFactory.getLogger(SettingsController.class);

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	XMPPClientImpl xmppClient;

	@Autowired
	private LDAPServiceImpl ldapService;
	
	@Autowired
	private OperationLogService operationLogService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private CustomPasswordEncoder customPasswordEncoder;
	
	@RequestMapping(method=RequestMethod.GET, value = "/configurations", produces = MediaType.APPLICATION_JSON_VALUE)
	public ConfigParams getConfigParams() {
		return configurationService.getConfigParams();
	}

	@RequestMapping(method=RequestMethod.GET ,value = "/getConsoleUsers", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<LdapEntry> getLiderConsoleUsers() {
		List<LdapEntry> ldapEntries = null;
		try {
			String filter= "(&(objectClass=pardusAccount)(objectClass=pardusLider)(liderPrivilege=ROLE_USER))";
			ldapEntries  = ldapService.findSubEntries(filter,
					new String[] { "*" }, SearchScope.SUBTREE);
		} catch (LdapException e) {
			e.printStackTrace();
		}
		return ldapEntries;
	}

	@RequestMapping(method=RequestMethod.POST, value = "/update/ldap", produces = MediaType.APPLICATION_JSON_VALUE)
	public ConfigParams updateLdapSettings(@RequestParam (value = "ldapServer", required = true) String ldapServer,
			@RequestParam (value = "ldapPort", required = true) String ldapPort,
			@RequestParam (value = "ldapUsername", required = true) String ldapUsername,
			@RequestParam (value = "ldapPassword", required = true) String ldapPassword,
			@RequestParam (value = "adIpAddress", required = true) String adIpAddress,
			@RequestParam (value = "adPort", required = true) String adPort,
			@RequestParam (value = "adDomainName", required = true) String adDomainName,
			@RequestParam (value = "adAdminUserName", required = true) String adAdminUserName,
			@RequestParam (value = "adAdminUserFullDN", required = true) String adAdminUserFullDN,
			@RequestParam (value = "adAdminPassword", required = true) String adAdminPassword,
			@RequestParam (value = "adHostName", required = true) String adHostName,
			@RequestParam (value = "adUseSSL", required = true) Boolean adUseSSL,
			@RequestParam (value = "adUseTLS", required = true) Boolean adUseTLS,
			@RequestParam (value = "adAllowSelfSignedCert", required = true) Boolean adAllowSelfSignedCert) {

		ConfigParams configParams = configurationService.getConfigParams();
		configParams.setLdapServer(ldapServer);
		configParams.setLdapPort(ldapPort);
		configParams.setLdapUsername(ldapUsername);
		configParams.setLdapPassword(ldapPassword);
		configParams.setAdIpAddress(adIpAddress);
		configParams.setAdPort(adPort);
		configParams.setAdDomainName(adDomainName);
		configParams.setAdAdminUserName(adAdminUserName);
		configParams.setAdAdminUserFullDN(adAdminUserFullDN);
		configParams.setAdAdminPassword(adAdminPassword);
		configParams.setAdHostName(adHostName);
		configParams.setAdUseSSL(adUseSSL);
		configParams.setAdUseTLS(adUseTLS);
		configParams.setAdAllowSelfSignedCert(adAllowSelfSignedCert);
		
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("ldapServer",configParams.getLdapServer());
		requestData.put("ldapPort",configParams.getLdapPort());
		requestData.put("ldapUsername",configParams.getLdapUsername());
		requestData.put("adIpAddress",configParams.getAdIpAddress());
		requestData.put("adPort",configParams.getAdPort());
		requestData.put("adDomainName",configParams.getAdDomainName());
		requestData.put("adAdminUserName",configParams.getAdAdminUserName());
		requestData.put("adAdminUserFullDN",configParams.getAdAdminUserFullDN());
		requestData.put("adHostName",configParams.getAdHostName());
		requestData.put("adUseSSL",configParams.getAdUseSSL());
		requestData.put("adUseTLS",configParams.getAdUseTLS());
		requestData.put("adAllowSelfSignedCert",configParams.getAdAllowSelfSignedCert());

		ObjectMapper dataMapper = new ObjectMapper();
		String jsonString = null ;
		try {
			jsonString = dataMapper.writeValueAsString(requestData);
		} catch (JsonProcessingException e1) {
			logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
		}
		String log = "LDAP server setting has been updated";
		operationLogService.saveOperationLog(OperationType.UPDATE, log, jsonString.getBytes(), null, null, null);
		
		return configurationService.updateConfigParams(configParams);
	}

	@RequestMapping(method=RequestMethod.POST, value = "/update/xmpp", produces = MediaType.APPLICATION_JSON_VALUE)
	public ConfigParams updateXMPPSettings(@RequestParam (value = "xmppHost", required = true) String xmppHost,
			@RequestParam (value = "xmppPort", required = true) int xmppPort,
			@RequestParam (value = "xmppUsername", required = true) String xmppUsername,
			@RequestParam (value = "xmppPassword", required = true) String xmppPassword,
			@RequestParam (value = "xmppMaxRetryConnectionCount", required = true) int xmppMaxRetryConnectionCount,
			@RequestParam (value = "xmppPacketReplayTimeout", required = true) int xmppPacketReplayTimeout,
			@RequestParam (value = "xmppPingTimeout", required = true) int xmppPingTimeout) {

		ConfigParams configParams = configurationService.getConfigParams();
		configParams.setXmppHost(xmppHost);
		configParams.setXmppPort(xmppPort);
		configParams.setXmppUsername(xmppUsername);
		configParams.setXmppPassword(xmppPassword);
		configParams.setXmppMaxRetryConnectionCount(xmppMaxRetryConnectionCount);
		configParams.setXmppPacketReplayTimeout(xmppPacketReplayTimeout);
		configParams.setXmppPingTimeout(xmppPingTimeout);
		ConfigParams updatedParams = configurationService.updateConfigParams(configParams);
		if(updatedParams != null) {
			logger.info("XMPP settings are updated. XMPP will disconnect and reconnect after resetting XMPP parameters.");
			try {
				xmppClient.disconnect();
				xmppClient.initXMPPClient();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("XMPP settings are updated but error occured while connecting with new settings. Message: " + e.getMessage());
			}
		}
		
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("xmppHost",configParams.getXmppHost());
		requestData.put("xmppPort",configParams.getXmppPort());
		requestData.put("xmppUsername",configParams.getXmppUsername());
		requestData.put("xmppMaxRetryConnectionCount",configParams.getXmppMaxRetryConnectionCount());
		requestData.put("xmppPacketReplayTimeout",configParams.getXmppPacketReplayTimeout());
		requestData.put("xmppPingTimeout",configParams.getXmppPingTimeout());

		ObjectMapper dataMapper = new ObjectMapper();
		String jsonString = null ;
		try {
			jsonString = dataMapper.writeValueAsString(requestData);
		} catch (JsonProcessingException e1) {
			logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
		}
		String log = "XMPP server setting has been updated";
		operationLogService.saveOperationLog(OperationType.UPDATE, log, jsonString.getBytes(), null, null, null);
		
		return updatedParams;
	}

	@RequestMapping(method=RequestMethod.POST, value = "/update/fileServer", produces = MediaType.APPLICATION_JSON_VALUE)
	public ConfigParams updateFileServerSettings(@RequestParam (value = "fileTransferType", required = true) Protocol fileTransferType,
			@RequestParam (value = "fileServerAddress", required = true) String fileServerAddress,
			@RequestParam (value = "fileServerUsername", required = true) String fileServerUsername,
			@RequestParam (value = "fileServerPassword", required = true) String fileServerPassword,
			@RequestParam (value = "fileServerPort", required = true) int fileServerPort,
			@RequestParam (value = "fileServerAgentFilePath", required = true) String fileServerAgentFilePath) {

		ConfigParams configParams = configurationService.getConfigParams();
		configParams.setFileServerProtocol(fileTransferType);
		configParams.setFileServerPort(fileServerPort);
		configParams.setFileServerHost(fileServerAddress);
		configParams.setFileServerUsername(fileServerUsername);
		configParams.setFileServerPassword(fileServerPassword);
		configParams.setFileServerAgentFilePath(fileServerAgentFilePath);
		
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("fileServerAddress",configParams.getFileServerHost());
		requestData.put("fileServerUsername",configParams.getFileServerUsername());
		requestData.put("fileServerPort",configParams.getFileServerPort());
		requestData.put("fileServerAgentFilePath",configParams.getFileServerAgentFilePath());
		requestData.put("fileTransferType",configParams.getFileServerProtocol());

		ObjectMapper dataMapper = new ObjectMapper();
		String jsonString = null ;
		try {
			jsonString = dataMapper.writeValueAsString(requestData);
		} catch (JsonProcessingException e1) {
			logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
		}
		String log = "File server setting has been updated";
		operationLogService.saveOperationLog(OperationType.UPDATE, log, jsonString.getBytes(), null, null, null);
		
		return configurationService.updateConfigParams(configParams);
	}

	@RequestMapping(method=RequestMethod.POST, value = "/update/emailSettings", produces = MediaType.APPLICATION_JSON_VALUE)
	public ConfigParams updateEmailSettings(
			@RequestParam (value = "emailHost", required = false) String emailHost,
			@RequestParam (value = "emailPort", required = false) Integer emailPort,
			@RequestParam (value = "emailUsername", required = false) String emailUsername,
			@RequestParam (value = "emailPassword", required = false) String emailPassword,
			@RequestParam (value = "smtpAuth", required = false) Boolean smtpAuth,
			@RequestParam (value = "tlsEnabled", required = false) Boolean tlsEnabled) {
		
		ConfigParams configParams = configurationService.getConfigParams();
		configParams.setMailHost(emailHost);
		configParams.setMailPassword(emailPassword);
		configParams.setMailSmtpPort(emailPort);
		configParams.setMailSmtpAuth(smtpAuth);
		configParams.setMailSmtpStartTlsEnable(tlsEnabled);
		configParams.setMailAddress(emailUsername);

		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("emailHost",configParams.getMailHost());
		requestData.put("emailPort",configParams.getMailPassword());
		requestData.put("emailUsername",configParams.getMailSmtpPort());
		requestData.put("smtpAuth",configParams.getMailSmtpStartTlsEnable());
		requestData.put("tlsEnabled",configParams.getMailAddress());

		ObjectMapper dataMapper = new ObjectMapper();
		String jsonString = null ;
		try {
			jsonString = dataMapper.writeValueAsString(requestData);
		} catch (JsonProcessingException e1) {
			logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
		}
		String log = "Mail server setting has been updated";
		operationLogService.saveOperationLog(OperationType.UPDATE, log, jsonString.getBytes(), null, null, null);
		
		return configurationService.updateConfigParams(configParams);
	}
	
	@RequestMapping(method=RequestMethod.POST, value = "/update/otherSettings", produces = MediaType.APPLICATION_JSON_VALUE)
	public ConfigParams updateOtherSettings(@RequestParam (value = "disableLocalUser", required = true) Boolean disableLocalUser,
			@RequestParam (value = "domainType", required = true) DomainType domainType,
			@RequestParam (value = "ahenkRepoAddress", required = true) String ahenkRepoAddress,
			@RequestParam (value = "ahenkRepoKeyAddress", required = true) String ahenkRepoKeyAddress,
			@RequestParam (value = "sudoRoleType", required = true) SudoRoleType sudoRoleType,
			@RequestParam (value = "selectedRegistrationType", required = true) RegistrationTemplateType selectedRegistrationType) {
		
		ConfigParams configParams = configurationService.getConfigParams();
		configParams.setDisableLocalUser(disableLocalUser);
		configParams.setDomainType(domainType);
		configParams.setsudoRoleType(sudoRoleType);
		configParams.setAhenkRepoAddress(ahenkRepoAddress);
		configParams.setAhenkRepoKeyAddress(ahenkRepoKeyAddress);
		configParams.setSelectedRegistrationType(selectedRegistrationType);
		
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("domainType",configParams.getDomainType());
		requestData.put("ahenkRepoAddress",configParams.getAhenkRepoAddress());
		requestData.put("ahenkRepoKeyAddress",configParams.getAhenkRepoKeyAddress());
		requestData.put("sudoRoleType",configParams.getSudoRoleType());
		requestData.put("selectedRegistrationType",configParams.getSelectedRegistrationType());
		requestData.put("disableLocalUser",configParams.getDisableLocalUser());

		ObjectMapper dataMapper = new ObjectMapper();
		String jsonString = null ;
		try {
			jsonString = dataMapper.writeValueAsString(requestData);
		} catch (JsonProcessingException e1) {
			logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
		}
		String log = "Other server setting has been updated";
		operationLogService.saveOperationLog(OperationType.UPDATE, log, jsonString.getBytes(), null, null, null);
		
		return configurationService.updateConfigParams(configParams);
	}

	//add roles to user. 
	@RequestMapping(method=RequestMethod.POST, value = "/editUserRoles", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<LdapEntry>> editUserRoles(@RequestParam (value = "dn", required = true) String dn,
			@RequestParam(value = "roles[]", required=true) String[] roles,
			Authentication authentication) {
		List<LdapEntry> ldapEntries = null;
		try {
			LdapEntry entry = ldapService.getEntryDetail(dn);
			if(entry != null) {
				if(entry.getAttributesMultiValues().get("liderPrivilege") != null) {
					String[] priviliges = entry.getAttributesMultiValues().get("liderPrivilege");
					for (int i = 0; i < priviliges.length; i++) {
						if(priviliges[i].startsWith("ROLE_")) {
							ldapService.updateEntryRemoveAttribute(dn, "liderPrivilege");
						}
					}
					for (int i = 0; i < roles.length; i++) {
						ldapService.updateEntryAddAtribute(dn, "liderPrivilege", roles[i]);
					}
				} else {
					for (int i = 0; i < roles.length; i++) {
						ldapService.updateEntryAddAtribute(dn, "liderPrivilege", roles[i]);
					}
				}
			}
			//if user edited own console roles redirect to logout
			if(AuthenticationService.getDn().equals(dn)) {
				authentication.setAuthenticated(false);
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			} else {
				String filter= "(&(objectClass=pardusAccount)(objectClass=pardusLider)(liderPrivilege=ROLE_USER))";
				ldapEntries  = ldapService.findSubEntries(filter,
						new String[] { "*" }, SearchScope.SUBTREE);
				try {
					Map<String, Object> requestData = new HashMap<String, Object>();
					requestData.put("dn", entry.getDistinguishedName());
					requestData.put("liderPrivilege", roles);
					ObjectMapper dataMapper = new ObjectMapper();
					String jsonString = dataMapper.writeValueAsString(requestData);
					String log = entry.getDistinguishedName()+ " Lider Privileges has been changed";
					operationLogService.saveOperationLog(OperationType.UPDATE, log, jsonString.getBytes(), null, null, null);
				}catch (Exception e) {
					logger.error("Error occured while mapping request data to json. Error: " +  e.getMessage());
				}
				return new ResponseEntity<>(ldapEntries, HttpStatus.OK);
			}
		} catch (LdapException e) {
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping(method=RequestMethod.POST, value = "/deleteConsoleUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<LdapEntry>> deleteConsoleUser(@RequestParam (value = "dn", required = true) String dn,
			Authentication authentication) {
		List<LdapEntry> ldapEntries = null;
		try {
			LdapEntry entry = ldapService.getEntryDetail(dn);
			if(entry != null) {
				if(entry.getAttributesMultiValues().get("liderPrivilege") != null) {
					String[] priviliges = entry.getAttributesMultiValues().get("liderPrivilege");
					for (int i = 0; i < priviliges.length; i++) {
						if(priviliges[i].startsWith("ROLE_")) {
							ldapService.updateEntryRemoveAttributeWithValue(dn, "liderPrivilege", priviliges[i]);
						}
					}
				}
			}
			//if user deleted own console roles redirect to logout
			if(AuthenticationService.getDn().equals(dn)) {
				authentication.setAuthenticated(false);
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			} else {
				String filter= "(&(objectClass=pardusAccount)(objectClass=pardusLider)(liderPrivilege=ROLE_USER))";
				ldapEntries  = ldapService.findSubEntries(filter,
						new String[] { "*" }, SearchScope.SUBTREE);
				

				Map<String, Object> requestData = new HashMap<String, Object>();	
				requestData.put("dn",entry.getDistinguishedName());
				requestData.put("menuRole",entry.getAttributesMultiValues().get("liderPrivilege"));
				ObjectMapper dataMapper = new ObjectMapper();
				String jsonString = null ;
				try {
					jsonString = dataMapper.writeValueAsString(requestData);
				} catch (JsonProcessingException e1) {
					logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
				}
				String log = entry.getName() + " Lider privileges access has been deleted" ;
				operationLogService.saveOperationLog(OperationType.DELETE, log, jsonString.getBytes(), null, null, null);
				
				return new ResponseEntity<>(ldapEntries, HttpStatus.OK);
			}

		} catch (LdapException e) {
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping(method=RequestMethod.GET ,value = "/getRoles", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RoleImpl> getRoles() {
		return roleService.getRoles();
	}

	@RequestMapping(method=RequestMethod.POST ,value = "/saveMenusForRole", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RoleImpl> saveMenusForRole(@RequestBody RoleImpl role) {
		if(!role.getName().equals("ROLE_ADMIN")) {
			roleService.saveRole(role);
			
			Map<String, Object> requestData = new HashMap<String, Object>();
			requestData.put("menuRole",role);
			ObjectMapper dataMapper = new ObjectMapper();
			String jsonString = null ;
			try {
				jsonString = dataMapper.writeValueAsString(requestData);
			} catch (JsonProcessingException e1) {
				logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
			}
			String log = role.getName() + " Menu settings has been chancged" ;
			operationLogService.saveOperationLog(OperationType.UPDATE, log, jsonString.getBytes(), null, null, null);
			
			return roleService.getRoles();
		} else {
			return null;
		}
	}

	@RequestMapping(method=RequestMethod.POST ,value = "/getOLCAccessRules", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<OLCAccessRule> getUsersOLCAccessRules(@RequestParam (value = "dn", required = true) String dn) {
		if(!dn.equals("")) {
			try {
				List<OLCAccessRule> ruleList = ldapService.getSubTreeOLCAccessRules(dn);
				return ruleList;
			} catch (LdapException e) {
				logger.error(e.getMessage());
				return null;
			}
		} else {
			return null;
		}
	}

	@RequestMapping(method=RequestMethod.POST ,value = "/addOLCAccessRule", produces = MediaType.APPLICATION_JSON_VALUE)
	public Boolean addOLCAccessRule(@RequestParam (value = "groupDN", required = true) String groupDN,
			@RequestParam (value = "olcAccessDN", required = true) String olcAccessDN,
			@RequestParam (value = "accessType", required = true) String accessType) {
		if(!groupDN.equals("")) {
			OLCAccessRule rule = new OLCAccessRule();
			rule.setAccessDNType("dn.subtree");
			rule.setAccessDN(olcAccessDN);
			rule.setAssignedDNType("group.exact");
			rule.setAssignedDN(groupDN);
			rule.setAccessType(accessType);
			
			Map<String, Object> requestData = new HashMap<String, Object>();
			requestData.put("olcAccess",rule);
			ObjectMapper dataMapper = new ObjectMapper();
			String jsonString = null ;
			try {
				jsonString = dataMapper.writeValueAsString(requestData);
			} catch (JsonProcessingException e1) {
				logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
			}
			String log = rule.getAssignedDN() + " OLC Access has been added" ;
			operationLogService.saveOperationLog(OperationType.CREATE, log, jsonString.getBytes(), null, null, null);
			
			return ldapService.addOLCAccessRule(rule);
		} else {
			return false;
		}
	}
	
	@RequestMapping(method=RequestMethod.POST ,value = "/deleteOLCAccessRule", produces = MediaType.APPLICATION_JSON_VALUE)
	public Boolean deleteOLCAccessRule(@RequestBody OLCAccessRule rule) 
	{
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("olcAccess",rule);
		ObjectMapper dataMapper = new ObjectMapper();
		String jsonString = null ;
		try {
			jsonString = dataMapper.writeValueAsString(requestData);
		} catch (JsonProcessingException e1) {
			logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
		}
		String log = rule.getAssignedDN() + " OLC Access has been deleted" ;
		operationLogService.saveOperationLog(OperationType.DELETE, log, jsonString.getBytes(), null, null, null);
		
		ldapService.removeOLCAccessRuleWithParents(rule);
		return true;
	}

	/**
	 * 
	 * add console user from settings
	 * edip.yildiz
	 * @param selectedEntry from 
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value = "/addConsoleUserBtn",produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public LdapEntry addConsoleUserBtn(LdapEntry user) {
		try {
			String gidNumber="6000";
			int randomInt = (int)(1000000.0 * Math.random());
			String uidNumber= Integer.toString(randomInt);
			String home="/home/"+user.getUid();

			Map<String, String[]> attributes = new HashMap<String, String[]>();
			attributes.put("objectClass", new String[] { "top", "posixAccount",
					"person","pardusLider","pardusAccount","organizationalPerson","inetOrgPerson"});
			attributes.put("cn", new String[] { user.getCn() });
			attributes.put("mail", new String[] { user.getMail() });
			attributes.put("gidNumber", new String[] { gidNumber });
			attributes.put("homeDirectory", new String[] { home });
			attributes.put("sn", new String[] { user.getSn() });
			attributes.put("uid", new String[] { user.getUid() });
			attributes.put("uidNumber", new String[] { uidNumber });
			attributes.put("loginShell", new String[] { "/bin/bash" });
			attributes.put("userPassword", new String[] { "{ARGON2}" + customPasswordEncoder.encode(user.getUserPassword()) });
			attributes.put("homePostalAddress", new String[] { user.getHomePostalAddress() });
			if(user.getTelephoneNumber()!=null && user.getTelephoneNumber()!="")
				attributes.put("telephoneNumber", new String[] { user.getTelephoneNumber() });

			if(user.getParentName()==null || user.getParentName().equals("")) {
				user.setParentName(configurationService.getUserLdapBaseDn());
			}
			
			String rdn="uid="+user.getUid()+","+user.getParentName();

			ldapService.addEntry(rdn, attributes);
			
			user.setAttributesMultiValues(attributes);
			user.setDistinguishedName(user.getUid());

			logger.info("User created successfully RDN ="+rdn);
			user = ldapService.findSubEntries(rdn, "(objectclass=*)", new String[] {"*"}, SearchScope.OBJECT).get(0);
			
			Map<String, Object> requestData = new HashMap<String, Object>();
			requestData.put("dn",user.getDistinguishedName());
			ObjectMapper dataMapper = new ObjectMapper();
			String jsonString = null ;
			try {
				jsonString = dataMapper.writeValueAsString(requestData);
			} catch (JsonProcessingException e1) {
				logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
			}
			String log = user.getDistinguishedName() + " Console user has been added";
			operationLogService.saveOperationLog(OperationType.CREATE, log, jsonString.getBytes(), null, null, null);
			
			return user;
		} catch (LdapException e) {
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping(value = "/addMemberToGroup")
	public Boolean addMemberToGroup(HttpServletRequest request, LdapEntry selectedEntry) {
		logger.info("Adding {} to group. Group info {} ", selectedEntry.getDistinguishedName(),selectedEntry.getParentName());
		try {
			ldapService.updateEntryAddAtribute(selectedEntry.getParentName(), "member", selectedEntry.getDistinguishedName());
			operationLogService.saveOperationLog(OperationType.CREATE,"Gruba üye eklendi. Üye: "+selectedEntry.getDistinguishedName(),null);
		} catch (LdapException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	
}