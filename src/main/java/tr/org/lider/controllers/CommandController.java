package tr.org.lider.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.entities.CommandExecutionResultImpl;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.services.CommandService;

@RestController
@RequestMapping("/api/command")
@Tag(name = "command-service", description = "Command Service Controller")
public class CommandController {

	@Autowired
	CommandService commandService;
	
	//TODO: method kullanılmıyor 
	@RequestMapping(method=RequestMethod.POST)
	public List<CommandImpl> findAllCommandByDNList(@RequestParam(value ="dn") String dn) {
		return commandService.getExecutedTasks(dn);
	}
	
	
	@Operation(summary = "execution command", description = "", tags = { "command-service" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Command executed."),
			  @ApiResponse(responseCode = "417", description = "Command could not be run. Unexpected error occured.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/command-execution-result",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CommandExecutionResultImpl>   getCommandExecutionResult(@RequestParam(value="id") Long id) {
		CommandExecutionResultImpl cer =  commandService.getCommandExecutionResultByID(id);
		if(cer.getResponseData() != null)
			cer.setResponseDataStr((new String(cer.getResponseData())));
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(commandService.getCommandExecutionResultByID(id));
	}

}