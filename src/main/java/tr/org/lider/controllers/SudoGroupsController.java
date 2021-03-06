package tr.org.lider.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.servlet.http.HttpServletRequest;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.ldap.LdapSearchFilterAttribute;
import tr.org.lider.ldap.SearchFilterEnum;
import tr.org.lider.services.ConfigurationService;

/**
 * Controller for sudo groups operations
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 * 
 */
@Secured({"ROLE_ADMIN", "ROLE_SUDO_GROUPS" })
@RestController
@RequestMapping("/lider/sudo_groups")
public class SudoGroupsController {

	Logger logger = LoggerFactory.getLogger(SudoGroupsController.class);

	@Autowired
	private LDAPServiceImpl ldapService;
	
	@Autowired
	private ConfigurationService configurationService;
	
	
	@RequestMapping(value = "/getGroups")
	public List<LdapEntry> getSudoGroups() {
		List<LdapEntry> retList = new ArrayList<LdapEntry>();
		retList.add(ldapService.getLdapSudoGroupsTree());
		return retList;
	}
	
	@RequestMapping(value = "/getOuDetails")
	public List<LdapEntry> getOuDetails(LdapEntry selectedEntry) {
		List<LdapEntry> subEntries = null;
		try {
			subEntries = ldapService.findSubEntries(selectedEntry.getUid(), "(objectclass=*)",
					new String[] { "*" }, SearchScope.ONELEVEL);
		} catch (LdapException e) {
			e.printStackTrace();
		}
		Collections.sort(subEntries);
		selectedEntry.setChildEntries(subEntries);
		return subEntries;
	}
	
