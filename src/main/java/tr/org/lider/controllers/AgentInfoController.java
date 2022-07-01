package tr.org.lider.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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

import tr.org.lider.entities.AgentImpl;
import tr.org.lider.services.AgentService;
import tr.org.lider.services.ExcelExportService;

@Secured({"ROLE_ADMIN", "ROLE_AGENT_INFO" })
@RestController
@RequestMapping("lider/agent_info")
public class AgentInfoController {

	@Autowired
	private AgentService agentService;

	@Autowired
	private ExcelExportService excelService;
	
	@RequestMapping(value="/getInnerHtmlPage", method = {RequestMethod.POST })
	public String getInnerHtmlPage(@RequestParam (value = "innerPage", required = true) String innerPage) {
		return innerPage;
	}

	@RequestMapping(method=RequestMethod.POST, value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public HashMap<String, Object> findAllAgents(
			@RequestParam (value = "pageNumber") int pageNumber,
			@RequestParam (value = "pageSize") int pageSize,
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
			@RequestParam (value = "agentVersion") Optional<String> agentVersion) {
		HashMap<String, Object> resultMap = new HashMap<>();
		if(getFilterData.isPresent() && getFilterData.get()) {
			resultMap.put("brands", agentService.getBrands());
			resultMap.put("models", agentService.getmodels());
			resultMap.put("processors", agentService.getProcessors());
			resultMap.put("osVersions", agentService.getOSVersions());
			resultMap.put("agentVersions", agentService.getAgentVersions());
		}
		Page<AgentImpl> listOfAgents = agentService.findAllAgents(
				pageNumber, 
				pageSize, 
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
				agentVersion);
				
		resultMap.put("agents", listOfAgents);
		return resultMap;
	}

	//get agent detail by ID
	@RequestMapping(method=RequestMethod.POST ,value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
	public AgentImpl findAgentByIDRest(@RequestParam (value = "agentID") Long agentID) {
		Optional<AgentImpl> agent = agentService.findAgentByID(agentID);
		if(agent.isPresent()) {
			return agent.get();
		}
		else {
			return null;
		}
	}

	@RequestMapping(method=RequestMethod.POST, value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> export(
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
			@RequestParam (value = "agentVersion") Optional<String> agentVersion) {
		Page<AgentImpl> listOfAgents = agentService.findAllAgents(
				1, 
				agentService.count().intValue(), 
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
				agentVersion);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("fileName", "Istemci Raporu_" + new SimpleDateFormat("dd_MM_yyyy_HH:mm:ss.SSS").format(new Date()) + ".xlsx");
		headers.setContentType(MediaType.parseMediaType("application/csv"));
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
		return new ResponseEntity<byte[]>(excelService.generateAgentReport(listOfAgents.getContent()), headers,  HttpStatus.OK);
	}
}