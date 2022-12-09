package tr.org.lider.controllers;

import java.util.List;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.entities.ConkyTemplate;
import tr.org.lider.services.ConkyService;

/**
 * 
 * Return the Conky Template list, saved, edited and deleted Conky
 * @author <a href="mailto:tuncay.colak@tubitak.gov.tr">Tuncay ÇOLAK</a>
 *
 */

@RestController
@RequestMapping("/api/conky")
@Tag(name = "Conky",description = "Conky Rest Service")
public class ConkyController {

	@Autowired
	private ConkyService conkyService;
	
//	get conky list with pagging
	@Secured({"ROLE_ADMIN", "ROLE_CONKY_DEFINITION", "ROLE_COMPUTERS" })
	@Operation(summary = "Get conky list", description = "", tags = { "conky-service" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns conky list. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not get conky list. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@GetMapping(value = "/list/page-size/{pageSize}/page-number/{pageNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
	//@RequestMapping(method=RequestMethod.POST ,value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Page<ConkyTemplate>> conkyList(
			@PathVariable int pageSize, @PathVariable int pageNumber
			) {
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(conkyService.list(pageNumber, pageSize));
	}
	
//	get conky list all as no pagging
	@Secured({"ROLE_ADMIN", "ROLE_CONKY_DEFINITION", "ROLE_COMPUTERS" })
	@Operation(summary = "Get conky list all", description = "", tags = { "conky-service" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Returns conky list all. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not get conky list all. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@GetMapping(value = "/list-all", produces = MediaType.APPLICATION_JSON_VALUE)
	//@RequestMapping(method=RequestMethod.POST ,value = "/list-all", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ConkyTemplate>> conkyListAll() {
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(conkyService.listAll());
	}

	@Secured({"ROLE_ADMIN", "ROLE_CONKY_DEFINITION" })
	@Operation(summary = "Add conky service", description = "", tags = { "conky-service" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Added conky service. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not add conky service. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	//@RequestMapping(method=RequestMethod.POST ,value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ConkyTemplate> notifyAdd(@RequestBody ConkyTemplate template){
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(conkyService.add(template));
	}
	
	@Secured({"ROLE_ADMIN", "ROLE_CONKY_DEFINITION" })
	@Operation(summary = "Delete conky service", description = "", tags = { "conky-service" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Deleted conky service. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not delete conky. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@DeleteMapping(value = "/delete/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//RequestMapping(method=RequestMethod.POST ,value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ConkyTemplate> notifyDel(@PathVariable Long id){
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(conkyService.delete(id));
	}
	
	@Secured({"ROLE_ADMIN", "ROLE_CONKY_DEFINITION" })
	@Operation(summary = "Update conky", description = "", tags = { "conky-service" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Updated conky. Successful"),
			  @ApiResponse(responseCode = "417", description = "Could not update conky. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	//@RequestMapping(method=RequestMethod.POST ,value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ConkyTemplate> notifyUpdate(@RequestBody ConkyTemplate template){
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(conkyService.update(template));
	}
}
