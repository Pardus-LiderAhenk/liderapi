package tr.org.lider.controllers;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.services.CommandService;
import tr.org.lider.services.ConfigurationService;

@RestController
@RequestMapping(value = "/dashboard")
public class DashboardController {
	
	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private LDAPServiceImpl ldapService;
	
	@Autowired
	private CommandService commandService;
	
	@Autowired
	private Environment env;
	
	
	@RequestMapping(value = "/info", method = RequestMethod.POST)
	public Map<String, Object> getDashboardInfo() {
		Map<String, Object> model = new HashedMap();
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
			e.printStackTrace();
		}
		model.put("totalComputerNumber", countOfComputers);
		model.put("totalUserNumber", countOfLDAPUsers);
		//sent task total number
		model.put("totalSentTaskNumber", commandService.getTotalCountOfSentTasks());
		
		return model;
	}

}
