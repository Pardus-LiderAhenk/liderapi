package tr.org.lider.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tr.org.lider.entities.AgentImpl;
import tr.org.lider.services.AgentSessionReportService;
import tr.org.lider.services.ExcelExportService;
import tr.org.lider.services.UserSessionReportService;

@Secured({"ROLE_ADMIN", "ROLE_USER_SESSION_REPORT" })
@RestController
@RequestMapping("/api/lider/agent-session")
@Tag(name = "Agent session service", description = "Agent Sesion Report controller")
public class AgentSessionReportController {
	
	private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AgentSessionReportService agentSessionReportService;
	
	@Autowired
	private UserSessionReportService userSessionReportService;
	
	@Autowired
	private ExcelExportService excelService;
	
	@Operation(summary = "Find all agents", description = "", tags = { "agent-service" })
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Find all agents", 
				content = { @Content(schema = @Schema(implementation = AgentImpl.class)) 
				}), 
			@ApiResponse(responseCode = "417", description = "Could not retrieve agents list. Unexpected error occured.", 
			content = { @Content(schema = @Schema(implementation = String.class)) 
			}) 
		})
	@PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> findAllAgents(
			@RequestParam (value = "pageNumber") int pageNumber,
			@RequestParam (value = "pageSize") int pageSize,
			@RequestParam (value = "sessionReportType") Optional<String> sessionReportType,
			@RequestParam (value = "getFilterData") Optional<Boolean> getFilterData,
			@RequestParam (value = "registrationStartDate") @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss") Optional<Date> registrationStartDate,
			@RequestParam (value = "registrationEndDate") @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss") Optional<Date> registrationEndDate,
			@RequestParam (value = "status") Optional<String> status,
			@RequestParam (value = "dn") Optional<String> dn,
			@RequestParam (value = "hostname") Optional<String> hostname,
			@RequestParam (value = "macAddress") Optional<String> macAddress,
			@RequestParam (value = "ipAddress") Optional<String> ipAddress,
			@RequestParam (value = "brand") Optional<String> brand,
			@RequestParam (value = "model") Optional<String> model,
			@RequestParam (value = "processor") Optional<String> processor,
			@RequestParam (value = "osVersion") Optional<String> osVersion,
			@RequestParam (value = "agentVersion") Optional<String> agentVersion,
			@RequestParam (value = "diskType") Optional<String> diskType,
			@RequestParam (value = "agentStatus") Optional<String> agentStatus) {
		HashMap<String, Object> resultMap = new HashMap<>();
		if(getFilterData.isPresent() && getFilterData.get()) {
			resultMap.put("brands", agentSessionReportService.getBrands());
			resultMap.put("models", agentSessionReportService.getmodels());
			resultMap.put("processors", agentSessionReportService.getProcessors());
			resultMap.put("osVersions", agentSessionReportService.getOSVersions());
			resultMap.put("agentVersions", agentSessionReportService.getAgentVersions());
			resultMap.put("diskType", agentSessionReportService.getDiskType());
		}
		Page<AgentImpl> listOfAgents = agentSessionReportService.findAllAgents(
				pageNumber, 
				pageSize, 
				sessionReportType,
				registrationStartDate, 
				registrationEndDate, 
				status, 
				dn,
				hostname, 
				macAddress, 
				ipAddress, 
				brand, 
				model, 
				processor, 
				osVersion,
				agentVersion,
				diskType,
				agentStatus);
				
		resultMap.put("agents", listOfAgents);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(resultMap);
	}

	@Operation(summary = "Find agent session detail by id.", description = "", tags = { "agent-service" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Find agent detail by id.", 
			    content = { @Content(schema = @Schema(implementation = AgentImpl.class)) }),
			  @ApiResponse(responseCode = "404", description = "Agent id not found.Not found.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?>  getAgentSessionsDetail(
			@RequestParam (value = "pageNumber") int pageNumber,
			@RequestParam (value = "pageSize") int pageSize,
			@RequestParam (value = "agentID") Long agentID,
			@RequestParam (value = "sessionType") String sessionType) {
		logger.debug("Agent id:  {} ", agentID);
		Page<Map<String, Object>> agentSessionList = agentSessionReportService.getSessionList(pageNumber,pageSize, agentID, sessionType);
		
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(agentSessionList);
		
	}
	
	@Operation(summary = "Find agent session list.", description = "", tags = { "agent-service" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Get agent session detail by id.", 
			    content = { @Content(schema = @Schema(implementation = AgentImpl.class)) }),
			  @ApiResponse(responseCode = "404", description = "Agent id not found.Not found.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?>  findAgentBySessions(
			@RequestParam (value = "pageNumber") int pageNumber,
			@RequestParam (value = "pageSize") int pageSize,
			@RequestParam (value = "agentID") Long agentID,
			@RequestParam (value = "sessionType") String sessionType) {
		logger.debug("Agent id:  {} ", agentID);
		 Page<Map<String, Object>> agentSessionList = null;

			if(sessionType.equals("LOGIN")) {
				agentSessionList = agentSessionReportService.getSessionLoginExportList(1,userSessionReportService.count().intValue(),agentID);
			}
			else if(sessionType.equals("LOGOUT")) {
				agentSessionList = agentSessionReportService.getSessionLogoutExportList(1,userSessionReportService.count().intValue(),agentID);
			}
			else {
				agentSessionList = agentSessionReportService.getSessionAllExportList(1, userSessionReportService.count().intValue(), agentID);
			}		
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(agentSessionList);
		
	}

	@Operation(summary = "Exports filtered agent session list to excel", description = "", tags = { "agent-service" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = "Created excel file successfully", 
			    content = { @Content(schema = @Schema(implementation = AgentImpl.class)) }),
			  @ApiResponse(responseCode = "400", description = "Could not create client report.", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> export(
			@RequestParam (value = "pageNumber") int pageNumber,
			@RequestParam (value = "pageSize") int pageSize,
			@RequestParam (value = "agentID") Long agentID,
			@RequestParam (value = "sessionType") String sessionType){
		
	    Page<Map<String, Object>> agentSessionList = null;

		if(sessionType.equals("LOGIN")) {
			agentSessionList = agentSessionReportService.getSessionLoginExportList(1,userSessionReportService.count().intValue(),agentID);
		}
		else if(sessionType.equals("LOGOUT")) {
			agentSessionList = agentSessionReportService.getSessionLogoutExportList(1,userSessionReportService.count().intValue(),agentID);
		}
		else {
			agentSessionList = agentSessionReportService.getSessionAllExportList(1, userSessionReportService.count().intValue(), agentID);
		}
		
		try {
			if (agentSessionList != null) {
				HttpHeaders headers = new HttpHeaders();
				headers.add("fileName", "Oturum Raporu_" + new SimpleDateFormat("dd_MM_yyyy_HH:mm:ss.SSS").format(new Date()) + ".xlsx");
				headers.setContentType(MediaType.parseMediaType("application/csv"));
				headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
				byte[] excelContent = excelService.generateUserSessionReport(agentSessionList.getContent());
				return new ResponseEntity<byte[]>(excelContent, headers,  HttpStatus.OK);
			}
			else {
				HttpHeaders headers = new HttpHeaders();
	            headers.add("message", "Invalid session type: " + sessionType);
	            return ResponseEntity
	                .status(HttpStatus.BAD_REQUEST)
	                .headers(headers)
	                .build();
			}
		} catch (Exception e) {
        	logger.error("Error occured while creating excel report Error: ." + e.getMessage());
        	HttpHeaders headers = new HttpHeaders();
        	headers.add("message", "Error occured while creating excel report. Error: " + e.getMessage());
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		
		
	}

}
