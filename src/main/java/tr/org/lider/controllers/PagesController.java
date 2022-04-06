package tr.org.lider.controllers;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.services.CommandService;
import tr.org.lider.services.ConfigurationService;


/**
 * 
 * Getting inner page for menu items.. When menu items clicked dynamic content of div rendered with inner page. 
 * @author M. Edip YILDIZ
 *
 */
@RestController
@RequestMapping("/lider/pages")
public class PagesController {
	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private LDAPServiceImpl ldapService;
	
	@Autowired
	private CommandService commandService;
	
	@Autowired
	private Environment env;
	
	
	@RequestMapping(value="/getInnerHtmlPage", method = {RequestMethod.POST })
	public Map<String, Object> getInnerHtmlPage(String innerPage) {
		
		Map<String, Object> model = new HashedMap();
		
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Boolean userAuthenticated =  null != authentication && !("anonymousUser").equals(authentication.getName());
        if(userAuthenticated) {
//        	ModelAndView modelAndView = new ModelAndView();
//        	modelAndView.setViewName(innerPage);
        	if(innerPage.equals("dashboard")) {
        		
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
        			e.printStackTrace();
        		}
        		model.put("totalComputerNumber", countOfComputers);
        		model.put("totalUserNumber", countOfLDAPUsers);
        		//sent task total number
        		model.put("totalSentTaskNumber", commandService.getTotalCountOfSentTasks());
        	}
        	/**
        	 * delete and update operations enable by application.properties values
        	 */
        	else if(innerPage.equals("directory-manager")) {
        		String enableDelete4Directory = env.getProperty("lider.enableDelete4Directory");
        		model.put("enableDeleteUpdate", enableDelete4Directory);
        		model.put("domainType", configurationService.getDomainType());
        	}
        	return model;
        } else {
        	
        	return model;
        }
		
	}
}
 