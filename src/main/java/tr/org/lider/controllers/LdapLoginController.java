package tr.org.lider.controllers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tr.org.lider.entities.PluginTask;
import tr.org.lider.ldap.DNType;
import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.ldap.LdapSearchFilterAttribute;
import tr.org.lider.ldap.SearchFilterEnum;
import tr.org.lider.models.ConfigParams;
import tr.org.lider.services.AgentService;
import tr.org.lider.services.ConfigurationService;
import tr.org.lider.services.TaskService;
import tr.org.lider.utils.IRestResponse;

/**
 * this controller for ldap-login management as OpenLDAP or AD 
 * @author <a href="mailto:tuncay.colak@tubitak.gov.tr">Tuncay ÇOLAK</a>
 *
 */

@Secured({"ROLE_ADMIN", "ROLE_COMPUTERS" })
@RestController
@RequestMapping("/ldapLogin")
public class LdapLoginController {
	
	Logger logger = LoggerFactory.getLogger(LdapLoginController.class);
	
	@Autowired
	ConfigurationService configurationService;
	
	@Autowired
	AgentService agentService;
	
	@Autowired
	private LDAPServiceImpl ldapService;
	
	@Autowired
	public TaskService taskService;
	
//	get directory server(Active Directory and OpenLDAP) configurations method for ldap-login task
	@RequestMapping(method=RequestMethod.GET, value = "/configurations", produces = MediaType.APPLICATION_JSON_VALUE)
	public HashMap<String, Object> getConfigParams() {
		ConfigParams configParams = configurationService.getConfigParams();
		HashMap<String, Object> configMap = new HashMap<String, Object>();
		configMap.put("ldapRootDn", configParams.getLdapRootDn());
		configMap.put("ldapServer", configParams.getLdapServer());
		configMap.put("adDomainName", configParams.getAdDomainName());
		configMap.put("adIpAddress", configParams.getAdIpAddress());
		configMap.put("adHostName", configParams.getAdHostName());
		configMap.put("adAdminUserName", configParams.getAdAdminUserName());
		configMap.put("disableLocalUser", configParams.getDisableLocalUser());
		configMap.put("allowDynamicDNSUpdate", configParams.getAllowDynamicDNSUpdate());
		return configMap;
	}
	
	//updated user directory domain method by agent dn as null, AD or OpenLDAP
	@RequestMapping(method=RequestMethod.POST ,value = "/updateDirectoryDomain", produces = MediaType.APPLICATION_JSON_VALUE)
	public boolean changeUserDirectoryDomain(@RequestParam (value = "userDirectoryDomain", required=false) String userDirectoryDomain,
			@RequestParam (value="dn", required=false) String dn){
		try {
			List<LdapSearchFilterAttribute> filterAttributes = new ArrayList<LdapSearchFilterAttribute>();
			filterAttributes.add(new LdapSearchFilterAttribute("entryDN", dn, SearchFilterEnum.EQ));
			List<LdapEntry> selectedEntry = ldapService.search(configurationService.getLdapRootDn(),filterAttributes, new String[] {"*"});
			if (selectedEntry.get(0).getType().equals(DNType.AHENK)) {
				agentService.updateUserDirectoryAgentByJid(selectedEntry.get(0).getUid(), userDirectoryDomain);
			}
			if (selectedEntry.get(0).getType().equals(DNType.GROUP)) {
				String[] members= selectedEntry.get(0).getAttributesMultiValues().get("member");
				for (int i = 0; i < members.length; i++) {
					String memberDn = members[i];
					agentService.updateUserDirectoryAgentByDn(memberDn, userDirectoryDomain);
				}
			}
		} catch (LdapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
//	This method is only for ldap-login plugin
	@RequestMapping(value = "/task/execute", method = { RequestMethod.POST })
	public IRestResponse executeTask(@RequestBody PluginTask requestBody, HttpServletRequest request)
			throws UnsupportedEncodingException {
		if (requestBody.getPlugin().getName().equals("ldap-login")) {
			if (requestBody.getCommandId().equals("EXECUTE_AD_LOGIN")) {
				ConfigParams configParams = configurationService.getConfigParams();
				Map<String, Object> parameterMap = requestBody.getParameterMap();
				parameterMap.put("ad_username", configParams.getAdAdminUserName());
				parameterMap.put("admin_password", configParams.getAdAdminPassword());
				parameterMap.put("ad_port", configParams.getAdPort());
				requestBody.setParameterMap(parameterMap);
			}
			IRestResponse restResponse = taskService.execute(requestBody);
			logger.debug("Completed processing request for ldap-login");
			return restResponse;
		} else {
			return null;
		}
	}
}
