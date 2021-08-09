package tr.org.lider.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tr.org.lider.ldap.DNType;
import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.ldap.LdapSearchFilterAttribute;
import tr.org.lider.ldap.SearchFilterEnum;
import tr.org.lider.models.ConfigParams;
import tr.org.lider.services.AgentService;
import tr.org.lider.services.ConfigurationService;

/**
 * 
 * @author <a href="mailto:tuncay.colak@tubitak.gov.tr">Tuncay ÇOLAK</a>
 *
 */

@RestController
@RequestMapping("ldap_login")
public class LdapLoginController {
	
	Logger logger = LoggerFactory.getLogger(LdapLoginController.class);
	
	@Autowired
	ConfigurationService configurationService;
	
	@Autowired
	AgentService agentService;
	
	@Autowired
	private LDAPServiceImpl ldapService;
	
//	get directory server(Active Directory and OpenLDAP) configurations method for ldap-login task
	@RequestMapping(method=RequestMethod.GET, value = "/configurations", produces = MediaType.APPLICATION_JSON_VALUE)
	public ConfigParams getConfigParams() {
		return configurationService.getConfigParams();
	}
	
	//updated user directory domain method by agent dn as null, AD or OpenLDAP
	@RequestMapping(method=RequestMethod.POST ,value = "/update_directory_domain", produces = MediaType.APPLICATION_JSON_VALUE)
	public String changeUserDirectoryDomain(@RequestParam (value = "userDirectoryDomain", required=false) String userDirectoryDomain,
			@RequestParam (value="dn", required=false) String dn){
		try {
			List<LdapSearchFilterAttribute> filterAttributes = new ArrayList<LdapSearchFilterAttribute>();
			filterAttributes.add(new LdapSearchFilterAttribute("entryDN", dn, SearchFilterEnum.EQ));
			List<LdapEntry> selectedEntry = ldapService.search(configurationService.getLdapRootDn(),filterAttributes, new String[] {"*"});
			if (selectedEntry.get(0).getType().equals(DNType.AHENK)) {
				agentService.updateUserDirectoryAgentByJid(selectedEntry.get(0).getUid(), userDirectoryDomain);
//				agentService.updateUserDirectoryAgentByDn(selectedEntry.get(0).getDistinguishedName(), userDirectoryDomain);
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
			return null;
		}
		return dn;
	}
}
