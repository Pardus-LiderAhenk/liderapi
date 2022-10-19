package tr.org.lider.controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.entities.PolicyImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.models.PolicyExecutionRequestImpl;
import tr.org.lider.models.PolicyResponse;
import tr.org.lider.services.PolicyService;
import tr.org.lider.utils.RestResponseImpl;
import tr.org.lider.utils.RestResponseStatus;

/**
 * 
 * Return the policies, saved, edited and deleted policy
 * @author <a href="mailto:tuncay.colak@tubitak.gov.tr">Tuncay ÇOLAK</a>
 *
 */
@Secured({"ROLE_ADMIN", "ROLE_COMPUTERS" })
@RestController
@RequestMapping("/api/policy")
@Tag(name = "" , description = "")
public class PolicyController {

	Logger logger = LoggerFactory.getLogger(PolicyController.class);

	@Autowired
	private PolicyService policyService;

	//	return policies if deleted is false
	
	//@RequestMapping(method=RequestMethod.POST ,value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	
	@PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<PolicyImpl>> policyList() {
		try {
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(policyService.list());
					
		} catch (DataAccessException e) {
			logger.error("Error list policy: " + e.getCause().getMessage());
			HttpHeaders headers = new HttpHeaders();
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
	}

	//	return saved policy
	//@RequestMapping(method=RequestMethod.POST ,value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	
	@PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PolicyImpl> policyAdd(@RequestBody PolicyImpl params) {
		try {
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(policyService.add(params));
					
		} catch (DataAccessException e) {
			logger.error("Error saving policy: " + e.getCause().getMessage());
			HttpHeaders headers = new HttpHeaders();
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
	}

	//	return deleted policy. Never truly delete, just mark as deleted!
	@DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PolicyImpl> policyDelete(@RequestBody PolicyImpl params) {
		try {
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(policyService.delete(params));
					
		} catch (DataAccessException e) {
			logger.error("Error delete policy: " + e.getCause().getMessage());
			HttpHeaders headers = new HttpHeaders();
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
	}

	//	return active policy
	@PostMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PolicyImpl> policyEnabled(@RequestBody PolicyImpl params) {
		try {
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(policyService.active(params));
					
		} catch (DataAccessException e) {
			e.printStackTrace();
			logger.error("Error active or passive policy: " + e.getCause().getMessage());
			HttpHeaders headers = new HttpHeaders();
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
	}

	//	return passive policy
	@PostMapping(value = "/passive", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PolicyImpl> policyDisabled(@RequestBody PolicyImpl params) {
		try {
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(policyService.passive(params));
					
		} catch (DataAccessException e) {
			e.printStackTrace();
			logger.error("Error passive policy: " + e.getCause().getMessage());
			HttpHeaders headers = new HttpHeaders();
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
	}

	//	return updated policy
	@PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PolicyImpl> policyUpdated(@RequestBody PolicyImpl params) {
		try {
			return ResponseEntity
					.status(HttpStatus.OK)
					.body(policyService.update(params));
			
		} catch (DataAccessException e) {
			e.printStackTrace();
			logger.error("Error updated policy: " + e.getCause().getMessage());
			HttpHeaders headers = new HttpHeaders();
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
	}
	// executed policy
	@RequestMapping(method=RequestMethod.POST ,value = "/execute", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> policyExecute(@RequestBody PolicyExecutionRequestImpl request) {
		policyService.executePolicy(request);
		//return new RestResponseImpl(RestResponseStatus.OK, new ArrayList<>(), null);
		return ResponseEntity.status(HttpStatus.OK).body("Task is executed.");
			
	}
	
	// 
	@RequestMapping(method=RequestMethod.POST ,value = "/getPoliciesForGroup", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PolicyResponse> getPolicies4Group(@RequestBody LdapEntry dn) {
		logger.info("Getting executed policies for group. DN : " +dn.getDistinguishedName());
		return policyService.getPoliciesForGroup(dn.getDistinguishedName());
	}
	
	@RequestMapping(method=RequestMethod.POST ,value = "/unassignment", produces = MediaType.APPLICATION_JSON_VALUE)
	public CommandImpl unassignmentPolicyOfUser(@RequestBody CommandImpl id) {
		logger.info("Getting executed policies for group. DN : " +id);
		return policyService.unassignmentCommandForUserPolicy(id);
	}
	
	@RequestMapping(method=RequestMethod.POST ,value = "/activePolicies", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PolicyImpl> activePolicyList() {
		try {
			return policyService.activePolicies();
		} catch (DataAccessException e) {
			logger.error("Error get active policy list: " + e.getCause().getMessage());
			HttpHeaders headers = new HttpHeaders();
    		return null;
		}
	}
}
