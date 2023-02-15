package tr.org.lider.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import tr.org.lider.entities.CommandExecutionImpl;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.entities.OperationType;
import tr.org.lider.entities.PolicyImpl;
import tr.org.lider.ldap.ILDAPService;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.messaging.enums.DomainType;
import tr.org.lider.models.PolicyExecutionRequestImpl;
import tr.org.lider.models.PolicyResponse;
import tr.org.lider.repositories.PolicyRepository;

/**
 * Service for getting policy parameters from database and added, updated and deleted policy to database.
 * 
 * @author <a href="mailto:tuncay.colak@tubitak.gov.tr">Tuncay ÇOLAK</a>
 * 
 */

@Service
public class PolicyService {
	Logger logger = LoggerFactory.getLogger(PolicyService.class);

	@Autowired
	private PolicyRepository policyRepository;

	@Autowired
	private CommandService commandService;

	@Autowired
	@Qualifier("ldapImpl")
	private ILDAPService ldapService;

	@Autowired
	@Qualifier("AdImpl")
	private ILDAPService adService;
	
	@Autowired
	private OperationLogService operationLogService; 
	
	@Autowired
	private ConfigurationService configService;

	public void executePolicy(PolicyExecutionRequestImpl request) {
		logger.debug("Finding Policy by requested policyId.");
		PolicyImpl policy = findPolicyByID(request.getId());
		logger.debug("Creating ICommand object.");
		CommandImpl command = createCommanEntity(request, policy);
		/*
		 * target entry must be group..
		 * all policies send only group entry.
		 */

		List<LdapEntry> targetEntries= getTargetList(request.getDnList());
		
		String logMessage = "[ "+ request.getDnList().get(0) +" ] kullanıcı grubuna [ " + policy.getLabel() + " ] politikası uygulandı.";
		operationLogService.saveOperationLog(OperationType.EXECUTE_POLICY, logMessage, policy.getLabel().getBytes(), null, policy.getId(), null);

		for (LdapEntry targetEntry : targetEntries) {
			String uid=targetEntry.get(configService.getAgentLdapIdAttribute()); // group uid is cn value.
			CommandExecutionImpl commandExecutionImpl=	new CommandExecutionImpl(null, (CommandImpl) command, uid, targetEntry.getType(), targetEntry.getDistinguishedName(),
					new Date(), null, false);

			command.addCommandExecution(commandExecutionImpl);
		}
		if(command!=null)
			commandService.addCommand(command);


	}

