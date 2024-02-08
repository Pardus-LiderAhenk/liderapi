package tr.org.lider.services;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonMappingException;

import tr.org.lider.entities.CommandExecutionImpl;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.entities.PluginTask;
import tr.org.lider.entities.TaskImpl;
import tr.org.lider.messaging.messages.ExecuteTaskMessageImpl;
import tr.org.lider.messaging.messages.FileServerConf;
import tr.org.lider.messaging.messages.ILiderMessage;
import tr.org.lider.messaging.messages.XMPPClientImpl;
import tr.org.lider.repositories.CommandExecutionRepository;
import tr.org.lider.repositories.CommandRepository;
import tr.org.lider.repositories.TaskRepository;
import tr.org.lider.utils.IRestResponse;



@Service
public class TaskSchedulerService {
	
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	private CommandExecutionRepository commandExecutionRepository;
	
	@Autowired
	private CommandRepository commandRepository;
	
	@Autowired
	private XMPPClientImpl messagingService;
	
	@Autowired
	private ConfigurationService configService;

	ILiderMessage scheduledMessage = null;
	
	public IRestResponse sendScheduledTaskMesasage() throws Throwable {
						
		List<CommandExecutionImpl> executionList = commandExecutionRepository.findCommandExecution();
		
		String taskJsonString = null;
		
		int limit = Math.min(5, executionList.size());

		for(int i=0;i<limit;i++) {		
			
			List<CommandImpl> commandList = commandRepository.findCommandId(executionList.get(i).getCommand().getId());
			List<TaskImpl> taskList = taskRepository.findByTask(commandList.get(0).getTask().getId());

			executionList.get(i).setCommanSend(true);
			commandExecutionRepository.save(executionList.get(i));
			
			taskJsonString = taskList.get(0).toString();
			FileServerConf fileServerConf=taskList.get(0).getPlugin().isUsesFileTransfer() ? configService.getFileServerConf(executionList.get(i).getUid().toLowerCase()) : null;

			scheduledMessage = new ExecuteTaskMessageImpl(taskJsonString, executionList.get(i).getUid(), new Date(), fileServerConf);
			messagingService.sendMessage(scheduledMessage);
			
		}
		
		
		
		return null;
		
	}
	
}
