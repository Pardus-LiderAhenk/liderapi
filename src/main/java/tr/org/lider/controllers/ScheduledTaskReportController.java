package tr.org.lider.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tr.org.lider.entities.CommandExecutionImpl;
import tr.org.lider.entities.CommandExecutionResultImpl;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.entities.PluginTask;
import tr.org.lider.services.CommandService;
import tr.org.lider.services.ExcelExportService;
import tr.org.lider.services.PluginTaskService;
import tr.org.lider.services.ScheduledTaskReportService;

@Secured({"ROLE_ADMIN", "ROLE_EXECUTED_TASK" })
@RestController
@RequestMapping("lider/scheduledTaskReport")
public class ScheduledTaskReportController {

	@Autowired
	private ScheduledTaskReportService scheduledTaskService;
	
	@Autowired
	private CommandService commandService;

	@Autowired
	private PluginTaskService pluginTaskService;
	
	@Autowired
	private ExcelExportService excelService;
	
	@RequestMapping(value="/getInnerHtmlPage", method = {RequestMethod.POST })
	public String getInnerHtmlPage(@RequestParam (value = "innerPage", required = true) String innerPage) {
		return innerPage;
	}

	@RequestMapping(method=RequestMethod.POST, value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<CommandImpl> findAllCommandsRest(@RequestParam (value = "pageNumber") int pageNumber,
			@RequestParam (value = "pageSize") int pageSize,
			@RequestParam (value = "taskCommand") Optional<String> taskCommand,
			@RequestParam (value="startDate") @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss") Optional<Date> startDate,
			@RequestParam (value="endDate") @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss") Optional<Date> endDate) {
		Page<CommandImpl> commands = scheduledTaskService.findAllCommandsFiltered(pageNumber, pageSize, taskCommand, startDate, endDate);
		for (CommandImpl command : commands.getContent()) {
			for (CommandExecutionImpl commandExecution : command.getCommandExecutions()) {
				for (CommandExecutionResultImpl commandExecutionResult : commandExecution.getCommandExecutionResults()) {
					if(commandExecutionResult.getResponseData() != null) {
						commandExecutionResult.setResponseDataStr((new String(commandExecutionResult.getResponseData())));
					}
				}
			}
		}
		return commands;
	}

	@RequestMapping(method=RequestMethod.POST, value = "/plugins", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PluginTask> getPlugins() {
		return pluginTaskService.findAll();
	}
	
	@RequestMapping(method=RequestMethod.POST, value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> export(
			@RequestParam (value = "taskCommand") Optional<String> taskCommand,
			@RequestParam (value="startDate") @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss") Optional<Date> startDate,
			@RequestParam (value="endDate") @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss") Optional<Date> endDate
			) {
			
		Page<CommandImpl> commands = scheduledTaskService.findAllCommandsFiltered(1, commandService.count().intValue(), taskCommand, startDate, endDate);
		for (CommandImpl command : commands.getContent()) {
			for (CommandExecutionImpl commandExecution : command.getCommandExecutions()) {
				for (CommandExecutionResultImpl commandExecutionResult : commandExecution.getCommandExecutionResults()) {
					if(commandExecutionResult.getResponseData() != null) {
						commandExecutionResult.setResponseDataStr((new String(commandExecutionResult.getResponseData())));
					}
				}
			}
		}
		
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("fileName", "Zamanlı Çalıştırılan Görevler Raporu" + new SimpleDateFormat("dd_MM_yyyy_HH:mm:ss.SSS").format(new Date()) + ".xlsx");
		headers.setContentType(MediaType.parseMediaType("application/csv"));
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
		return new ResponseEntity<byte[]>(excelService.generateTaskReport(commands.getContent()), headers,  HttpStatus.OK);
	}

}