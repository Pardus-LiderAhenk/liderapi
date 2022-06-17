package tr.org.lider.sambabox;

import java.util.HashMap;
import java.util.Map;

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.AddRequest;
import org.apache.directory.api.ldap.model.message.AddRequestImpl;
import org.apache.directory.api.ldap.model.message.AddResponse;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.security.CustomPasswordEncoder;
import tr.org.lider.services.ConfigurationService;

@RestController()
@RequestMapping("/api/sambabox")
public class SambaboxController {
	
	private final static Logger logger = LoggerFactory.getLogger(SambaboxController.class);
	
	@Autowired
	private LDAPServiceImpl ldapService;
	
	@Autowired
	private CustomPasswordEncoder customPasswordEncoder;
	
	@Autowired
	private ConfigurationService configurationService;
	
	
	@Operation(summary = "Create user for sambabox", description = "", tags = { "sambabox" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "User created", 
			    content = { @Content(schema = @Schema(implementation = String.class)) }),
			  @ApiResponse(responseCode = "400", description = "User not created", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/add-user", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> createNewUser(@RequestBody UserDto user) {
		
		Map<String, String[]> attributes = new HashMap<String, String[]>();
		attributes.put("objectClass", new String[] { "top",
				"person","pardusLider","pardusAccount","organizationalPerson","inetOrgPerson"});
		attributes.put("cn", new String[] { user.getUsername() });
		attributes.put("sn", new String[] {  user.getUsername()  });
		attributes.put("uid", new String[] {  user.getUsername()  });
		attributes.put("userPassword", new String[] { "{ARGON2}" + customPasswordEncoder.encode(user.getPassword()) });
		attributes.put("liderPrivilege", new String[] {"ROLE_USER"});
		
		
		
		String rdn="uid="+user.getUsername()+","+configurationService.getUserLdapBaseDn();
		
		try {
			addEntry(rdn, attributes);
		} catch (LdapException e) {
			HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "User not created");
    		return ResponseEntity
    				.status(HttpStatus.BAD_REQUEST)
    				.headers(headers)
    				.build();
		}
		
		return ResponseEntity.status(HttpStatus.OK).body("");
	}
	
	
	private void addEntry(String newDn, Map<String, String[]> attributes) throws LdapException {
		LdapConnection connection = null;
		try {
			connection = ldapService.getConnectionForAdmin();
			Dn dn = new Dn(newDn);
			Entry entry = new DefaultEntry(dn);

			for (Map.Entry<String, String[]> Entry : attributes.entrySet()) {
				String[] entryValues = Entry.getValue();
				for (String value : entryValues) {
					entry.add(Entry.getKey(), value);
				}
			}

			AddRequest addRequest = new AddRequestImpl();
			addRequest.setEntry(entry);

			AddResponse addResponse = connection.add(addRequest);
			LdapResult ldapResult = addResponse.getLdapResult();

			if (ResultCodeEnum.SUCCESS.equals(ldapResult.getResultCode())) {
				return;
			} else {
				logger.error("Could not create LDAP entry: {}", ldapResult.getDiagnosticMessage());
				throw new LdapException(ldapResult.getDiagnosticMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw new LdapException(e);
		} finally {
			ldapService.releaseConnection(connection);
		}
	}
	
	
	@Operation(summary = "Update user password for sambabox", description = "", tags = { "sambabox user password" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Password updated", 
			    content = { @Content(schema = @Schema(implementation = String.class)) }),
			  @ApiResponse(responseCode = "400", description = "Password not updated", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/update-user-password", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updatePassword(@RequestBody UserDto user) {
		String dn = "uid="+user.getUsername()+","+configurationService.getUserLdapBaseDn();
		
		try {
			updateEntry(dn, "userPassword", "{ARGON2}" + customPasswordEncoder.encode(user.getPassword()));
		} catch (LdapException e) {
			HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "Changing user password unsuccess!");
    		return ResponseEntity
    				.status(HttpStatus.BAD_REQUEST)
    				.headers(headers)
    				.build();
		}
		
		return ResponseEntity.status(HttpStatus.OK).body("Success");
	}
	
	private void updateEntry(String entryDn, String attribute, String value) throws LdapException {
		logger.info("Replacing attribute " + attribute + " value " + value);
		LdapConnection connection = null;

		connection = ldapService.getConnectionForAdmin();
		Entry entry = null;
		try {
			entry = connection.lookup(entryDn);
			if (entry != null) {
				if (entry.get(attribute) != null) {
					Value<?> oldValue = entry.get(attribute).get();
					entry.remove(attribute, oldValue);
				}
				entry.add(attribute, value);
				connection.modify(entry, ModificationOperation.REPLACE_ATTRIBUTE);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new LdapException(e);
		} finally {
			ldapService.releaseConnection(connection);
		}
	}
	
	
	@Operation(summary = "Update user status (active, inactive) for sambabox", description = "", tags = { "sambabox user status" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Status changed", 
			    content = { @Content(schema = @Schema(implementation = String.class)) }),
			  @ApiResponse(responseCode = "400", description = "Status not changed", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/change-user-status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> changeUserStatus(@RequestBody UserStatusDto userStatus) {
		String dn = "uid="+userStatus.getUsername()+","+configurationService.getUserLdapBaseDn();
		if (!userStatus.getStatus()) {
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
			} catch (Exception e) {
				HttpHeaders headers = new HttpHeaders();
	        	headers.add("message", "Inactivating user unsuccess!");
	    		return ResponseEntity
	    				.status(HttpStatus.BAD_REQUEST)
	    				.headers(headers)
	    				.build();
			}
		} else {
			
			try {
				updateEntryAddAtribute(dn, "liderPrivilege", "ROLE_USER");
			} catch (LdapException e) {
				HttpHeaders headers = new HttpHeaders();
	        	headers.add("message", "Activating user unsuccess!");
	    		return ResponseEntity
	    				.status(HttpStatus.BAD_REQUEST)
	    				.headers(headers)
	    				.build();
			}
		}
		
		return ResponseEntity.status(HttpStatus.OK).body("Success");
	}
	
	public void updateEntryAddAtribute(String entryDn, String attribute, String value) throws LdapException {
		logger.info("Adding attribute " + attribute + " value " + value);
		LdapConnection connection = null;

		connection = ldapService.getConnectionForAdmin();
		Entry entry = null;
		try {
			entry = connection.lookup(entryDn);
			if (entry != null) {
				entry.put(attribute, value);

				ModifyRequest mr = new ModifyRequestImpl();
				mr.setName(new Dn(entryDn));
				mr.add(attribute, value);

				connection.modify(mr);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new LdapException(e);
		} finally {
			ldapService.releaseConnection(connection);
		}
	}

}
