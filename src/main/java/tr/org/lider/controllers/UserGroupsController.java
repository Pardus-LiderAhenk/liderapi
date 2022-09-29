package tr.org.lider.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.ldap.LdapName;
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

import tr.org.lider.entities.OperationType;
import tr.org.lider.ldap.DNType;
import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.ldap.LdapSearchFilterAttribute;
import tr.org.lider.ldap.SearchFilterEnum;
import tr.org.lider.services.ConfigurationService;
import tr.org.lider.services.OperationLogService;


/**
 * Controller for user groups operations
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 * 
 */
@Secured({"ROLE_ADMIN", "ROLE_USER_GROUPS" })
@RestController
@RequestMapping("/lider/user_groups")
public class UserGroupsController {
	Logger logger = LoggerFactory.getLogger(UserGroupsController.class);

	@Autowired
	private OperationLogService operationLogService;
	
	@Autowired
	private LDAPServiceImpl ldapService;
	
	@Autowired
	private ConfigurationService configurationService;
	
	//gets tree of groups of names which just has user members
	@RequestMapping(value = "/getGroups")
	public List<LdapEntry> getLdapUserGroupsTree() {
		List<LdapEntry> result = new ArrayList<LdapEntry>();
		result.add(ldapService.getLdapUsersGroupTree());
		return result;
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
	
//	add user to existing group by member list
	@RequestMapping(method=RequestMethod.POST ,value = "/group/existing", produces = MediaType.APPLICATION_JSON_VALUE)
	public LdapEntry addUserToExistingGroup(@RequestParam(value="groupDN") String groupDN,
			@RequestParam(value = "checkedList[]", required=true) String[] checkedList) {
		LdapEntry entry;
		try {
			//when single dn comes spring boot takes it as multiple arrays
			//so dn must be joined with comma
			//if member dn that will be added to group is cn=agent1,ou=Groups,dn=liderahenk,dc=org
			//spring boot gets this param as array which has size 4
			Boolean checkedArraySizeIsOne = true;
			for (int i = 0; i < checkedList.length; i++) {
				if(checkedList[i].contains(",")) {
					checkedArraySizeIsOne = false;
					break;
				}
			}
			if(checkedArraySizeIsOne ) {
				ldapService.updateEntryAddAtribute(groupDN, "member", String.join(",", checkedList));
			} else {
				for (int i = 0; i < checkedList.length; i++) {
					ldapService.updateEntryAddAtribute(groupDN, "member", checkedList[i]);
				}
			}
			entry = ldapService.getEntryDetail(groupDN);
		} catch (LdapException e) {
			System.out.println("Error occured while adding new group.");
			return null;
		}
		return entry;
	}
	
//	Add user list to existing group by checked node list
	@RequestMapping(method=RequestMethod.POST ,value = "/group/existing/addUser", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> addUsersToExistingGroup(@RequestBody Map<String, String> params) {
		LdapEntry entry;
		ObjectMapper mapper = new ObjectMapper();
		List<LdapEntry> entries = new ArrayList<>();
		try {
			entries = Arrays.asList(mapper.readValue(params.get("checkedEntries"), LdapEntry[].class));
		} catch (JsonProcessingException e) {
			logger.error("Error occured while mapping checked entry list to object");
		}
		List<LdapEntry> users = new ArrayList<>();
		List<LdapEntry> directories = new ArrayList<>();
		
		for (LdapEntry ldapEntry : entries) {
			if(ldapEntry.getType().equals(DNType.USER)) {
				users.add(ldapEntry);
			}
		}
		
		for (LdapEntry ldapEntry : entries) {
			Boolean hasParentChecked = false;
			for (LdapEntry entryTemp : entries) {
				if(ldapEntry.getType().equals(DNType.ORGANIZATIONAL_UNIT) && entryTemp.getType().equals(DNType.ORGANIZATIONAL_UNIT)) {
					if(!ldapEntry.getDistinguishedName().equals(entryTemp.getDistinguishedName()) 
							&& ldapEntry.getDistinguishedName().contains(entryTemp.getDistinguishedName())) {
						hasParentChecked = true;
						break;
					}
				}
			}
			if(ldapEntry.getType().equals(DNType.ORGANIZATIONAL_UNIT)  && !hasParentChecked) {
				directories.add(ldapEntry);
			}
		}

		List<LdapEntry> allUsers = getUsersUnderOUs(directories, users);
		if(allUsers.size() == 0) {
			return new ResponseEntity<String>("Seçili klasörlerde kullanıcı bulunamadı. Lütfen en az bir kullanıcı seçiniz.", HttpStatus.NOT_ACCEPTABLE);
		}
		try {
			String [] allUserDNs = allUsers.stream().map(LdapEntry::getDistinguishedName).toArray(String[]::new);
			for (int i = 0; i < allUserDNs.length; i++) {
				ldapService.updateEntryAddAtribute(params.get("groupDN"), "member", allUserDNs[i]);
			}
			entry = ldapService.getEntryDetail(params.get("groupDN"));
		} catch (LdapException e) {
			logger.error("Error occured while adding new group.");
			return null;
		}
		Map<String, Object> requestData = new HashMap<String, Object>();
		List<String> userAdded = new ArrayList<>();
		
		for (LdapEntry user : entries) {
			if (user.getUid() != null) {
				userAdded.add(user.getUid());
			}
		}
		requestData.put("uid",userAdded);
		ObjectMapper dataMapper = new ObjectMapper();
		String jsonString = "";
		try {
			jsonString = dataMapper.writeValueAsString(requestData);
		} catch (JsonProcessingException e1) {
			logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
		}
		String log = "Users has been added to " + entry.getDistinguishedName();
		operationLogService.saveOperationLog(OperationType.MOVE, log, jsonString.getBytes(), null, null, null);
		
		return new ResponseEntity<LdapEntry>(entry, HttpStatus.OK);
	}
	
	//get members of group
	@RequestMapping(method=RequestMethod.POST ,value = "/group/members", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<LdapEntry> getMembersOfGroup(@RequestParam(value="dn", required=true) String dn) {
		LdapEntry entry = ldapService.getEntryDetail(dn);
		List<LdapEntry> listOfMembers = new ArrayList<>();

		for(String memberDN: entry.getAttributesMultiValues().get("member")) {
			listOfMembers.add(ldapService.getEntryDetail(memberDN));
		}
		return listOfMembers;
	}
	
	//delete member from group
	@RequestMapping(method=RequestMethod.POST ,value = "/delete/group/members", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public LdapEntry deleteMembersOfGroup(@RequestParam(value="dn", required=true) String dn, 
			@RequestParam(value="dnList[]", required=true) List<String> dnList) {
		//when single dn comes spring boot takes it as multiple arrays
		//so dn must be joined with comma
		//if member dn that will be added to group is cn=agent1,ou=Groups,dn=liderahenk,dc=org
		//spring boot gets this param as array which has size 4
		Boolean checkedArraySizeIsOne = true;
		for (int i = 0; i < dnList.size(); i++) {
			if(dnList.get(i).contains(",")) {
				checkedArraySizeIsOne = false;
				break;
			}
		}
		if(checkedArraySizeIsOne) {
			try {
				ldapService.updateEntryRemoveAttributeWithValue(dn, "member", String.join(",", dnList));
			} catch (LdapException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			for (int i = 0; i < dnList.size(); i++) {
				try {
					ldapService.updateEntryRemoveAttributeWithValue(dn, "member", dnList.get(i));
				} catch (LdapException e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("dn",dn);
		requestData.put("uid",dnList.get(0));
		ObjectMapper dataMapper = new ObjectMapper();
		String jsonString = null ;
		try {
			jsonString = dataMapper.writeValueAsString(requestData);
		} catch (JsonProcessingException e1) {
			logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
		}
		String log = dnList.get(0) + " has been deleted from " + dn;
		operationLogService.saveOperationLog(OperationType.DELETE, log, jsonString.getBytes(), null, null, null);
		return ldapService.getEntryDetail(dn);
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
		
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("SourceDN",sourceDN);
		requestData.put("DestinationDN",destinationDN);

		ObjectMapper dataMapper = new ObjectMapper();
		String jsonString = null ;
		try {
			jsonString = dataMapper.writeValueAsString(requestData);
		} catch (JsonProcessingException e1) {
			logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
		}
		String log = "Entry has been moved from " + sourceDN + " to " + destinationDN ;
		operationLogService.saveOperationLog(OperationType.MOVE, log, jsonString.getBytes(), null, null, null);
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
			
			Map<String, Object> requestData = new HashMap<String, Object>();
			requestData.put("oldDN", oldDN);
			requestData.put("newDN", newName);
			ObjectMapper dataMapper = new ObjectMapper();
			String jsonString = dataMapper.writeValueAsString(requestData);
			String log = "Entry name has been changed from " + oldDN + " to " + newName;
			operationLogService.saveOperationLog(OperationType.UPDATE, log, jsonString.getBytes(), null, null, null);
			
			return selectedEntry;
		} catch (Exception e) {
			logger.error("Error occured while mapping request data to json. Error: " +  e.getMessage());
			return null;
		}
	}
	
	@RequestMapping(method=RequestMethod.POST, value = "/addOu",produces = MediaType.APPLICATION_JSON_VALUE)
	public LdapEntry addOu(@RequestBody  LdapEntry selectedEntry) {
		try {
			Map<String, String[]> attributes = new HashMap<String,String[]>();
			attributes.put("objectClass", new String[] {"organizationalUnit", "top", "pardusLider"} );
			attributes.put("ou", new String[] { selectedEntry.getOu() });

			String dn="ou="+selectedEntry.getOu()+","+selectedEntry.getParentName();
			
			ldapService.addEntry(dn, attributes);
			logger.info("OU created successfully RDN =" + dn);
			
			//get full of ou details after creation
			selectedEntry = ldapService.getEntryDetail(dn);
			
			Map<String, Object> requestData = new HashMap<String, Object>();
			requestData.put("dn", selectedEntry.getDistinguishedName());
			ObjectMapper dataMapper = new ObjectMapper();
			String jsonString = dataMapper.writeValueAsString(requestData);
			String log = "Entry has been added to " + selectedEntry.getDistinguishedName();
			operationLogService.saveOperationLog(OperationType.CREATE, log, jsonString.getBytes(), null, null, null);
			
			return selectedEntry;
		} catch (Exception e) {
			logger.error("Error occured while mapping request data to json. Error: " +  e.getMessage());
			return null;
		}
	}
	
	@RequestMapping(method=RequestMethod.POST, value = "/deleteEntry")
	public ResponseEntity<?> deleteEntry(@RequestParam(value = "dn") String dn) {
		try {
			if(dn.equals("cn=adminGroups," + configurationService.getUserGroupLdapBaseDn())) {
				return new ResponseEntity<String[]>(new String[] {"Admin grubu silinemez!"}, HttpStatus.CONFLICT);
			}
			if(dn.contains(configurationService.getUserGroupLdapBaseDn()) && dn.length() > configurationService.getUserGroupLdapBaseDn().length() ) {
				ldapService.updateOLCAccessRulesAfterEntryDelete(dn);
				ldapService.deleteNodes(ldapService.getOuAndOuSubTreeDetail(dn));
				

				Map<String, Object> requestData = new HashMap<String, Object>();
				requestData.put("dn", dn);
				ObjectMapper dataMapper = new ObjectMapper();
				String jsonString = dataMapper.writeValueAsString(requestData);
				String log = "Entry name has been deleted " + dn;
				operationLogService.saveOperationLog(OperationType.DELETE, log, jsonString.getBytes(), null, null, null);
				
				return new ResponseEntity<String[]>(new String[] {"Grup başarıyla silindi"}, HttpStatus.OK);
			} else {
				return new ResponseEntity<String[]>(new String[] {"Hata oluştu"}, HttpStatus.EXPECTATION_FAILED);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String[]>(new String[] {"Hata oluştu"}, HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	//add new group and add selected agents
	@RequestMapping(method=RequestMethod.POST ,value = "/createNewGroup", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createNewUserGroup(@RequestBody Map<String, String> params) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String selectedOUDN = params.get("selectedOUDN");
		String groupName =  params.get("groupName");
		//String[] checkedList = (String[]) Arrays.asList(mapper.readValue(params.get("checkedEntries"), String.class)).toArray();
		
		List<LdapEntry> entries = new ArrayList<>();
		try {
			entries = Arrays.asList(mapper.readValue(params.get("checkedEntries"), LdapEntry[].class));
		} catch (JsonProcessingException e) {
			logger.error("Error occured while mapping checked entry list to object");
		}
		String newGroupDN = "";
		//to return newly added entry with its details
		LdapEntry entry;
		if(selectedOUDN == null || selectedOUDN.equals("")) {
			newGroupDN = "cn=" +  groupName +","+ configurationService.getUserGroupLdapBaseDn();
		} else {
			newGroupDN = "cn=" +  groupName +","+ selectedOUDN;
		}
		
		List<LdapEntry> users = new ArrayList<>();
		List<LdapEntry> directories = new ArrayList<>();
		
		for (LdapEntry ldapEntry : entries) {
			if(ldapEntry.getType().equals(DNType.USER)) {
				users.add(ldapEntry);
			}
		}
		for (LdapEntry ldapEntry : entries) {
			Boolean hasParentChecked = false;
			for (LdapEntry entryTemp : entries) {
				if(ldapEntry.getType().equals(DNType.ORGANIZATIONAL_UNIT) && entryTemp.getType().equals(DNType.ORGANIZATIONAL_UNIT)) {
					if(!ldapEntry.getDistinguishedName().equals(entryTemp.getDistinguishedName()) 
							&& ldapEntry.getDistinguishedName().contains(entryTemp.getDistinguishedName())) {
						hasParentChecked = true;
						break;
					}
				}
			}
			if(ldapEntry.getType().equals(DNType.ORGANIZATIONAL_UNIT)  && !hasParentChecked) {
				directories.add(ldapEntry);
			}
		}
		List<LdapEntry> allUsers = getUsersUnderOUs(directories, users);
		if(allUsers.size() == 0) {
			return new ResponseEntity<String>("Seçili klasörlerde kullanıcı bulunamadı. Lütfen en az bir kullanıcı seçiniz.", HttpStatus.NOT_ACCEPTABLE);
		}
		Map<String, String[]> attributes = new HashMap<String,String[]>();
		attributes.put("objectClass", new String[] {"groupOfNames", "top", "pardusLider"} );
		attributes.put("liderGroupType", new String[] {"USER"} );
		
		try {
			String [] allUserDNs = allUsers.stream().map(LdapEntry::getDistinguishedName).toArray(String[]::new);
			attributes.put("member", allUserDNs);
			ldapService.addEntry(newGroupDN , attributes);
			entry = ldapService.getEntryDetail(newGroupDN);
		} catch (LdapException e) {
			logger.error("Error occured while adding new group.");
			return null;
		}
		
		Map<String, Object> requestData = new HashMap<String, Object>();
		List<String> userAdded = new ArrayList<>();
		
		for (LdapEntry user : entries) {
			if (user.getUid() != null) {
				userAdded.add(user.getUid());
			}
		}
		requestData.put("uid",userAdded);
		ObjectMapper dataMapper = new ObjectMapper();
		String jsonString = null;
		try {
			jsonString = dataMapper.writeValueAsString(requestData);
		} catch (JsonProcessingException e1) {
			logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
		}
		String log = "New user group has been created " + entry.getDistinguishedName();
		operationLogService.saveOperationLog(OperationType.CREATE, log, jsonString.getBytes(), null, null, null);
		
//		try {
//			//when single dn comes spring boot takes it as multiple arrays
//			//so dn must be joined with comma
//			//if member dn that will be added to group is cn=user1,ou=Groups,dn=liderahenk,dc=org
//			//spring boot gets this param as array which has size 4
//
//			String[] strings = entries.stream().map(x -> x.getDistinguishedName()).
//	                   toArray(String[]::new);
//			attributes.put("member", strings );
//			
//			ldapService.addEntry(newGroupDN , attributes);
//			entry = ldapService.getEntryDetail(newGroupDN);
//		} catch (LdapException e) {
//			System.out.println("Error occured while adding new group.");
//			return null;
//		}
		return new ResponseEntity<LdapEntry>(entry, HttpStatus.OK);
	}
	
	public List<LdapEntry> getUsersUnderOUs(List<LdapEntry> directories, List<LdapEntry> users) {
		for (LdapEntry ldapEntry : directories) {
			try {
				List<LdapEntry> retList = ldapService.findSubEntries(ldapEntry.getDistinguishedName(), "(objectclass=pardusAccount)", new String[] { "*" }, SearchScope.SUBTREE);
				for (LdapEntry ldapEntry2 : retList) {
					boolean isExist=false;
					for (LdapEntry ldapEntryUser : users) {
						if(ldapEntry2.getEntryUUID().equals(ldapEntryUser.getEntryUUID())) {
							isExist=true;
							break;
						}
					}
					if(!isExist) {
						users.add(ldapEntry2);
					}
				}
			} catch (LdapException e) {
				e.printStackTrace();
			}
		}
		return users;
	}
}
