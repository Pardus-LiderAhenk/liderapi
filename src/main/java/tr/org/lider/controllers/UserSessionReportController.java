package tr.org.lider.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import tr.org.lider.entities.OperationLogImpl;
import tr.org.lider.entities.UserSessionImpl;
import tr.org.lider.models.UserSessionsModel;
import tr.org.lider.services.AgentSessionReportService;
import tr.org.lider.services.ExcelExportService;
import tr.org.lider.services.UserService;
import tr.org.lider.services.UserSessionReportService;

@Secured({"ROLE_ADMIN", "ROLE_USER_SESSION_REPORT" })
@RestController
@RequestMapping("/api/lider/user-session")
@Tag(name = "User Session", description = "User Session Rest Service")
public class UserSessionReportController {
	
	Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserSessionReportService userSessionReportService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private AgentSessionReportService agentSessionReportService;
	
	
	
	@Autowired
	private ExcelExportService excelService;
	
	@Operation(summary = "Gets user session", description = "", tags = { "user" })
	@ApiResponses(value = { 
			  @ApiResponse(responseCode = "200", description = ""),
			  @ApiResponse(responseCode = "417", description = "Could not get user session. Unexpected error occurred", 
			    content = @Content(schema = @Schema(implementation = String.class))) })
	@GetMapping(value = "/uid/{uid}")
	public ResponseEntity<List<UserSessionsModel>> getUserSessions(@PathVariable String uid) {
		List<UserSessionsModel> userSessions=null;
		try {
			List<UserSessionImpl> userSessionsDb = userService.getUserSessions(uid);
			userSessions=new ArrayList<>();
			for (UserSessionImpl userSessionImpl : userSessionsDb) {
				UserSessionsModel model = new UserSessionsModel();
				model.setAgent(userSessionImpl.getAgent());
				model.setCreateDate(userSessionImpl.getCreateDate());
				model.setId(userSessionImpl.getId());
				model.setSessionEvent(userSessionImpl.getSessionEvent());
				model.setUserIp(userSessionImpl.getUserIp());
				model.setUsername(userSessionImpl.getUsername());
				userSessions.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
			HttpHeaders headers = new HttpHeaders();
    		return ResponseEntity
    				.status(HttpStatus.EXPECTATION_FAILED)
    				.headers(headers)
    				.build();
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(userSessions);
				
	}
	
	@Operation(summary = "Lider usage history.", description = "", tags = { "user-sesion" })
	@ApiResponses(value = { 
      	  @ApiResponse(responseCode = "200", description = "Get usage history for lider.",
			  content = { @Content(schema = @Schema(implementation = OperationLogImpl.class))}),
		  @ApiResponse(responseCode = "417",description = "Coul not retrieve usage history. Unexpected error occured.",
	   		 content = @Content(schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/list")
	public ResponseEntity<?> operationLogs(
			@RequestParam (value = "pageNumber") int pageNumber,
			@RequestParam (value = "pageSize") int pageSize,
			@RequestParam (value = "sessionType") Integer sessionType,
			@RequestParam (value = "username", required = false) String username,
			@RequestParam (value="startDate", required = false) @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss") Optional<Date> startDate,
			@RequestParam (value="endDate", required = false) @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss") Optional<Date> endDate) {
		
		Page<UserSessionImpl> users = userSessionReportService.findAllUserFiltered(pageNumber, pageSize, sessionType,username,startDate, endDate);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(users);
	}
	
//	@Operation(summary = "Exports filtered user session list to excel.", description = "", tags = { "operation-log" })
//	@ApiResponses(value = { 
//      	  @ApiResponse(responseCode = "200", description = "Created excel file successfully.",
//			  content = { @Content(schema = @Schema(implementation = OperationLogImpl.class))}),
//		  @ApiResponse(responseCode = "400",description = "Could not create operation log report.Bad request.",
//	   		 content = @Content(schema = @Schema(implementation = String.class))) })
//	@PostMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<?> export(
//			@RequestParam (value = "sessionType") String sessionType,
//			@RequestParam (value = "username", required = false) String username,
//			@RequestParam (value="startDate", required = false) @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss") Optional<Date> startDate,
//			@RequestParam (value="endDate", required = false) @DateTimeFormat(pattern="dd/MM/yyyy HH:mm:ss") Optional<Date> endDate) {
//			
//		
//		Page<AgentImpl> listOfAgents = agentSessionReportService.findAllAgents(
//				1, 
//				sessionType,
//				username,
//				startDate,
//				endDate
//				);		
//		try {
//			HttpHeaders headers = new HttpHeaders();
//			headers.add("fileName", "Oturum Raporu_" + new SimpleDateFormat("dd_MM_yyyy_HH:mm:ss.SSS").format(new Date()) + ".xlsx");
//			headers.setContentType(MediaType.parseMediaType("application/csv"));
//			headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
//			byte[] excelContent = excelService.generateSessionReport(listOfAgents.getContent());
//			return new ResponseEntity<byte[]>(excelContent, headers,  HttpStatus.OK);
//		} catch (Exception e) {
//        	logger.error("Error occured while creating excel report Error: ." + e.getMessage());
//        	HttpHeaders headers = new HttpHeaders();
//        	headers.add("message", "Error occured while creating excel report. Error: " + e.getMessage());
//    		return ResponseEntity
//    				.status(HttpStatus.EXPECTATION_FAILED)
//    				.headers(headers)
//    				.build();
//		}
//	}

}
