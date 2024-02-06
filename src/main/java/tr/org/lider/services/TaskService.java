package tr.org.lider.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import tr.org.lider.entities.AgentStatus;
import tr.org.lider.entities.CommandExecutionImpl;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.entities.OperationType;
import tr.org.lider.entities.PluginTask;
import tr.org.lider.entities.TaskImpl;
import tr.org.lider.ldap.DNType;
import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.messaging.messages.ExecuteScheduledTaskMessageImpl;
import tr.org.lider.messaging.messages.ExecuteTaskMessageImpl;
import tr.org.lider.messaging.messages.FileServerConf;
import tr.org.lider.messaging.messages.ILiderMessage;
import tr.org.lider.messaging.messages.XMPPClientImpl;
import tr.org.lider.repositories.AgentRepository;
import tr.org.lider.repositories.CommandExecutionRepository;
import tr.org.lider.repositories.TaskRepository;
import tr.org.lider.utils.IRestResponse;
import tr.org.lider.utils.ResponseFactoryService;
import tr.org.lider.utils.RestResponseStatus;
import tr.org.lider.services.ConfigurationService;


@Service
public class TaskService {

	Logger logger = LoggerFactory.getLogger(TaskService.class);

//	@Autowired
//	private ConfigurationService configurationService;
	
	@Autowired
	private LDAPServiceImpl ldapService;

	@Autowired
	private ConfigurationService configService;

	@Autowired
	private XMPPClientImpl messagingService;

	@Autowired
	private CommandService commandService;

	@Autowired
	private ResponseFactoryService responseFactoryService;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private OperationLogService operationLogService; 
	
	@Autowired
	private PluginTaskService pluginTaskService;
	
	@Autowired
	private AgentRepository agentRepository;
	
	@Autowired
	private CommandExecutionRepository commandExecutionRepository;
	
	@Autowired
	private TaskSchedulerService taskSchedulerService;
	
	@PersistenceContext
	private EntityManager entityManager;


