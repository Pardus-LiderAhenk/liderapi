package tr.org.lider.controllers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.entities.OperationType;
import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.ldap.LdapSearchFilterAttribute;
import tr.org.lider.ldap.SearchFilterEnum;
import tr.org.lider.models.PolicyResponse;
import tr.org.lider.security.CustomPasswordEncoder;
import tr.org.lider.services.AdService;
import tr.org.lider.services.ConfigurationService;
import tr.org.lider.services.OperationLogService;
import tr.org.lider.services.PolicyService;

/**
 * 
 * @author M. Edip YILDIZ
 *
 */
@RestController
@RequestMapping("/api/ad")
@Tag(name="Ad", description = "Ad Rest Service")
public class AdController {
	Logger logger = LoggerFactory.getLogger(AdController.class);

	@Autowired
	private AdService service;

	@Autowired
	private LDAPServiceImpl ldapService;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private OperationLogService operationLogService; 
	
	@Autowired
	private PolicyService policyService;
	
	@Autowired
	private CustomPasswordEncoder customPasswordEncoder;
	
	@Operation(summary = "Get Ldap list domain entry", description = "")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Ldap list exists"),
			  @ApiResponse(responseCode = "417", description = "Could not retrieve domain entry. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/domain-entry")
	public ResponseEntity<List<LdapEntry>>  getDomainEntry(HttpServletRequest request) {
		logger.info("Getting AD base DN ");
		List<LdapEntry> retList =null;
		try {
			retList= new ArrayList<LdapEntry>();
			LdapEntry domainEntry=service.getDomainEntry();
			if(domainEntry ==null)
			{
				return null;
			}
			domainEntry.setName(domainEntry.getDistinguishedName());
			retList.add(domainEntry);
		} catch (LdapException e) {
			e.printStackTrace();
			logger.error("Could not retrieve domain entry. Error: " + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "Could not retrieve domain entry. Error: " + e.getMessage());
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(retList);
	}
	
	@Operation(summary = "Get Ldap list domain entry", description = "")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns entries under selected ou."),
			  @ApiResponse(responseCode = "417", description = "Could not retrieve entries under selected ou. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/child-entries-ou")
	public ResponseEntity<List<LdapEntry>>  getChildEntriesOu(HttpServletRequest request, LdapEntry selectedEntry) {
		logger.info("Getting AD child OU entries for dn = "+ selectedEntry.getUid());
		List<LdapEntry> oneLevelSubList=null;
		try {
			String filter="(|"
					+ "(objectclass=container)"
					+ "(objectclass=organizationalUnit)"
					+ "(objectclass=computer)"
					+ "(objectclass=organizationalPerson)"
					+ "(objectclass=group)"
					+ "(objectclass=user)"
					+")";
			
			oneLevelSubList= new ArrayList<>();
			oneLevelSubList = service.findSubEntries(selectedEntry.getUid(),filter,new String[] { "*" }, SearchScope.ONELEVEL);
		} catch (LdapException e) {
			e.printStackTrace();
			logger.error("Could not retrieve entries under selected ou. Error: " + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "Could not retrieve entries under selected ou. Error: " + e.getMessage());
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(oneLevelSubList);	
	}
	
	@Operation(summary = "Returns ldap sub list", description = "")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns ldap sub list under selected entry."),
			  @ApiResponse(responseCode = "417", description = "Could not retrieve ldap sub list. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/child-entries")
	public ResponseEntity<List<LdapEntry>> getChildEntries(HttpServletRequest request, LdapEntry selectedEntry) {
		logger.info("Getting AD child entries for dn = "+ selectedEntry.getDistinguishedName());
		List<LdapEntry> oneLevelSubList=null;
		try {
			String filter="(|"
					+ "(objectclass=container)"
					+ "(objectclass=organizationalUnit)"
						+ "(objectclass=computer)"
						+ "(objectclass=organizationalPerson)"
						+ "(objectclass=group)"
					+")";
			
			oneLevelSubList= new ArrayList<>();
			oneLevelSubList = service.findSubEntries(selectedEntry.getDistinguishedName(),filter,new String[] { "*" }, SearchScope.ONELEVEL);
		} catch (LdapException e) {
			e.printStackTrace();
			logger.error("Could not retrieve ldap sub list. Error: " + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "Could not retrieve ldap sub list. Error: " + e.getMessage());
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(oneLevelSubList);	
	}
	
	@Operation(summary = "Add user to AD", description = "")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Add user to AD."),
			  @ApiResponse(responseCode = "417", description = "Could not add user to AD. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/add-user-to-ad")
	public ResponseEntity<?> addUser2AD(HttpServletRequest request, LdapEntry selectedEntry) {
		 logger.info("Adding user to AD. User info : "+ selectedEntry.getDistinguishedName());
		 
		 Map<String, String[]> attributes = new HashMap<String, String[]>();
		
		 attributes.put("objectClass", new String[] {"top","person","organizationalPerson","user"});
		 attributes.put("cn", new String[] {selectedEntry.getCn()});
		 attributes.put("sAMAccountName", new String[] {selectedEntry.getUid()});
		 attributes.put("userPrincipalName", new String[] {selectedEntry.getUid()+"@"+configurationService.getAdDomainName()});
		 attributes.put("givenName", new String[] {selectedEntry.getName()});
		 attributes.put("displayName", new String[] {selectedEntry.getCn()});
		 attributes.put("name", new String[] {selectedEntry.getCn()});
		 attributes.put("mail", new String[] {selectedEntry.getMail()});
		 attributes.put("telephoneNumber", new String[] {selectedEntry.getTelephoneNumber()});
		 attributes.put("streetAddress", new String[] {selectedEntry.getHomePostalAddress()});
		 attributes.put("sn", new String[] {selectedEntry.getSn()});
//		 attributes.put("userpassword", new String[] {selectedEntry.getUserPassword()});
		 String newQuotedPassword = "\"" + selectedEntry.getUserPassword() + "\"";
		 
		 byte[] newUnicodePassword =null;
		 try {
			 newUnicodePassword	= newQuotedPassword.getBytes("UTF-16LE");
				attributes.put("unicodePwd", new String[] {new String(newUnicodePassword)});
		 } 
		 catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
		}

		// some useful constants from lmaccess.h
		 int UF_ACCOUNTENABLE = 0x0001;   
//		 int UF_ACCOUNTDISABLE = 0x0002;
	     int UF_PASSWD_NOTREQD = 0x0020;
//	     int UF_PASSWD_CANT_CHANGE = 0x0040;
	     int UF_NORMAL_ACCOUNT = 0x0200;
//	     int UF_DONT_EXPIRE_PASSWD = 0x10000;
	     int UF_PASSWORD_EXPIRED = 0x800000;
	        
	     String uacStr=   Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWD_NOTREQD + UF_PASSWORD_EXPIRED + UF_ACCOUNTENABLE);
	     attributes.put("userAccountControl", new String[] {uacStr});
	     attributes.put("pwdLastSet", new String[] {"0"});
	    
		 try {
			String rdn="CN="+selectedEntry.getCn()+","+selectedEntry.getParentName();
			service.addEntry(rdn, attributes);
			selectedEntry = service.getEntryDetail(rdn);
			operationLogService.saveOperationLog(OperationType.CREATE, selectedEntry.getCn() + ", USER has been added " + rdn + " [Active Directroy]", null);
//			return responseFactoryService.createResponse(RestResponseStatus.OK,"Kullanıcı Başarı ile oluşturuldu.");
			return new ResponseEntity<LdapEntry>(selectedEntry, HttpStatus.OK);
			} catch (LdapException e) {
				e.printStackTrace();
				String message=e.getLocalizedMessage();
				if(message!=null && message.contains("ENTRY_EXISTS")) {
					return new ResponseEntity<LdapEntry>(HttpStatus.ALREADY_REPORTED);
				} else {
					return new ResponseEntity<LdapEntry>(HttpStatus.EXPECTATION_FAILED);
				}
			}
	}
	
	@Operation(summary = "Add ou to AD.", description = "Add ou to AD.")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Add ou to AD."),
			  @ApiResponse(responseCode = "417", description = "Could not add ou to AD. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/add-ou-to-ad")
	public ResponseEntity<LdapEntry> addOu2AD(HttpServletRequest request, LdapEntry selectedEntry) {
		logger.info("Adding OU to AD. Ou info {} {}", selectedEntry.getDistinguishedName(),selectedEntry.getOu());
		Map<String, String[]> attributes = new HashMap<String, String[]>();
		attributes.put("objectClass", new String[] {"top","organizationalUnit"});
		attributes.put("ou", new String[] {selectedEntry.getOu()});
		try {
			String rdn = "OU="+selectedEntry.getOu()+","+selectedEntry.getParentName();
			service.addEntry(rdn, attributes);
			selectedEntry = service.getEntryDetail(rdn);
			operationLogService.saveOperationLog(OperationType.CREATE, selectedEntry.getOu() + ", FOLDER has been added " + rdn + " [Active Directroy]", null);
			return new ResponseEntity<LdapEntry>(selectedEntry, HttpStatus.OK);
		} catch (LdapException e) {
			return new ResponseEntity<LdapEntry>(HttpStatus.EXPECTATION_FAILED);
		}

	}
	
	@Operation(summary = "Add group to AD .", description = "Adding group to AD.")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Add group to AD."),
			  @ApiResponse(responseCode = "417", description = "Could not add group to AD. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/add-group-to-ad")	
	public ResponseEntity<LdapEntry>  addGroup2AD(HttpServletRequest request, LdapEntry selectedEntry) {
		logger.info("Adding Group to AD. Group info {} {}", selectedEntry.getDistinguishedName(),selectedEntry.getCn());
		
		Map<String, String[]> attributes = new HashMap<String, String[]>();
		
		attributes.put("objectClass", new String[] {"top","group"});
		attributes.put("CN", new String[] {selectedEntry.getCn()});
		attributes.put("sAMAccountName", new String[] {selectedEntry.getCn()});
		
		try {
			String rdn="CN="+selectedEntry.getCn()+","+selectedEntry.getParentName();
			service.addEntry(rdn, attributes);
			selectedEntry = service.getEntryDetail(rdn);
			operationLogService.saveOperationLog(OperationType.CREATE, selectedEntry.getCn() + ", GROUP has been added " + rdn + " [Active Directroy]",null);
			return new ResponseEntity<LdapEntry>(selectedEntry, HttpStatus.OK);
		} catch (LdapException e) {
			return new ResponseEntity<LdapEntry>(HttpStatus.EXPECTATION_FAILED);

		}
	}
	
	@Operation(summary = "Add member to group.", description = "Adding member in group")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Add member to group"),
			  @ApiResponse(responseCode = "417", description = "Could not add member to group. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/add-member-to-ad-group")
	public ResponseEntity<LdapEntry> addMember2ADGroup(HttpServletRequest request, LdapEntry selectedEntry) {
		logger.info("Adding {} to group. Group info {} ", selectedEntry.getDistinguishedName(),selectedEntry.getParentName());
		
		LdapEntry memberEntry = null;
		
		try {
			memberEntry = service.getEntryDetail(selectedEntry.getDistinguishedName());
			service.updateEntryAddAtribute(selectedEntry.getParentName(), "member", selectedEntry.getDistinguishedName());
			operationLogService.saveOperationLog(OperationType.UPDATE, memberEntry.getCn()+ " User has been added to Group " + selectedEntry.getParentName() + " [Active Directroy]", null);
			selectedEntry = service.getEntryDetail(selectedEntry.getParentName());
			return new ResponseEntity<LdapEntry>(selectedEntry, HttpStatus.OK);
		} catch (LdapException e) {
			return new ResponseEntity<LdapEntry>(HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	@Operation(summary = "Returns user list under selected entry.", description = "")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns user list under selected entry."),
			  @ApiResponse(responseCode = "417", description = "Could not retrieve user list under selected entry. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@GetMapping(value = "/search-entry-user/search-dn/{searchDn}/key/{key}/value/{value}")
	public ResponseEntity<List<LdapEntry>>  searchEntryUser(HttpServletRequest request,
			@RequestParam(value="searchDn", required=true) String searchDn,
			@RequestParam(value="key", required=true) String key, 
			@RequestParam(value="value", required=true) String value) {
		List<LdapEntry> results=null;
		
		logger.info("Search for key {} value {}  only users ",key, value);
		try {
			if(searchDn.equals("")) {
				searchDn=service.getADDomainName();
			}
			List<LdapSearchFilterAttribute> filterAttributes = new ArrayList<LdapSearchFilterAttribute>();
			filterAttributes.add(new LdapSearchFilterAttribute(key, value, SearchFilterEnum.EQ));
			filterAttributes.add(new LdapSearchFilterAttribute("objectclass", "user", SearchFilterEnum.EQ)); 
			filterAttributes.add(new LdapSearchFilterAttribute("objectclass", "computer", SearchFilterEnum.NOT_EQ)); 
			results = service.search(searchDn,filterAttributes, new String[] {"*"});
		} catch (LdapException e) {
			logger.error("Could not retrieve user list. Error: " + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "Could not retrieve user list. Error: " + e.getMessage());
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(results);
	}
	
	@Operation(summary = "Returns entry group list.", description = "")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns entry group list."),
			  @ApiResponse(responseCode = "417", description = "Could not retrieve entry group list. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@GetMapping(value = "/search-entry-group/search-dn/{searchDn}/key/{key}/value/{value}")
	public ResponseEntity<List<LdapEntry>>   searchEntryGroup(HttpServletRequest request,
			@RequestParam(value="searchDn", required=true) String searchDn,
			@RequestParam(value="key", required=true) String key, 
			@RequestParam(value="value", required=true) String value) {
		List<LdapEntry> results=null;
		
		logger.info("Search for key {} value {}  only groups ",key, value);
		try {
			if(searchDn.equals("")) {
				searchDn=service.getADDomainName();
			}
			List<LdapSearchFilterAttribute> filterAttributes = new ArrayList<LdapSearchFilterAttribute>();
			filterAttributes.add(new LdapSearchFilterAttribute(key, value, SearchFilterEnum.EQ));
			filterAttributes.add(new LdapSearchFilterAttribute("objectclass", "group", SearchFilterEnum.EQ)); 
			results = service.search(searchDn,filterAttributes, new String[] {"*"});
		} catch (LdapException e) {
			logger.error("Could not retrieve entry group list. Error: " + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "Could not retrieve entry group list. Error: " + e.getMessage());
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(results);
	}
	
	@Operation(summary = "Returns entry list.", description = "")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns entry  list."),
			  @ApiResponse(responseCode = "417", description = "Could not entry  list. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/search-entry")
	public ResponseEntity<List<LdapEntry>> searchEntry(HttpServletRequest request,
			@RequestParam(value="searchDn", required=true) String searchDn,
			@RequestParam(value="key", required=true) String key, 
			@RequestParam(value="value", required=true) String value) {
		logger.info("Search for key {} value {}   ",key, value);
		List<LdapEntry> results=null;
		try {
			if(searchDn.equals("")) {
				searchDn=service.getADDomainName();
			}
			List<LdapSearchFilterAttribute> filterAttributes = new ArrayList<LdapSearchFilterAttribute>();
			filterAttributes.add(new LdapSearchFilterAttribute(key, value, SearchFilterEnum.EQ));
			results = service.search(searchDn,filterAttributes, new String[] {"*"});
		} catch (LdapException e) {
			e.printStackTrace();
			logger.error("Could not entry  list. Error: " + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "Could not entry  list. Error: " + e.getMessage());
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(results);
	}
	
	@Operation(summary = "User created from ad to ldap.", description = "User created from ad to ldap.")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "User created from ad to ldap."),
			  @ApiResponse(responseCode = "417", description = "A new user could not be created. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/sync-user-from-ad-to-ldap")	
	public ResponseEntity<List<LdapEntry>> syncUserFromAd2Ldap(HttpServletRequest request,@RequestBody LdapEntry selectedLdapDn) {
		logger.info("SYNC AD to LDAP starting.. Sync to LDAP OU ="+selectedLdapDn.getDistinguishedName() );
		String filter="(objectClass=organizationalUnit)";
		List<LdapEntry> existUserList= new ArrayList<>();
		try {
			//getting ldap ou, userss added this ou
			List<LdapEntry> selectedLdapEntryList=ldapService.findSubEntries(selectedLdapDn.getDistinguishedName() , filter, new String[] { "*" }, SearchScope.OBJECT);
			
			String adfilter="(objectclass=organizationalPerson)";
			/**
			 *  selectedLdapDn.getChildEntries() holds users that they will add to ldap
			 */
			for (LdapEntry adUserEntry : selectedLdapDn.getChildEntries()) {
				//getting users from AD
				List<LdapEntry> adUserList = service.findSubEntries(adUserEntry.getDistinguishedName(),adfilter,new String[] { "*" }, SearchScope.OBJECT);
				
				if(adUserList !=null && adUserList.size()>0) {
					LdapEntry adUser= adUserList.get(0);
					String sAMAccountName= adUser.getAttributesMultiValues().get("sAMAccountName")[0];
					String CN= adUser.getAttributesMultiValues().get("cn")[0];
					
					List<LdapEntry> adUserListForCheck=ldapService.findSubEntries(ldapService.getDomainEntry().getDistinguishedName() 
							, "(uid="+sAMAccountName+")", new String[] { "*" }, SearchScope.SUBTREE);
					
					if(adUserListForCheck!=null && adUserListForCheck.size()==0) {
						addUserToLDAP(selectedLdapEntryList.get(0).getDistinguishedName(), adUser, sAMAccountName,sAMAccountName);
						operationLogService.saveOperationLog(OperationType.UPDATE, sAMAccountName + " User has been moved to LDAP" + selectedLdapEntryList.get(0).getDistinguishedName(),null);
					}
					else {
						logger.info("SYNC AD to LDAP.. User exist ="+adUser.getDistinguishedName() );
						existUserList.add(adUser);
					}
				}
			}
			
		} catch (LdapException e) {
			e.printStackTrace();
			logger.error("A new user has not been created. Error: " + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "A new user could not be created. Error: " + e.getMessage());
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(existUserList);
	}
	
	@Operation(summary = "User has been moved to the lider system.", description = "User has been moved to the lider system.")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "User has been moved to the lider system."),
			  			  @ApiResponse(responseCode = "417", description = "User could not be moved to the lider system.Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/move-ad-user-to-ldap")
	public ResponseEntity<List<LdapEntry>> moveAdUser2Ldap(HttpServletRequest request,@RequestBody LdapEntry selectedLdapDn) {
		
		List<LdapEntry> existUserList= new ArrayList<>();
		try {
			String globalUserOu = configurationService.getUserLdapBaseDn();
			String adfilter="(objectclass=organizationalPerson)";
			/**
			 *  selectedLdapDn.getChildEntries() holds users that they will add to ldap
			 */
			for (LdapEntry adUserEntry : selectedLdapDn.getChildEntries()) {
				//getting users from AD
				List<LdapEntry> adUserList = service.findSubEntries(adUserEntry.getDistinguishedName(),adfilter,new String[] { "*" }, SearchScope.OBJECT);
				
				if(adUserList !=null && adUserList.size()>0) {
					LdapEntry adUser= adUserList.get(0);
					String sAMAccountName= adUser.getAttributesMultiValues().get("sAMAccountName")[0];
					
					List<LdapEntry> adUserListForCheck=ldapService.findSubEntries(ldapService.getDomainEntry().getDistinguishedName() 
							, "(uid="+sAMAccountName+")", new String[] { "*" }, SearchScope.SUBTREE);
					
					if(adUserListForCheck!=null && adUserListForCheck.size()==0) {
						String dn=addUserToLDAP(globalUserOu, adUser, sAMAccountName, customPasswordEncoder.encode(selectedLdapDn.getUserPassword()));
						
						ldapService.updateEntryAddAtribute(dn, "liderPrivilege", "ROLE_USER");
						ldapService.updateEntryAddAtribute(dn, "liderPrivilege", "ROLE_ADMIN");
						
						operationLogService.saveOperationLog(OperationType.MOVE, dn + " User authorization has been given for Lider", null);
					}
					else {
						logger.info("SYNC AD to LDAP.. User exist ="+adUser.getDistinguishedName() );
						existUserList.add(adUser);
					}
				}
			}
		} catch (LdapException e) {
			e.printStackTrace();
			logger.error("User has not been moved to the lider system. Error: " + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "User has not been moved to the lider system. Error: " + e.getMessage());
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(existUserList);
	}
	
	
	@Operation(summary = "Add selected AD group to LDAP", description = "")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Add selected AD group to LDAP"),
			  			  @ApiResponse(responseCode = "417", description = "Could not LDAP user group list. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/sync-group-from-ad-to-ldap")
	public ResponseEntity<List<LdapEntry>> syncGroupFromAd2Ldap(HttpServletRequest request,@RequestBody LdapEntry selectedLdapDn) {
		logger.info("SYNC GROUP AD to LDAP starting.. Sync to LDAP OU ="+selectedLdapDn.getDistinguishedName() );
		
		List<LdapEntry> existGroupList= new ArrayList<>();
		String filter="(objectClass=organizationalUnit)";
		try {
			//getting ldap ou, userss added this ou
			List<LdapEntry> selectedLdapEntryList=ldapService.findSubEntries(selectedLdapDn.getDistinguishedName() , filter, new String[] { "*" }, SearchScope.OBJECT);
			String destinationDnLdap=selectedLdapEntryList.get(0).getDistinguishedName();
			String adGroupfilter="(objectclass=group)";
			
			for (LdapEntry adGroupEntry : selectedLdapDn.getChildEntries()) {
				List<LdapEntry> adGroupList = service.findSubEntries(adGroupEntry.getDistinguishedName(),adGroupfilter,new String[] { "*" }, SearchScope.OBJECT);
				
				if(adGroupList !=null && adGroupList.size()>0) {
					
					LdapEntry adGroup= adGroupList.get(0);
					String cn=adGroup.get("cn");
					String filterLdapSearch="(&(objectClass=groupOfNames)(cn="+cn+"))";
					List<LdapEntry> adGroupListForCheck=ldapService.findSubEntries(ldapService.getDomainEntry().getDistinguishedName(), filterLdapSearch , new String[] { "*" }, SearchScope.SUBTREE);
					
					if(adGroupListForCheck!=null && adGroupListForCheck.size()==0) {
						
					
 						
						// find users of selected member and add this users to ldap user folder( ou=Users).. 
						String[] memberArr=adGroup.getAttributesMultiValues().get("member");
						// create temp list to add members for ldap adding
						List<String> memberDistinguishedNameArr= new ArrayList<>();
						if(memberArr.length>0) {
							for (int i = 0; i < memberArr.length; i++) {
								String memberDistinguishedName=memberArr[i];
								String adUserfilter="(objectclass=organizationalPerson)";
								/**
								 * getting ad group member details from AD
								 */
								List<LdapEntry> adUserList = service.findSubEntries(memberDistinguishedName,adUserfilter,new String[] { "*" }, SearchScope.OBJECT);
								
								String sAMAccountName=adUserList.get(0).get("sAMAccountName");
								
								List<LdapEntry> adUserListForCheck=ldapService.findSubEntries(ldapService.getDomainEntry().getDistinguishedName(), "(uid="+sAMAccountName+")", new String[] { "*" }, SearchScope.SUBTREE);
								/**
								 * if user isn't in ldap, user can add ldap
								 */
								if(adUserListForCheck!=null && adUserListForCheck.size()==0) {
									String rdn=addUserToLDAP(configurationService.getUserLdapBaseDn(), adUserList.get(0), sAMAccountName,sAMAccountName);
									memberDistinguishedNameArr.add(rdn);
								}
								else {
									memberDistinguishedNameArr.add(adUserListForCheck.get(0).getDistinguishedName());
								}
							}
						}
						// add selected AD group to LDAP
						Map<String, String[]> attributes = new HashMap<String, String[]>();
						attributes.put("objectClass", new String[] { "top", "groupOfNames", "pardusLider"});
						attributes.put("cn", new String[] { adGroup.get("cn") });
						attributes.put("liderGroupType", new String[] { "USER" });
						attributes.put("description", new String[] { "ADGROUP" });
						attributes.put("member", memberDistinguishedNameArr.stream().toArray(String[]::new));
						
						String rdn="cn="+adGroup.get("cn")+","+destinationDnLdap;
						ldapService.addEntry(rdn, attributes);
						
						operationLogService.saveOperationLog(OperationType.UPDATE, adGroup.get("cn") + " group has been moved to LDAP "+selectedLdapEntryList.get(0).getDistinguishedName(),null);
					}
					else {
						logger.info("SYNC AD to LDAP.. Group already exist ="+adGroup.getDistinguishedName() );
						existGroupList.add(adGroup);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Could not LDAP user group list. Error: " + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "Could not LDAP user group list. Error: " + e.getMessage());
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(existGroupList);
	}

	private String addUserToLDAP(String destinationDistinguishedName, LdapEntry adUser, String sAMAccountName, String userPassword)
			throws LdapException {
		String gidNumber="9000";
		int randomInt = (int)(1000000.0 * Math.random());
		String uidNumber= Integer.toString(randomInt);
		
		String home="/home/"+adUser.get("sAMAccountName"); 
		
		Map<String, String[]> attributes = new HashMap<String, String[]>();
		
		attributes.put("objectClass", new String[] { "top", "posixAccount",	"person","pardusLider","pardusAccount","organizationalPerson","inetOrgPerson"});
		attributes.put("cn", new String[] { adUser.get("givenName") });
		attributes.put("mail", new String[] { adUser.get("mail") });
		attributes.put("gidNumber", new String[] { gidNumber });
		attributes.put("homeDirectory", new String[] { home });
		if(adUser.get("sn") !=null &&  adUser.get("sn")!="" ) {
			attributes.put("sn", new String[] { adUser.get("sn") });
		}else {
			logger.info("SN not exist " );
			attributes.put("sn", new String[] { " " });
		}
		attributes.put("uid", new String[] { sAMAccountName });
		attributes.put("uidNumber", new String[] { uidNumber });
		attributes.put("loginShell", new String[] { "/bin/bash" });
		attributes.put("userPassword", new String[] { userPassword });
		attributes.put("homePostalAddress", new String[] { adUser.get("streetAddress") });
		attributes.put("employeeType", new String[] { "ADUser" });
		if(adUser.get("telephoneNumber")!=null && adUser.get("telephoneNumber")!="")
			attributes.put("telephoneNumber", new String[] { adUser.get("telephoneNumber") });
		
		String rdn="uid="+sAMAccountName+","+destinationDistinguishedName;
		try {
			ldapService.addEntry(rdn, attributes);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return rdn;
	}

	/**
	 * update user password
	 * @param selectedEntry
	 * @return
	 */
	@Operation(summary = "Update user password", description = "Update user password")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Update user password"),
			  			  @ApiResponse(responseCode = "404", description = "User is not found.Not found.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PutMapping(value = "/update-user-password",produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public  ResponseEntity<LdapEntry> updateUserPassword(LdapEntry selectedEntry) {
		logger.info("Resetting user password. Dn: {}",selectedEntry.getDistinguishedName());
		try {
			String newPassword =  selectedEntry.getUserPassword();
			     
			     String newQuotedPassword = "\"" + newPassword + "\"";
			     byte[] newUnicodePassword = null;
				try {
					newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
			
			if(!"".equals(selectedEntry.getUserPassword())){
				service.updateEntryReplaceAttribute(selectedEntry.getDistinguishedName(),"unicodePwd",new String(newUnicodePassword));
			}
			selectedEntry = service.findSubEntries(selectedEntry.getDistinguishedName(), "(objectclass=*)", new String[] {"*"}, SearchScope.OBJECT).get(0);
			operationLogService.saveOperationLog(OperationType.CHANGE_PASSWORD, selectedEntry.getDistinguishedName() + " password has been changed [Active Directory]", null);

			return new ResponseEntity<LdapEntry>(selectedEntry, HttpStatus.OK);
		} catch (LdapException e) {
			return new ResponseEntity<LdapEntry>(HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * delete entry for ad
	 * @param selectedEntry
	 * @return
	 */
	@Operation(summary = "Delete AD entries.", description = "Delete AD entries.")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Delete AD entries."),
			  			  @ApiResponse(responseCode = "404", description = "Could not delete AD entry. Not found", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@DeleteMapping(value = "/entry",produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody	
	public ResponseEntity<LdapEntry> deleteEntry(LdapEntry selectedEntry) {
		try {
			logger.info("AD Deleting entry. Dn: {}",selectedEntry.getDistinguishedName());
			service.deleteEntry(selectedEntry.getDistinguishedName());
			
			operationLogService.saveOperationLog(OperationType.DELETE, selectedEntry.getDistinguishedName() + " entry has been deleted from " + "[Active Directroy]", null);
			return new ResponseEntity<LdapEntry>(selectedEntry, HttpStatus.OK);
		} catch (LdapException e) {
			return new ResponseEntity<LdapEntry>(HttpStatus.EXPECTATION_FAILED);
		}
	}
	
//	/**
//	 * delete entry for ad
//	 * @param selectedEntry
//	 * @return
//	 */
//	@RequestMapping(method=RequestMethod.POST, value = "/deleteMemberFromGroup",produces = MediaType.APPLICATION_JSON_VALUE)
//	@ResponseBody
//	public LdapEntry deleteMemberFromGroup(LdapEntry selectedEntry) {
//		logger.info("AD Delete member from group. Dn: {} Member {}",selectedEntry.getDistinguishedName());
//		try {
//			service.updateEntryRemoveAttributeWithValue(selectedEntry.getDistinguishedName(),"","");
//			return selectedEntry;
//		} catch (LdapException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
	
	
	@Operation(summary = "Delete member from Ldap group", description = "")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Delete member from Ldap group"),
			  			  @ApiResponse(responseCode = "404", description = "Member is not found.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@DeleteMapping(value = "/member-from-group/{dn}",produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public  ResponseEntity<LdapEntry> deleteMembersOfGroup(@RequestParam(value="dn", required=true) String dn, 
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
				service.updateEntryRemoveAttributeWithValue(dn, "member", String.join(",", dnList));
				operationLogService.saveOperationLog(OperationType.DELETE,  dnList + " User has been deleted from group [Active Directory]", null);
				
			} catch (LdapException e) {
				e.printStackTrace();
	        	HttpHeaders headers = new HttpHeaders();
				headers.add("message", "Could not deleted member: " + e.getMessage());
	    		return ResponseEntity
	    				.status(HttpStatus.EXPECTATION_FAILED)
	    				.headers(headers)
	    				.build();
			}
		} else {
			for (int i = 0; i < dnList.size(); i++) {
				try {
					service.updateEntryRemoveAttributeWithValue(dn, "member", dnList.get(i));
					operationLogService.saveOperationLog(OperationType.DELETE, dnList.get(i) + "Users has been deleted from group [Active Directory]",null);
				} catch (LdapException e) {
					e.printStackTrace();
					HttpHeaders headers = new HttpHeaders();
					headers.add("message", "Could not deleted member: " + e.getMessage());
		    		return ResponseEntity
		    				.status(HttpStatus.EXPECTATION_FAILED)
		    				.headers(headers)
		    				.build();
				}
			}
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(service.getEntryDetail(dn));
				
	}
	
	
	/**
	 * getting user policies
	 * @param selectedEntry
	 * @return
	 */
	@Operation(summary = "Get user policies.", description = "")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Get user policies."),
			  			  @ApiResponse(responseCode = "417", description = "Failed to get user policies.Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/user-policies",produces = MediaType.APPLICATION_JSON_VALUE)	@ResponseBody
	public ResponseEntity<List<PolicyResponse>> getUserPolicies(LdapEntry selectedEntry) {
		logger.info("Getting user policies user Dn: {}",selectedEntry.getDistinguishedName());
		try {
			//get user groups
			List<LdapSearchFilterAttribute> filterAttributes = new ArrayList<LdapSearchFilterAttribute>();
			filterAttributes.add(new LdapSearchFilterAttribute("objectClass", "group", SearchFilterEnum.EQ));
			filterAttributes.add(new LdapSearchFilterAttribute("member", selectedEntry.getDistinguishedName(), SearchFilterEnum.EQ));
			List<LdapEntry> groups = service.search(service.getADDomainName(),filterAttributes, new String[] {"*"});

			
			List<PolicyResponse> userPolicies=new ArrayList<PolicyResponse>();
			// find policiy for user groups
			for (LdapEntry ldapEntry : groups) {
				List<PolicyResponse> policies= policyService.getPoliciesForGroup(ldapEntry.getDistinguishedName());
				userPolicies.addAll(policies);
			}
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(userPolicies);
			
		} catch (LdapException e) {
			e.printStackTrace();
			logger.error("Failed to get user policies.Error: " + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "Failed to get user policies. Error: " + e.getMessage());
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
	}
	
	@Operation(summary = "Get child user", description = "Created cild user")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Get child user"),
			  			  @ApiResponse(responseCode = "417", description = "Could not get child user.Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/child-user")
	public ResponseEntity<List<LdapEntry>>  getChildUSer(HttpServletRequest request,
			@RequestParam(value="searchDn", required=true) String searchDn,
			@RequestParam(value="key", required=true) String key, 
			@RequestParam(value="value", required=true) String value) {
		List<LdapEntry> results=null;
		
		logger.info("Search for key {} value {}  only users ",key, value);
		try {
			if(searchDn.equals("")) {
				searchDn=service.getADDomainName();
			}
			List<LdapSearchFilterAttribute> filterAttributes = new ArrayList<LdapSearchFilterAttribute>();
			filterAttributes.add(new LdapSearchFilterAttribute(key, value, SearchFilterEnum.EQ));
			filterAttributes.add(new LdapSearchFilterAttribute("objectclass", "person", SearchFilterEnum.EQ)); 
			results = service.search(searchDn,filterAttributes, new String[] {"*"});
		} catch (LdapException e) {
			e.printStackTrace();
			logger.error("Could not create child user. Error: " + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "Could not create child user. Error: " + e.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(results);
	}
	
	@Operation(summary = "Get child group", description = "Get child group")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Get child group"),
			  			  @ApiResponse(responseCode = "417", description = "Could not get child group.Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@GetMapping(value = "/child-group/searchDn/{searchDn}/key/{key}/value/{value}")
	public ResponseEntity<List<LdapEntry>>  getChildGroup(HttpServletRequest request,
			@RequestParam(value="searchDn", required=true) String searchDn,
			@RequestParam(value="key", required=true) String key, 
			@RequestParam(value="value", required=true) String value) {
		List<LdapEntry> results=null;
		
		logger.info("Search for key {} value {}  only users ",key, value);
		try {
			if(searchDn.equals("")) {
				searchDn=service.getADDomainName();
			}
			List<LdapSearchFilterAttribute> filterAttributes = new ArrayList<LdapSearchFilterAttribute>();
			filterAttributes.add(new LdapSearchFilterAttribute(key, value, SearchFilterEnum.EQ));
			results = service.search(searchDn,filterAttributes, new String[] {"*"});
		} catch (LdapException e) {
			e.printStackTrace();
			logger.error("Could not create child group. Error: " + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "Could not create child group. Error: " + e.getMessage());
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(results);
	}
	
//	get domainType and enableDelete4Directory parameters for AD management
	@Operation(summary = "Get AD configuration", description = "Creat AD configuration")
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Creat AD configuration"),
			  			  @ApiResponse(responseCode = "417", description = "Could not get AD configuration.Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@GetMapping(value = "/configurations", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getConfigParams() {
		HashMap<String, Object> configMap = new HashMap<String, Object>();
		configMap.put("domainType", configurationService.getDomainType());
		configMap.put("enableDelete4Directory", configurationService.getEnableDelete4Directory());
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(configMap);
	}
	
}
