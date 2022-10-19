package tr.org.lider.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.entities.ScriptTemplate;
import tr.org.lider.services.ScriptService;

/**
 * 
 * Return the script templates, saved, edited and deleted script
 * @author <a href="mailto:tuncay.colak@tubitak.gov.tr">Tuncay ÇOLAK</a>
 *
 */
@RestController
@RequestMapping("/api/script")
@Tag(name = "Script", description = "Script Rest Service")
public class ScriptController {

	@Autowired
	private ScriptService scriptService;
	
	@Secured({"ROLE_ADMIN", "ROLE_SCRIPT_DEFINITION", "ROLE_COMPUTERS" })
	@Operation(summary = "Get script list", description = "", tags = { "script" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns script list. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not get script list. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<ScriptTemplate>> scriptList(
			@RequestParam (value = "pageSize") int pageSize,
			@RequestParam (value = "pageNumber") int pageNumber
			) {
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(scriptService.list(pageNumber, pageSize));
				
	}

	@Secured({"ROLE_ADMIN", "ROLE_SCRIPT_DEFINITION" })
	@Operation(summary = "Add script", description = "", tags = { "script" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Added script. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not add script. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ScriptTemplate> scriptAdd(@RequestBody ScriptTemplate script){
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(scriptService.add(script));			
	}
	
	@Secured({"ROLE_ADMIN", "ROLE_SCRIPT_DEFINITION" })
	@Operation(summary = "Delete script", description = "", tags = { "script" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Deleted script. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not delete script. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ScriptTemplate> scriptDel(@RequestBody ScriptTemplate script){
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(scriptService.delete(script));
				
	}
	
	@Secured({"ROLE_ADMIN", "ROLE_SCRIPT_DEFINITION" })
	@Operation(summary = "Update script", description = "", tags = { "script" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Updated script. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not update script. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ScriptTemplate scriptUpdate(@RequestBody ScriptTemplate script){
		return scriptService.update(script);
	}
}
