package tr.org.lider.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.entities.AgentImpl;
import tr.org.lider.entities.AgentPropertyImpl;
import tr.org.lider.entities.OperationType;
import tr.org.lider.entities.PluginImpl;
import tr.org.lider.entities.PluginTask;
import tr.org.lider.ldap.DNType;
import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.ldap.LdapSearchFilterAttribute;
import tr.org.lider.ldap.SearchFilterEnum;
import tr.org.lider.message.service.IMessagingService;
import tr.org.lider.repositories.AgentRepository;
import tr.org.lider.services.AgentService;
import tr.org.lider.services.CommandService;
import tr.org.lider.services.ConfigurationService;
import tr.org.lider.services.OperationLogService;
import tr.org.lider.services.PluginService;
import tr.org.lider.services.TaskService;
import tr.org.lider.utils.IRestResponse;;


/**
 * Controller for computer url requests 
 */
@RestController
@RequestMapping("/api/lider/computer")
@Tag(name="Computer Management",description = "Computer Management Rest Service")
public class ComputerController {

	Logger logger = LoggerFactory.getLogger(ComputerController.class);

	@Autowired
	private LDAPServiceImpl ldapService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private IMessagingService messagingService;

	@Autowired
	private AgentService agentService;

	@Autowired
	private CommandService commandService;

	@Autowired
	private PluginService pluginService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private AgentRepository agentRepository;
	