	private CommandImpl createCommanEntity(PolicyExecutionRequestImpl request, PolicyImpl policy) {
		CommandImpl command=null;
		try {
			command= new CommandImpl(null, policy, null, request.getDnList(), request.getDnType(), null, findCommandOwnerJid(), 
					request.getActivationDate(), 
					request.getExpirationDate(), new Date(), null, false, false);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return command;
	}

	public List<PolicyImpl> list( ){
		Boolean deleted = false;
		return policyRepository.findAllByDeleted(deleted);
	}
	
	public List<PolicyImpl> activePolicies( ){
		Boolean active = true;
		Boolean deleted = false;
		return policyRepository.findAllByActiveAndDeleted(active, deleted);
	}

	public PolicyImpl add(PolicyImpl policy) {
		policy.setCommandOwnerUid(null);
		PolicyImpl existPolicy = policyRepository.save(policy);
		existPolicy.setPolicyVersion(existPolicy.getId()+"-"+ 1);
		String logMessage = "[ "+ existPolicy.getLabel() + " ] politikası oluşturuldu.";
		operationLogService.saveOperationLog(OperationType.CREATE, logMessage, existPolicy.getLabel().getBytes(), null, existPolicy.getId(), null);
		return policyRepository.save(existPolicy);
	}

	public PolicyImpl delete(Long id) {
		PolicyImpl existingPolicy = policyRepository.findOne(id);
		existingPolicy.setDeleted(true);
		existingPolicy.setModifyDate(new Date());
		String logMessage = "[ "+ existingPolicy.getLabel() + " ] politikası silindi.";
		operationLogService.saveOperationLog(OperationType.DELETE, logMessage, existingPolicy.getLabel().getBytes(), null, existingPolicy.getId(), null);
		return policyRepository.save(existingPolicy);
	}

	public PolicyImpl update(PolicyImpl policy) {
		PolicyImpl existPolicy = policyRepository.findOne(policy.getId());
		existPolicy.setLabel(policy.getLabel());
		existPolicy.setActive(policy.isActive());
		existPolicy.setDescription(policy.getDescription());
		existPolicy.setProfiles(policy.getProfiles());
		String oldVersion = existPolicy.getPolicyVersion().split("-")[1];
		Integer newVersion = new Integer(oldVersion) + 1;
		existPolicy.setPolicyVersion(policy.getId() + "-" + newVersion);
		existPolicy.setModifyDate(new Date());
		String logMessage = "[ "+ existPolicy.getLabel() + " ] politikası güncellendi.";
		operationLogService.saveOperationLog(OperationType.UPDATE, logMessage, existPolicy.getLabel().getBytes(), null, existPolicy.getId(), null);
		return policyRepository.save(existPolicy);
	}

	public PolicyImpl active(PolicyImpl policy) {
		PolicyImpl existPolicy = policyRepository.findOne(policy.getId());
		existPolicy.setActive(true);
		existPolicy.setModifyDate(new Date());
		String logMessage = "[ "+ existPolicy.getLabel() + " ] politikası aktif edildi.";
		operationLogService.saveOperationLog(OperationType.UPDATE, logMessage, existPolicy.getLabel().getBytes(), null, existPolicy.getId(), null);
		return policyRepository.save(existPolicy);
	}

	public PolicyImpl passive(PolicyImpl policy) {
		PolicyImpl existPolicy = policyRepository.findOne(policy.getId());
		existPolicy.setActive(false);
		existPolicy.setModifyDate(new Date());
		String logMessage = "[ "+ existPolicy.getLabel() + " ] politikası pasif edildi.";
		operationLogService.saveOperationLog(OperationType.UPDATE, logMessage, existPolicy.getLabel().getBytes(), null, existPolicy.getId(), null);
		return policyRepository.save(existPolicy);
	}

	public PolicyImpl findPolicyByID(Long id) {
		return policyRepository.findOne(id);
	}

	public PolicyImpl updateVersion(PolicyImpl policy) {
		String oldVersion = policy.getPolicyVersion().split("-")[1];
		Integer newVersion = new Integer(oldVersion) + 1;
		policy.setPolicyVersion(policy.getId() + "-" + newVersion);
		policy.setModifyDate(new Date());
		return policyRepository.save(policy);
	}

	private List<LdapEntry> getTargetList(List<String> selectedDns) {
		List<LdapEntry> targetEntries= new ArrayList<>();
		for (String dn : selectedDns) {
			try {
				List<LdapEntry> member=null;
				if(configService.getDomainType().equals(DomainType.ACTIVE_DIRECTORY)) {
					member=adService.findSubEntries(dn, "(objectclass=*)", new String[] { "*" }, SearchScope.OBJECT);
				}
				else if(configService.getDomainType().equals(DomainType.LDAP) || configService.getDomainType().equals(DomainType.NONE)) {
					member=ldapService.findSubEntries(dn, "(objectclass=*)", new String[] { "*" }, SearchScope.OBJECT);
				}
				if(member!=null && member.size()>0) {
					targetEntries.add(member.get(0));
				}
			} catch (LdapException e) {
				e.printStackTrace();
			}		
		}
		return targetEntries;
	}

	private String findCommandOwnerJid() {
		if (AuthenticationService.isLogged()) {
			logger.info(" task owner jid : " + AuthenticationService.getUser().getName());
			return AuthenticationService.getUser().getName();
		}
		return null;
	}

	public List<PolicyResponse> getPoliciesForGroup(String dn) {
		List<Object[]> results = policyRepository.findPoliciesByGroupDn(dn);
		List<PolicyResponse> resp= new ArrayList<PolicyResponse>();
		for (Object[] objects : results) {
			PolicyResponse policyResponse= new PolicyResponse();
			policyResponse.setPolicyImpl((PolicyImpl)objects[0]);
			policyResponse.setCommandExecutionImpl((CommandExecutionImpl)objects[1]);
			policyResponse.setCommandImpl((CommandImpl)objects[2]);
			resp.add(policyResponse);
		}
		return resp;
	}

	public CommandImpl unassignmentCommandForUserPolicy(CommandImpl comImpl) {
		CommandImpl existCommand = commandService.getCommand(comImpl.getId());
		existCommand.setDeleted(true);
		String logMessage = "[ "+ existCommand.getDnList().get(0) +" ] kullanıcı grubunun [ " + existCommand.getPolicy().getLabel() + " ] politikası kaldırıldı.";
		operationLogService.saveOperationLog(OperationType.UNASSIGMENT_POLICY, logMessage, existCommand.getPolicy().getLabel().getBytes(), null, existCommand.getPolicy().getId(), null);
		return commandService.updateCommand(existCommand);
	}
}