	public IRestResponse execute(PluginTask request){

		// Getting target entries 
		/**
		 * Getting target entries.. selected entries can be group ahhenk or organizational unit.
		 * targetEntries must be only Ahenk. 
		 * getTargetList method finds all suitable ahenks from selected group or organizational unit
		 * 
		 */
		List<LdapEntry> targetEntries = getTargetList(request);
	    //ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		// Create & persist task
		TaskImpl task= new TaskImpl(null, request.getPlugin(), request.getCommandId(), request.getParameterMap(), false,
				request.getCronExpression(), new Date(), null, request.isTaskParts());
		task = taskRepository.save(task);
		String commandIdForLog = task.getCommandClsId();
		List<PluginTask> pluginTask = pluginTaskService.findPluginTaskByCommandID(task.getCommandClsId());
		if (pluginTask != null && pluginTask.size() > 0) {
			commandIdForLog = pluginTask.get(0).getName();
		}
		String logMessage = "";
		String fragmentationLogMessage = "";
		if (request.getEntryList().get(0).getType().equals(DNType.GROUP)) {
			if(task.isTaskParts() == true) {
				fragmentationLogMessage = "[ "+ request.getEntryList().get(0).getDistinguishedName() + " ] istemci grubuna [ " + commandIdForLog + " ] görevi parçalı olarak gönderildi.";

			}else {
				
				logMessage = "[ "+ request.getEntryList().get(0).getDistinguishedName() + " ] istemci grubuna [ " + commandIdForLog + " ] görevi gönderildi.";

			}
			
		} 
		else {
			logMessage = "[ "+ request.getEntryList().get(0).get(configService.getAgentLdapJidAttribute()) +" ] istemciye [ " + commandIdForLog + " ] görevi gönderildi.";
		}
		try {
			if(task.isTaskParts() == true) {
				operationLogService.saveOperationLog(OperationType.EXECUTE_TASK, fragmentationLogMessage, task.getParameterMapBlob(), task.getId(), null, null);
			}
			else
				operationLogService.saveOperationLog(OperationType.EXECUTE_TASK, logMessage, task.getParameterMapBlob(), task.getId(), null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Task has an activation date, it will be sent to agent(s) on that date.

		List<String> uidList = new ArrayList<String>();

		for (LdapEntry entry : targetEntries) {
			if (ldapService.isAhenk(entry)) {
				uidList.add(entry.get(configService.getAgentLdapJidAttribute()));
				System.out.println(uidList + "  uid list getirliyo");
			}
		}

		CommandImpl command=null;

		try {

			    command = new CommandImpl(null, null, task, request.getDnList(), request.getDnType(), uidList, findCommandOwnerJid(),
			            ((PluginTask) request).getActivationDate(),null, new Date(), null, false, false);

		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(command!=null)
			commandService.addCommand(command);
		

		if (targetEntries != null && !targetEntries.isEmpty()) {
			 
			for (final LdapEntry entry : targetEntries) {

				boolean isAhenk = ldapService.isAhenk(entry);

				String uid = isAhenk ? entry.get(configService.getAgentLdapJidAttribute()) : null;

				logger.info("DN type: {}, UID: {}", entry.getType().toString(), uid);

				uid=uid.trim();

				Boolean isOnline=messagingService.isRecipientOnline(getFullJid(uid));
				
				CommandExecutionImpl execution = new CommandExecutionImpl();
				Boolean isTaskSend = false;
				if(task.isTaskParts() == false) {
					isTaskSend = true;
				}
				
					execution=	new CommandExecutionImpl(null, command, uid, entry.getType(), entry.getDistinguishedName(),new Date(), null, isOnline, isTaskSend);
					command.addCommandExecution(execution);
					
					// Task message
					ILiderMessage message = null;
					if (isAhenk) {
						// Set agent JID (the JID is UID of the LDAP entry)
						if (uid == null || uid.isEmpty()) {
							logger.error("JID was null. Ignoring task: {} for agent: {}",	new Object[] { task.toJson(), entry.getDistinguishedName() });
							continue;
						}
						logger.info("Sending task to agent w6ith JID: {}", uid);

						String taskJsonString = null;
						try {
							taskJsonString = task.toJson();
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}

						FileServerConf fileServerConf=request.getPlugin().isUsesFileTransfer() ? configService.getFileServerConf(uid.toLowerCase()) : null;
						// uid=jid
						message= new ExecuteTaskMessageImpl(taskJsonString, uid, new Date(), fileServerConf);

						// TaskStatusUpdateListener in XMPPClientImpl class
						try {
							if(!task.isTaskParts()) {
								messagingService.sendMessage(message);
							}
							taskSchedulerService.sendScheduledTaskMesasage();
							
						} catch (JsonGenerationException e) {
							e.printStackTrace();
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (NotConnectedException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					commandService.addCommandExecution(execution);
			}
		}
		return responseFactoryService.createResponse(RestResponseStatus.OK,"Task Basarı ile Gonderildi.");
	}
	
	
	private List<LdapEntry> getTargetList(PluginTask request) {
		List<LdapEntry> targetEntries= new ArrayList<>();
		List <LdapEntry> ldapEntryGroups = new ArrayList<>();
		List<LdapEntry> selectedtEntries= request.getEntryList();
		for (LdapEntry ldapEntry : selectedtEntries) {
			if(ldapEntry.getType().equals(DNType.AHENK)) {
				if(agentRepository.findByJid(ldapEntry.getUid()).get(0).getAgentStatus().equals(AgentStatus.Active)) {
					targetEntries.add(ldapEntry); 
				}
				
			}
			if(ldapEntry.getType().equals(DNType.GROUP)) {
				List <String> dnList= ldapService.getGroupInGroupsTask(ldapEntry);
				ldapEntryGroups = ldapService.getLdapDnStringToEntry(dnList);
				
					for(LdapEntry ldapEntryGroup : ldapEntryGroups) {
						String[] members= ldapEntryGroup.getAttributesMultiValues().get("member");
						//member sayısı alınıyor
						for (int i = 0; i < members.length; i++) {
							String dn = members[i];
							try {
								List<LdapEntry> member= ldapService.findSubEntries(dn, "(objectclass=pardusDevice)", new String[] { "*" }, SearchScope.OBJECT);
								if(member!=null && member.size()>0 ) {
									if(!ldapService.isExistInLdapEntry(targetEntries, member.get(0)))
										//if(agentRepository.findByJid(member.get(0).getUid()).get(0).getAgentStatus().equals(AgentStatus.Active)) {
											targetEntries.add(member.get(0));
										//}
										
								}
							} catch (LdapException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
			}
			if(ldapEntry.getType().equals(DNType.ORGANIZATIONAL_UNIT)) {

			}
		}


		return targetEntries;
	}

	public String buildKey(String pluginName, String pluginVersion, String commandId) {
		StringBuilder key = new StringBuilder();
		key.append(pluginName).append(":").append(pluginVersion).append(":").append(commandId);
		return key.toString().toUpperCase(Locale.ENGLISH);
	}

	public String getFullJid(String jid) {
		String jidFinal = jid;
		if (jid.indexOf("@") < 0) {
			jidFinal = jid + "@" + configService.getXmppServiceName();
		}
		return jidFinal;
	}


	private String findCommandOwnerJid() {
		if (AuthenticationService.isLogged()) {
			logger.info(" task owner jid : " + AuthenticationService.getUser().getName());
			return AuthenticationService.getUser().getName();
		}
		return null;
	}

	public List<Object[]> findExecutedTaskWithCount() {
		List<Object[]> tasks = taskRepository.findExecutedTaskWithCount(AuthenticationService.getUserName());
		return tasks;
	}
	
	public List<String> getCommandExecutions(List<LdapEntry> targetList) {
        return commandExecutionRepository.findCommandExecutionByUidList(targetList);
    }

}