	@Autowired
	private OperationLogService operationLogService;

	
	@Operation(summary = "Get computer features list", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns the computer specs list."),
			  @ApiResponse(responseCode = "417", description = "Could not get computer specs list.Unexpected error occured", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/computers")
	public ResponseEntity<List<LdapEntry>>  getComputers() {
		List<LdapEntry> retList = new ArrayList<LdapEntry>();
		retList.add(ldapService.getLdapComputersTree());
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(retList);
	}

	
	@Operation(summary = "Get organization unit details list", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Retrieved the ou details list."),
			  @ApiResponse(responseCode = "417", description = "Could not ou details list. Unexpected error occured", 
			    content = @Content(schema = @Schema(implementation = String.class))) 
	})
	@PostMapping(value = "/ou-details")
	public ResponseEntity<List<LdapEntry>>  task(LdapEntry selectedEntry) {
		List<LdapEntry> subEntries = null;
		try {
			subEntries = ldapService.findSubEntries(selectedEntry.getUid(), "(objectclass=*)",
					new String[] { "*" }, SearchScope.ONELEVEL);
		} catch (LdapException e) {
			e.printStackTrace();
			HttpHeaders headers = new HttpHeaders();
			return ResponseEntity.
					status(HttpStatus.EXPECTATION_FAILED).
					headers(headers)
					.build();
		}
		Collections.sort(subEntries);
		selectedEntry.setChildEntries(subEntries);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(subEntries);
	}

	@Operation(summary = "Get organization unit list", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns the ou list "),
			  @ApiResponse(responseCode = "417", description = "Could not ou list. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@GetMapping(value = "/ou")
	public ResponseEntity<List<LdapEntry>>  getOu(LdapEntry selectedEntry) {
		List<LdapEntry> subEntries = null;
		try {
			subEntries = ldapService.findSubEntries(selectedEntry.getUid(), "(&(objectclass=organizationalUnit))",
					new String[] { "*" }, SearchScope.ONELEVEL);
		} catch (LdapException e) {
			e.printStackTrace();
			HttpHeaders headers = new HttpHeaders();
			return ResponseEntity.
					status(HttpStatus.EXPECTATION_FAILED).
					headers(headers)
					.build();
		}
		Collections.sort(subEntries);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(subEntries);

	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	@Operation(summary = "Getting search entry list ", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns search entry list"),
			  @ApiResponse(responseCode = "417", description = "Could not get search entry list", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/search-entry",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<LdapEntry>>  searchEntry(
			@RequestParam(value="searchDn", required=true) String searchDn,
			@RequestParam(value="key", required=true) String key, 
			@RequestParam(value="value", required=true) String value) {

		List<LdapEntry> results=null;
		try {
			if(searchDn.equals("")) {
				searchDn=configurationService.getLdapRootDn();
			}
			List<LdapSearchFilterAttribute> filterAttributes = new ArrayList<LdapSearchFilterAttribute>();
			filterAttributes.add(new LdapSearchFilterAttribute(key, value, SearchFilterEnum.EQ));
			results = ldapService.search(searchDn,filterAttributes, new String[] {"*"});
		} catch (LdapException e) {
			e.printStackTrace();
			HttpHeaders headers = new HttpHeaders();
			return ResponseEntity
					.status(HttpStatus.EXPECTATION_FAILED)
					.headers(headers)
					.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(results);
		}

	@Operation(summary = "Getting ahenk list", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = ""),
			  @ApiResponse(responseCode = "417", description = "Could not ahenk list", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/online-ahenks")
	public ResponseEntity<String>  getOnlyOnlineAhenks(@RequestBody LdapEntry[] selectedEntryArr) {
		List<LdapEntry> ahenkList=new ArrayList<>();
		for (LdapEntry ldapEntry : selectedEntryArr) {
			List<LdapSearchFilterAttribute> filterAttributes = new ArrayList<LdapSearchFilterAttribute>();
			LdapSearchFilterAttribute fAttr = new LdapSearchFilterAttribute("objectClass", "pardusDevice",	SearchFilterEnum.EQ);
			filterAttributes.add(fAttr);
			try {
				List<LdapEntry> retList=ldapService.findSubEntries(ldapEntry.getDistinguishedName(), "(objectclass=pardusDevice)", new String[] { "*" }, SearchScope.SUBTREE);
				for (LdapEntry ldapEntry2 : retList) {
					boolean isExist=false;
					for (LdapEntry ldapEntryAhenk : ahenkList) {
						if(ldapEntry2.getEntryUUID().equals(ldapEntryAhenk.getEntryUUID())) {
							isExist=true;
							break;
						}
					}
					if(!isExist && messagingService.isRecipientOnline(ldapEntry2.getUid())) {
						ahenkList.add(ldapEntry2);
					}
				}
			} catch (LdapException e) {
				e.printStackTrace();
				HttpHeaders headers = new HttpHeaders();
				return ResponseEntity.
						status(HttpStatus.EXPECTATION_FAILED).
						headers(headers)
						.build();
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		String ret = null;
		try {
			ret = mapper.writeValueAsString(ahenkList);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			HttpHeaders headers = new HttpHeaders();
			return ResponseEntity.
					status(HttpStatus.EXPECTATION_FAILED).
					headers(headers)
					.build();
			
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(ret);
				
	}

	//add new group and add selected agents
	@Operation(summary = "Getting new agent group", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "The new agent group has been created."),
			  @ApiResponse(responseCode = "417", description = "Could not create new agent group.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/create-new-agent-group", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LdapEntry> createNewAgentGroup(@RequestParam(value = "selectedOUDN", required=false) String selectedOUDN,
			@RequestParam(value = "groupName", required=true) String groupName,
			@RequestParam(value = "checkedList[]", required=true) String[] checkedList) {
		String newGroupDN = "";
		//to return newly added entry with its details
		LdapEntry entry;
		if(selectedOUDN == null || selectedOUDN.equals("")) {
			newGroupDN = "cn=" +  groupName +","+ configurationService.getAhenkGroupLdapBaseDn();
		} else {
			newGroupDN = "cn=" +  groupName +","+ selectedOUDN;
		}
		Map<String, String[]> attributes = new HashMap<String,String[]>();
		attributes.put("objectClass", new String[] {"groupOfNames", "top", "pardusLider"} );
		attributes.put("liderGroupType", new String[] {"AHENK"} );
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
				attributes.put("member", new String[] {String.join(",", checkedList)} );
			} else {
				attributes.put("member", checkedList );
			}
			ldapService.addEntry(newGroupDN , attributes);
			entry = ldapService.getEntryDetail(newGroupDN);
		} catch (LdapException e) {
			System.out.println("Error occured while adding new group.");
			HttpHeaders headers = new HttpHeaders();
			return ResponseEntity.
					status(HttpStatus.EXPECTATION_FAILED).
					headers(headers)
					.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(entry);
}

	//add agents to existing group
	@Operation(summary = "Adding agent to existing group", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Added agent to existing group"),
			  @ApiResponse(responseCode = "404", description = "The group id not found", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/group/existing", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LdapEntry>  addAgentsToExistingGroup(@RequestParam(value="groupDN") String groupDN,
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
			HttpHeaders headers = new HttpHeaders();
			return ResponseEntity.
					status(HttpStatus.EXPECTATION_FAILED).
					headers(headers)
					.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(entry);
	}


	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	@Operation(summary = "Search online entries", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = ""),
			  @ApiResponse(responseCode = "404", description = "Could not found online entries.Not found", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/search-online-entries", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<LdapEntry>> searchOnlineEntries(
			@RequestParam(value="searchDn", required=true) String searchDn) {

		List<LdapEntry> results=null;
		try {
			if(searchDn.equals("")) {
				searchDn=configurationService.getLdapRootDn();
			}
			//			List<LdapSearchFilterAttribute> filterAttributes = new ArrayList<LdapSearchFilterAttribute>();
			//			filterAttributes.add(new LdapSearchFilterAttribute(key, value, SearchFilterEnum.EQ));
			List<LdapEntry> res = ldapService.findSubEntries(searchDn, "(objectclass=pardusDevice)",new String[] { "*" }, SearchScope.SUBTREE);
			results= new ArrayList<>();
			for (LdapEntry ldapEntry : res) {
				if(ldapEntry.isOnline()) {
					results.add(ldapEntry);
				}
			}
		} catch (LdapException e) {
			e.printStackTrace();
			HttpHeaders headers = new HttpHeaders();
			return ResponseEntity.
					status(HttpStatus.EXPECTATION_FAILED).
					headers(headers)
					.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(results);
	}

	
	@Operation(summary = "Getting agent list", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns agents list"),
			  @ApiResponse(responseCode = "417", description = "Could not retrieved agent list. Unexpected error occured", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/agent-list-size", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LdapEntry>  getAgentList(@RequestParam(value="searchDn") String searchDn) {
		LdapEntry returnLdapEntry=null;
		List<LdapEntry> retList = new ArrayList<LdapEntry>();
		List<LdapEntry> onlineRetList = new ArrayList<LdapEntry>();
		try {
			
			if (searchDn.equals("agents")) {
				searchDn = configurationService.getAgentLdapBaseDn();
			}
			returnLdapEntry=new LdapEntry();
			retList = ldapService.findSubEntries(searchDn, "(objectclass=pardusDevice)", new String[] { "*" }, SearchScope.SUBTREE);

			for (LdapEntry ldapEntry : retList) {
				if(ldapEntry.isOnline()) {
					onlineRetList.add(ldapEntry);
				}
			}

			returnLdapEntry.setOnlineAgentListSize(onlineRetList.size());
			returnLdapEntry.setAgentListSize(retList.size());
		} catch (LdapException e) {
			e.printStackTrace();
			HttpHeaders headers = new HttpHeaders();
			return ResponseEntity.
					status(HttpStatus.EXPECTATION_FAILED).
					headers(headers)
					.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(returnLdapEntry);
				
	}

	@Operation(summary = "Move agent", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Moved to agent"),
			  @ApiResponse(responseCode = "417", description = "Could not move agent. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/move/agent", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LdapEntry>  moveEntry(@RequestParam(value="sourceDN", required=true) String sourceDN,
			@RequestParam(value="sourceCN", required=true) String sourceCN,
			@RequestParam(value="destinationDN", required=true) String destinationDN) {
		try {
			logger.info("Agent move request has been receieved. Agent will be moved from: " + sourceDN + " to: " + destinationDN);
			//check memberships and if membership exists in any group update DN info
			String newAgentDN = "cn=" + sourceCN + "," + destinationDN;
			ldapService.moveEntry(sourceDN, destinationDN);
			List<LdapEntry> subEntries = ldapService.search("member", sourceDN, new String[] {"*"});
			for (LdapEntry ldapEntry : subEntries) {

				ldapService.updateEntryAddAtribute(ldapEntry.getDistinguishedName(), "member", newAgentDN);
				ldapService.updateEntryRemoveAttributeWithValue(ldapEntry.getDistinguishedName(), "member", sourceDN);
			}
			List<AgentImpl> agent = agentService.findAgentByDn(sourceDN);
			//update C_AGENT table
			agentService.updateAgentDN(sourceDN, newAgentDN);

			//update C_COMMAND and C_COMMAND_EXECUTION table
			commandService.updateAgentDN(sourceDN, newAgentDN);
			
			String directoryType = agent.get(0).getUserDirectoryDomain();
			if (directoryType == "AD") {
				directoryType = "ACTIVE_DIRECTORY";
			}
			//send task to Ahenk to change DN with new DN
			Map<String, Object> parameterMap = new  HashMap<>();
			parameterMap.put("dn", sourceDN);
			parameterMap.put("new_parent_dn", destinationDN);
			parameterMap.put("directory_server", directoryType);
			List<String> dnList = new ArrayList<>();
			dnList.add(sourceCN);

			PluginImpl plugin = new PluginImpl();
			plugin = pluginService.findPluginIdByName("ldap");
			PluginTask requestBody = new PluginTask();
			requestBody.setCommandId("MOVE_AGENT");
			requestBody.setPlugin(plugin);

			requestBody.setParameterMap(parameterMap);
			requestBody.setDnType(DNType.AHENK);
			requestBody.setState(1);
			requestBody.setDnList(dnList);
			List<LdapEntry> entryList = new  ArrayList<LdapEntry>();
			LdapEntry ldapEntry = ldapService.getEntryDetail(newAgentDN);
			entryList.add(ldapEntry);

			requestBody.setEntryList(entryList);
			IRestResponse restResponse = taskService.execute(requestBody);
			logger.debug("Completed processing request, returning result: {}", restResponse.toJson());
			
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(ldapEntry);
			
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.EXPECTATION_FAILED)
					.body(null);
		}

	}

	@Operation(summary = "Delete agent by id", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Agent by id deleted"),
			  @ApiResponse(responseCode = "404", description = "Agent id not found", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/delete/agent", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean>  deleteAgent(@RequestParam(value="agentDN", required=true) String agentDN,
			@RequestParam(value="agentUID", required=true) String agentUID) {
		logger.info("Agent delete request has been receieved. DN: " + agentDN);

		List<AgentImpl> agent = agentService.findAgentByDn(agentDN);
		String directoryType = agent.get(0).getUserDirectoryDomain();
		if (directoryType == "AD") {
			directoryType = "ACTIVE_DIRECTORY";
		}
		Boolean isAgentOnline = false;
		if(agent != null && agent.size() > 0) {
			isAgentOnline = messagingService.isRecipientOnline(agent.get(0).getJid());
		}
		
		//send task to Ahenk to change DN with new DN
		Map<String, Object> parameterMap = new  HashMap<>();
		parameterMap.put("dn", agentDN);
		parameterMap.put("directory_server", directoryType);
		List<String> dnList = new ArrayList<>();
		dnList.add(agentDN);
		//if only agent is online send delete task
		//if agent is offline just delete from database and ldap
		if(isAgentOnline) {
			PluginImpl plugin = new PluginImpl();
			plugin = pluginService.findPluginIdByName("ldap");
			PluginTask requestBody = new PluginTask();
			requestBody.setCommandId("DELETE_AGENT");
			requestBody.setPlugin(plugin);

			requestBody.setParameterMap(parameterMap);
			requestBody.setDnType(DNType.AHENK);
			requestBody.setState(1);
			requestBody.setDnList(dnList);
			List<LdapEntry> entryList = new  ArrayList<LdapEntry>();
			LdapEntry ldapEntry = ldapService.getEntryDetail(agentDN);
			entryList.add(ldapEntry);

			requestBody.setEntryList(entryList);
			IRestResponse restResponse = taskService.execute(requestBody);
			logger.debug("Completed processing request, returning result: {}", restResponse.toJson());
		}

		agentService.deleteAgent(agentDN);
		try {
			ldapService.deleteEntry(agentDN);
		} catch (LdapException e) {
			e.printStackTrace();
			HttpHeaders headers = new HttpHeaders();
			return ResponseEntity.
					status(HttpStatus.EXPECTATION_FAILED).
					headers(headers)
					.build();
			
		}
		
		commandService.deleteAgentCommands(agentDN, agentUID);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(true);

	}


	@Operation(summary = "Rename agent ", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Updated agent name"),
			  @ApiResponse(responseCode = "417", description = "Could not rename agent. Unexpected error occured", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/rename/agent", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> renameAgent(
			@RequestParam(value="agentDN", required=true) String agentDN,
			@RequestParam(value="cn", required=true) String cn,
			@RequestParam(value="newHostname", required=true) String newHostname) {
		logger.info("Agent rename request has been receieved. DN: " + agentDN);
		
		List<AgentImpl> agent = agentService.findAgentByDn(agentDN);
		Boolean isAgentOnline = false;
		if(agent != null && agent.size() > 0) {
			isAgentOnline = messagingService.isRecipientOnline(agent.get(0).getJid());
		}
		
		String newAgentDN = agentDN.replace("cn=" + cn, "cn=" + newHostname);

		List<LdapEntry> entryList = new  ArrayList<LdapEntry>();
		LdapEntry ldapEntryForTask = ldapService.getEntryDetail(agentDN);
		entryList.add(ldapEntryForTask);

		if(agentService.findAgentByHostname(newHostname).size() > 0) {
			return new ResponseEntity<Boolean>(false, HttpStatus.CONFLICT);
		}
		try {
			//update uid attribute
			ldapService.renameHostname("uid", newHostname, agentDN);
			
			messagingService.addClientToRoster(newHostname + "@"+configurationService.getXmppServiceName());

			//check memberships and if membership exists in any group update DN info
			ldapService.renameEntry(agentDN, "cn=" + newHostname);
			List<LdapEntry> subEntries = ldapService.search("member", agentDN, new String[] {"*"});
			for (LdapEntry ldapEntry : subEntries) {
				ldapService.updateEntryAddAtribute(ldapEntry.getDistinguishedName(), "member", newAgentDN);
				ldapService.updateEntryRemoveAttributeWithValue(ldapEntry.getDistinguishedName(), "member", agentDN);
			}
		} catch (LdapException e) {
			logger.error("Error occured while renaming the agent. Message: " + e.getMessage());
		}
		String directoryType = agent.get(0).getUserDirectoryDomain();
		if (directoryType == "AD") {
			directoryType = "ACTIVE_DIRECTORY";
		}

		//send task to Ahenk to change DN with new DN
		Map<String, Object> parameterMap = new  HashMap<>();
		parameterMap.put("dn", agentDN);
		parameterMap.put("old_cn", cn);
		parameterMap.put("new_cn", newHostname);
		parameterMap.put("directory_server", directoryType);
		List<String> dnList = new ArrayList<>();
		dnList.add(agentDN);
		
		if(isAgentOnline) {
			PluginImpl plugin = new PluginImpl();
			plugin = pluginService.findPluginIdByName("ldap");
			PluginTask requestBody = new PluginTask();
			requestBody.setCommandId("RENAME_ENTRY");
			requestBody.setPlugin(plugin);

			requestBody.setParameterMap(parameterMap);
			requestBody.setDnType(DNType.AHENK);
			requestBody.setState(1);
			requestBody.setDnList(dnList);

			requestBody.setEntryList(entryList);
			IRestResponse restResponse = taskService.execute(requestBody);
			logger.debug("Completed processing request, returning result: {}", restResponse.toJson());
		}
		//update C_AGENT table
		agentService.updateHostname(agentDN, newAgentDN, newHostname);
		//update C_COMMAND and C_COMMAND_EXECUTION table
		commandService.updateAgentHostname(agentDN, newAgentDN, cn, newHostname);
		
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}

	/**
	 * delete user ous
	 * @param selectedEntryArr
	 * @return
	 */

	
	@Operation(summary = "Delete computer by ou ", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Delete computer ou"),
			  @ApiResponse(responseCode = "417", description = "Could not delete computer ou. Unexpected error occured", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/delete-computer-ou")
	public ResponseEntity<Boolean> deleteComputerOu(@RequestBody LdapEntry[] selectedEntryArr) {
		try {
			for (LdapEntry ldapEntry : selectedEntryArr) {
				if(ldapEntry.getType().equals(DNType.ORGANIZATIONAL_UNIT)) {
					//					ldapService.updateOLCAccessRulesAfterEntryDelete(ldapEntry.getDistinguishedName());
					LdapEntry entry= ldapService.getOuAndOuSubTreeDetail(ldapEntry.getDistinguishedName());
					if(entry.getChildEntries().size()>0) {
						return ResponseEntity
								.status(HttpStatus.OK)
								.body(false);
							
					}
					else {
						
						Map<String, Object> requestData = new HashMap<String, Object>();
						requestData.put("dn",entry.getDistinguishedName());
						ObjectMapper dataMapper = new ObjectMapper();
						String jsonString = null ; 
						try {
							jsonString = dataMapper.writeValueAsString(requestData);
						} catch (JsonProcessingException e1) {
							logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
						}
						String log = entry.getDistinguishedName()+ " folder has been deleted";
						operationLogService.saveOperationLog(OperationType.DELETE, log, jsonString.getBytes(), null, null, null);
						
						ldapService.deleteNodes(entry);
						return ResponseEntity
								.status(HttpStatus.OK)
								.body(true);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			HttpHeaders headers = new HttpHeaders();
			return ResponseEntity
					.status(HttpStatus.EXPECTATION_FAILED)
					.headers(headers)
					.build();
		}
		//return null;
		return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Getting agent info", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns agent info"),
			  @ApiResponse(responseCode = "417", description = "Could not get agent info. Unexpexted error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/get-agent-info", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean>  getAgentInfo(@RequestParam(value="agentDN", required=true) String agentDN) {
		logger.info("Agent info request has been receieved. DN: " + agentDN);
		//send task to Ahenk to change DN with new DN
		Map<String, Object> parameterMap = new  HashMap<>();
		parameterMap.put("dn", agentDN);
		List<String> dnList = new ArrayList<>();
		dnList.add(agentDN);

		PluginImpl plugin = new PluginImpl();
		plugin = pluginService.findPluginIdByName("resource-usage");
		PluginTask requestBody = new PluginTask();
		requestBody.setCommandId("AGENT_INFO");
		requestBody.setPlugin(plugin);

		requestBody.setParameterMap(parameterMap);
		requestBody.setDnType(DNType.AHENK);
		requestBody.setState(1);
		requestBody.setDnList(dnList);
		List<LdapEntry> entryList = new  ArrayList<LdapEntry>();
		LdapEntry ldapEntry = ldapService.getEntryDetail(agentDN);
		entryList.add(ldapEntry);

		requestBody.setEntryList(entryList);
		IRestResponse restResponse = taskService.execute(requestBody);
		//		agentService.deleteAgent(agentDN);
		//		try {
		//			ldapService.deleteEntry(agentDN);
		//		} catch (LdapException e) {
		//			e.printStackTrace();
		//		}
		//		logger.debug("Completed processing request, returning result: {}", restResponse.toJson());
		//
		//		commandService.deleteAgentCommands(agentDN, agentUID);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(true);
	}

	@Operation(summary = "Update agent info", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Update agent info"),
			  @ApiResponse(responseCode = "404", description = "Agent info is not found. Not found", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/update-agent-info",  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AgentImpl>  updateAgentInfo(@RequestParam(value="ipAddresses", required=true) String ipAddresses,
			@RequestParam(value="hostname", required=true) String hostname,
			@RequestParam(value="agentVersion", required=true) String agentVersion,
			@RequestParam(value="macAddresses", required=true) String macAddresses,
			@RequestParam(value="phase", required=true) String phase,
			@RequestParam(value="agentUid", required=true) String agentUid,
			@RequestParam(value="osVersion", required=true) String osVersion,
			@RequestParam(value="hardwareDiskSsdInfo", required=true) String hardwareDiskSsdInfo,
			@RequestParam(value="hardwareDiskHddInfo", required=true) String hardwareDiskHddInfo){

		List<AgentImpl> agents =  agentService.findAgentByJid(agentUid);

		if (agents != null && agents.size() > 0) {
			AgentImpl agent = agents.get(0);
			for (AgentPropertyImpl prop : agent.getProperties()) {
				if (prop.getPropertyName().equals("hardware.network.ipAddresses")
						&& prop.getPropertyValue() != ipAddresses
						&& !agent.getIpAddresses().equals(ipAddresses)) {
					prop.setPropertyValue(ipAddresses);
					agent.setIpAddresses(ipAddresses);
				} else if (hostname != null && !agent.getHostname().equals(hostname)) {
					agent.setHostname(hostname);
				} else if (prop.getPropertyName().equals("macAddresses")
						&& prop.getPropertyValue() != macAddresses
						&& !agent.getMacAddresses().equals(macAddresses)) {
					prop.setPropertyValue(macAddresses);
					agent.setMacAddresses(macAddresses);
				} else if (prop.getPropertyName().equals("agentVersion")
						&& prop.getPropertyValue() != agentVersion) {
					prop.setPropertyValue(agentVersion);
				} else if (phase != null && phase != "" && prop.getPropertyName().equals("phase")
						&& prop.getPropertyValue() != phase) {
					prop.setPropertyValue(phase);
				}
				else if (osVersion != null 
						&& prop.getPropertyName().equals("os.version") 
						&& !prop.getPropertyValue().equals(osVersion)) {
					prop.setPropertyValue(osVersion);
				}
				else if (phase != null && phase != "" && prop.getPropertyName().equals("phase")
						&& prop.getPropertyValue() != phase) {
					prop.setPropertyValue(phase);
				}
				else if (hardwareDiskSsdInfo != null 
						&& prop.getPropertyName().equals("hardwareDiskSsdInfo") 
						&& !prop.getPropertyValue().equals(hardwareDiskSsdInfo)) {
					prop.setPropertyValue(hardwareDiskSsdInfo);
				}
				else if (hardwareDiskHddInfo != null 
						&& prop.getPropertyName().equals("hardwareDiskHddInfo") 
						&& !prop.getPropertyValue().equals(hardwareDiskHddInfo)) {
					prop.setPropertyValue(hardwareDiskHddInfo);
				}
			}

			if (isPropertyName(agentUid, "agentVersion") == false) {
				agent.addProperty(new AgentPropertyImpl(null, agent, "agentVersion",
						agentVersion.toString(), new Date()));
			} 
			if (isPropertyName(agentUid, "phase") == false) {
				agent.addProperty(new AgentPropertyImpl(null, agent, "phase",
						phase.toString(), new Date()));
			} 
			if (isPropertyName(agentUid, "hardwareDiskSsdInfo") == false) {
				agent.addProperty(new AgentPropertyImpl(null, agent, "hardwareDiskSsdInfo",
						phase.toString(), new Date()));
			} 
			if (isPropertyName(agentUid, "hardwareDiskHddInfo") == false) {
				agent.addProperty(new AgentPropertyImpl(null, agent, "hardwareDiskHddInfo",
						phase.toString(), new Date()));
			} 
			agentRepository.save(agent);
			
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(agent);
					
		} else {
			HttpHeaders headers = new HttpHeaders();
			return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.headers(headers)
					.build();
					
		}
	}

	public Boolean isPropertyName(String agentUid, String propertyName) {
		Boolean isExist = false;
		List<AgentImpl> agents =  agentService.findAgentByJid(agentUid);
		if (agents != null && agents.size() > 0) {
			AgentImpl agent = agents.get(0);
			for (AgentPropertyImpl prop : agent.getProperties()) {
				if (prop.getPropertyName().equals(propertyName)) {
					isExist = true;
				}
			}
		}
		return isExist;
	}
	
	@Operation(summary = "Adding organization unit", description = "", tags = { "computer-management" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Add ou"),
			  @ApiResponse(responseCode = "417", description = "Could not add organization unit. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/add-ou",  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LdapEntry>  addOu(LdapEntry selectedEntry) {
		try {
			Map<String, String[]> attributes = new HashMap<String,String[]>();
			attributes.put("objectClass", new String[] {"organizationalUnit", "top", "pardusLider"} );
			attributes.put("ou", new String[] { selectedEntry.getOu() });

			String dn="ou="+selectedEntry.getOu()+","+selectedEntry.getParentName();
			
			ldapService.addEntry(dn, attributes);
			logger.info("OU created successfully RDN ="+dn);
			
			//get full of ou details after creation
			selectedEntry = ldapService.getEntryDetail(dn);
			
			Map<String, Object> requestData = new HashMap<String, Object>();
			requestData.put("dn",selectedEntry.getDistinguishedName());
			ObjectMapper dataMapper = new ObjectMapper();
			String jsonString = null ; 
			try {
				jsonString = dataMapper.writeValueAsString(requestData);
			} catch (JsonProcessingException e1) {
				logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
			}
			String log = selectedEntry.getDistinguishedName() + " folder has been created";
			operationLogService.saveOperationLog(OperationType.CREATE, log, jsonString.getBytes(), null, null, null);
			
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(selectedEntry);
					
		} catch (LdapException e) {
			e.printStackTrace();
			HttpHeaders headers = new HttpHeaders();
			return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.headers(headers)
					.build();
		}
	}
}