	@RequestMapping(method=RequestMethod.POST, value = "/addOu",produces = MediaType.APPLICATION_JSON_VALUE)
	public LdapEntry addOu(@RequestBody LdapEntry selectedEntry) {
		try {
			Map<String, String[]> attributes = new HashMap<String,String[]>();
			attributes.put("objectClass", new String[] {"organizationalUnit", "top", "pardusLider"} );
			attributes.put("ou", new String[] { selectedEntry.getOu() });

			String dn="ou="+selectedEntry.getOu()+","+selectedEntry.getParentName();
			
			ldapService.addEntry(dn, attributes);
			logger.info("OU created successfully RDN ="+dn);
			
			//get full of ou details after creation
			selectedEntry = ldapService.getEntryDetail(dn);
			
			return selectedEntry;
		} catch (LdapException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@RequestMapping(method=RequestMethod.POST, value = "/deleteEntry")
	public Boolean deleteEntry(@RequestParam(value = "dn") String dn) {
		try {
			if(dn != configurationService.getAgentLdapBaseDn()) {
				ldapService.updateOLCAccessRulesAfterEntryDelete(dn);
				ldapService.deleteNodes(ldapService.getOuAndOuSubTreeDetail(dn));
				return true;
			} else {
				return false;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@RequestMapping(method=RequestMethod.POST ,value = "/move/entry", produces = MediaType.APPLICATION_JSON_VALUE)
	public Boolean moveEntry(@RequestParam(value="sourceDN", required=true) String sourceDN,
			@RequestParam(value="destinationDN", required=true) String destinationDN) {
		try {
			ldapService.moveEntry(sourceDN, destinationDN);
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	@RequestMapping(method=RequestMethod.POST ,value = "/rename/entry", produces = MediaType.APPLICATION_JSON_VALUE)
	public LdapEntry renameEntry(@RequestParam(value="oldDN", required=true) String oldDN,
			@RequestParam(value="newName", required=true) String newName) {
		try {
			ldapService.renameEntry(oldDN, newName);
			String newEntryDN = newName + ",";
			LdapName dn = new LdapName(oldDN);
			for (int i = dn.size()-2; 0 <= i; i--) {
				newEntryDN += dn.get(i);
				if(i>0) {
					newEntryDN += ",";
				}
			}
			LdapEntry selectedEntry = ldapService.getEntryDetail(newEntryDN);
			
			return selectedEntry;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@RequestMapping(value = "/getUsers")
	public List<LdapEntry> getUsers() {
		List<LdapEntry> retList = new ArrayList<LdapEntry>();
		retList.add(ldapService.getLdapUserTree());
		return retList;
	}
	
	/**
	 * get users under sent ORGANIZATIONAL_UNIT 
	 * @param selectedEntryArr
	 * @return
	 */
	@RequestMapping(value = "/getUsersUnderOU", method = { RequestMethod.POST })
	public List<LdapEntry> getUsersUnderOU(HttpServletRequest request,Model model, @RequestBody LdapEntry[] selectedEntryArr) {
		List<LdapEntry> userList=new ArrayList<>();
		for (LdapEntry ldapEntry : selectedEntryArr) {
			List<LdapSearchFilterAttribute> filterAttributes = new ArrayList<LdapSearchFilterAttribute>();
			LdapSearchFilterAttribute fAttr = new LdapSearchFilterAttribute("objectClass", "pardusAccount",	SearchFilterEnum.EQ);
			filterAttributes.add(fAttr);
			try {
				List<LdapEntry> retList=ldapService.findSubEntries(ldapEntry.getDistinguishedName(), "(objectclass=pardusAccount)", new String[] { "*" }, SearchScope.SUBTREE);
				for (LdapEntry ldapEntry2 : retList) {
					boolean isExist=false;
					for (LdapEntry ldapEntryAhenk : userList) {
						if(ldapEntry2.getEntryUUID().equals(ldapEntryAhenk.getEntryUUID())) {
							isExist=true;
							break;
						}
					}
					if(!isExist) {
						userList.add(ldapEntry2);
					}
				}
			} catch (LdapException e) {
				e.printStackTrace();
			}
		}
		return userList;
	}
	
	//add new group and add selected attributes
	@RequestMapping(method=RequestMethod.POST ,value = "/createSudoGroup", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public LdapEntry createSudoGroup(@RequestBody(required=false) String body) {
		
		try {
			Map<String, Object> readValue = new ObjectMapper().readValue(body, Map.class);
			
			String groupName = (String) readValue.get("groupName");
			String selectedOUDN = (String) readValue.get("selectedOUDN");
			List<String> sudoCommandList = (ArrayList<String>) readValue.get("sudoCommandList");
			List<String> sudoUserList = (ArrayList<String>) readValue.get("sudoUserList");
			List<String> sudoHostList = (ArrayList<String>) readValue.get("sudoHostList");
			
			String newGroupDN = "";
			//to return newly added entry with its details
			LdapEntry entry=null;
			Map<String, String[]> attributes = new HashMap<String,String[]>();
			attributes.put("objectClass", new String[] {"top", "sudoRole"} );
			try {
				//add sudoUser attributes 
				if(sudoUserList != null) {
					attributes.put("sudoUser", sudoUserList.toArray(new String[sudoUserList.size()]));
				}
				//add sudoHost attributes 
				if(sudoHostList != null) {
					attributes.put("sudoHost", sudoHostList.toArray(new String[sudoHostList.size()]));
				}
				//add sudoCommand attributes 
				if(sudoCommandList != null) {
					attributes.put("sudoCommand", sudoCommandList.toArray(new String[sudoCommandList.size()]));
				}
				newGroupDN = "cn=" +  groupName +","+ selectedOUDN;
				System.out.println(attributes);
				ldapService.addEntry(newGroupDN , attributes);
				entry = ldapService.getEntryDetail(newGroupDN);
				return entry;
			} catch (LdapException e) {
				logger.error("Error occured while adding new group.");
				return null;
			}
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		

		return null;
		
	}
	
	//edit sudo group
	@RequestMapping(method=RequestMethod.POST ,value = "/editSudoGroup", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public LdapEntry editSudoGroup(@RequestBody(required=false) String body) {
		try {
			Map<String, Object> readValue = new ObjectMapper().readValue(body, Map.class);
			
			String newName = (String) readValue.get("newName");
			String selectedDN = (String) readValue.get("selectedDN");
			List<String> sudoCommandList = (ArrayList<String>) readValue.get("sudoCommandList");
			List<String> sudoUserList = (ArrayList<String>) readValue.get("sudoUserList");
			List<String> sudoHostList = (ArrayList<String>) readValue.get("sudoHostList");
			
			LdapEntry entry=null;
			ldapService.updateEntryRemoveAttribute(selectedDN, "sudoCommand");
			ldapService.updateEntryRemoveAttribute(selectedDN, "sudoHost");
			ldapService.updateEntryRemoveAttribute(selectedDN, "sudoUser");
			if(sudoHostList != null) {
				for (String value : sudoHostList) {
					ldapService.updateEntryAddAtribute(selectedDN, "sudoHost", value);
				}
			}
			if(sudoCommandList != null) {
				for (String value : sudoCommandList) {
					ldapService.updateEntryAddAtribute(selectedDN, "sudoCommand", value);
				}
			}
			if(sudoUserList != null) {
				for (String value : sudoUserList) {
					ldapService.updateEntryAddAtribute(selectedDN, "sudoUser", value);
				}
			}
			//if name has been edited edit name
			ldapService.renameEntry(selectedDN, newName);
			String newEntryDN = newName + ",";
			LdapName dn = new LdapName(selectedDN);
			for (int i = dn.size()-2; 0 <= i; i--) {
				newEntryDN += dn.get(i);
				if(i>0) {
					newEntryDN += ",";
				}
			}
			entry = ldapService.getEntryDetail(newEntryDN);
			return entry;
		} catch (LdapException e) {
			e.printStackTrace();
			return null;
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	//delete sudoUser from sudo groups
	@RequestMapping(method=RequestMethod.POST ,value = "/delete/sudo/user", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public LdapEntry deleteUserOfSudoGroup(@RequestParam(value="dn", required=true) String dn, 
			@RequestParam(value="uid", required=true) String uid) {
		try {
			ldapService.updateEntryRemoveAttributeWithValue(dn, "sudoUser", uid);
		} catch (LdapException e) {
			e.printStackTrace();
			return null;
		}
		return ldapService.getEntryDetail(dn);
	}
	
}
