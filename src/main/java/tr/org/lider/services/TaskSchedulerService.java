package tr.org.lider.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mysql.cj.x.protobuf.MysqlxDatatypes.Array;

import tr.org.lider.entities.CommandExecutionImpl;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.entities.PluginTask;
import tr.org.lider.entities.TaskImpl;
import tr.org.lider.messaging.messages.ExecuteScheduledTaskMessageImpl;
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
	
	public IRestResponse sendScheduledTaskMesasage() throws Throwable, JsonMappingException, NotConnectedException, IOException {
						
		List<CommandExecutionImpl> executionList = commandExecutionRepository.findCommandExecution();
				
		for(int i=0;i<1;i++) {
			
			Long commandId = executionList.get(i).getCommand().getId();
			
			List<CommandImpl> commandList = commandRepository.findCommandId(commandId);
			List<TaskImpl> taskList = taskRepository.findByTask(commandList.get(i).getTask().getId());
			String taskJsonString = null;
			taskJsonString = taskList.toString();
			
			executionList.get(i).setCommanSend(true);	
			
			
			FileServerConf fileServerConf=taskList.get(i).getPlugin().isUsesFileTransfer() ? configService.getFileServerConf(executionList.get(i).getUid().toLowerCase()) : null;

			scheduledMessage = new ExecuteScheduledTaskMessageImpl(taskJsonString, executionList.get(i).getUid(), new Date(), fileServerConf);
			messagingService.sendMessage(scheduledMessage);
			
			//command execution constructorunu command send true yap yolla
		}
		
		
		
		return null;
		
	}
	
}
