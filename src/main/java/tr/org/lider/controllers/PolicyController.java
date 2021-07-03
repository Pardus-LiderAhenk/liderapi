package tr.org.lider.controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/policy")
public class PolicyController {

	Logger logger = LoggerFactory.getLogger(PolicyController.class);

	@Autowired
	private PolicyService policyService;

	//	return policies if deleted is false
	@RequestMapping(method=RequestMethod.POST ,value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PolicyImpl> policyList() {
		try {
			return policyService.list();
		} catch (DataAccessException e) {
			logger.error("Error list policy: " + e.getCause().getMessage());
			return null;
		}
	}

	//	return saved policy
	@RequestMapping(method=RequestMethod.POST ,value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public PolicyImpl policyAdd(@RequestBody PolicyImpl params) {
		try {
			return policyService.add(params);
		} catch (DataAccessException e) {
			logger.error("Error saving policy: " + e.getCause().getMessage());
			return null;
		}
	}

	//	return deleted policy. Never truly delete, just mark as deleted!
	@RequestMapping(method=RequestMethod.POST ,value = "/del", produces = MediaType.APPLICATION_JSON_VALUE)
	public PolicyImpl policyDel(@RequestBody PolicyImpl params) {
		try {
			return policyService.del(params);
		} catch (DataAccessException e) {
			logger.error("Error delete policy: " + e.getCause().getMessage());
			return null;
		}
	}

	//	return active policy
	@RequestMapping(method=RequestMethod.POST ,value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
	public PolicyImpl policyEnabled(@RequestBody PolicyImpl params) {
		try {
			return policyService.active(params);
		} catch (DataAccessException e) {
			e.printStackTrace();
			logger.error("Error active or passive policy: " + e.getCause().getMessage());
			return null;
		}
	}

	//	return passive policy
	@RequestMapping(method=RequestMethod.POST ,value = "/passive", produces = MediaType.APPLICATION_JSON_VALUE)
	public PolicyImpl policyDisabled(@RequestBody PolicyImpl params) {
		try {
			return policyService.passive(params);
		} catch (DataAccessException e) {
			e.printStackTrace();
			logger.error("Error passive policy: " + e.getCause().getMessage());
			return null;
		}
	}

	//	return updated policy
	@RequestMapping(method=RequestMethod.POST ,value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public PolicyImpl policyUpdated(@RequestBody PolicyImpl params) {
		try {
			return policyService.update(params);
		} catch (DataAccessException e) {
			e.printStackTrace();
			logger.error("Error updated policy: " + e.getCause().getMessage());
			return null;
		}
	}
	// executed policy
	@RequestMapping(method=RequestMethod.POST ,value = "/execute", produces = MediaType.APPLICATION_JSON_VALUE)
	public RestResponseImpl policyExecute(@RequestBody PolicyExecutionRequestImpl request) {
		policyService.executePolicy(request);
		return new RestResponseImpl(RestResponseStatus.OK, new ArrayList<>(), null);
	}
	
	// 
	@RequestMapping(method=RequestMethod.POST ,value = "/getPoliciesForGroup", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PolicyResponse> getPolicies4Group(@RequestBody LdapEntry dn) {
		logger.info("Getting executed policies for group. DN : " +dn);
		return policyService.getPoliciesForGroup(dn.getDistinguishedName());
	}
	
	@RequestMapping(method=RequestMethod.POST ,value = "/unassignment", produces = MediaType.APPLICATION_JSON_VALUE)
	public CommandImpl unassignmentPolicyOfUser(@RequestBody CommandImpl id) {
		logger.info("Getting executed policies for group. DN : " +id);
		return policyService.unassignmentCommandForUserPolicy(id);
	}
}
