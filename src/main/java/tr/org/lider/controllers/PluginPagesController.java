package tr.org.lider.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.entities.OperationLogImpl;
import tr.org.lider.entities.PluginTask;
import tr.org.lider.services.PluginService;


/**
 * 
 * Plugin pages rendered with tasks and policies..
 * @author M. Edip YILDIZ
 *
 **/
@RestController
@Tag(name = "" ,description = "")
public class PluginPagesController {
	
	Logger logger = LoggerFactory.getLogger(PluginPagesController.class);
	
	@Autowired
	public PluginService pluginService;
	
	@Operation(summary = "", description = "", tags = { "" })
	@ApiResponses(value = { 
      	  @ApiResponse(responseCode = "200", description = "",
			  content = { @Content(schema = @Schema(implementation = OperationLogImpl.class))}),
		  @ApiResponse(responseCode = "417",description = "",
	   		 content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/api/get-plugin-task-list")
	//@RequestMapping(value="/getPluginTaskList", method = {RequestMethod.POST })
	public ResponseEntity<List<PluginTask>> getPluginTaskList(Model model, PluginTask pluginTask) {
		List<PluginTask>  list = pluginService.findAllPluginTask();
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(list);
				
	}

}
