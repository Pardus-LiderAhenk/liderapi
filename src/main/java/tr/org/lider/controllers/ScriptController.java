package tr.org.lider.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.constant.RoleConstants;
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

	@Secured({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_COMPUTERS, RoleConstants.ROLE_SCRIPT_DEFINITION, RoleConstants.ROLE_SCRIPT })
	@Operation(summary = "Get script list with filters", description = "Returns a list of scripts with optional filters", tags = {"script" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Returns script list. Successful"),
			@ApiResponse(responseCode = "417", description = "Could not get script list. Unexpected error occurred", 
				content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/list/page-size/{pageSize}/page-number/{pageNumber}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<ScriptTemplate>> scriptList(
			@PathVariable int pageSize,
			@PathVariable int pageNumber,
			@RequestBody Map<String, String> params) {

		return ResponseEntity
				.status(HttpStatus.OK)
				.body(scriptService.list(pageNumber, pageSize, params));
	}
	
//	get script list all as no pagging
    @Secured({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_COMPUTERS, RoleConstants.ROLE_SCRIPT_DEFINITION, RoleConstants.ROLE_SCRIPT })
	@Operation(summary = "Get script list all", description = "", tags = { "script" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns script list all. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not get script list all. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@GetMapping(value = "/list-all", produces = MediaType.APPLICATION_JSON_VALUE)
	//@RequestMapping(method=RequestMethod.POST ,value = "/list-all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ScriptTemplate>> scriptListAll() {
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(scriptService.listAll());
				
	}



	@Secured({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_SCRIPT_DEFINITION, RoleConstants.ROLE_SCRIPT })
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

	@Secured({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_SCRIPT_DEFINITION, RoleConstants.ROLE_SCRIPT })
	@Operation(summary = "Delete script", description = "", tags = { "script" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Deleted script. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not delete script. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@DeleteMapping(value = "/delete/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ScriptTemplate> scriptDel(@PathVariable Long id){
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(scriptService.delete(id));
				
	}

	@Secured({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_SCRIPT_DEFINITION, RoleConstants.ROLE_SCRIPT })
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
