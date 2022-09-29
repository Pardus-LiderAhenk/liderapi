package tr.org.lider.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tr.org.lider.entities.OperationType;
import tr.org.lider.models.ConfigParams;
import tr.org.lider.models.PackageInfo;
import tr.org.lider.plugins.RepoSourcesListParser;
import tr.org.lider.services.ConfigurationService;
import tr.org.lider.services.OperationLogService;


/**
 * 
 * Return package list from the specified Linux package repository
 * @author <a href="mailto:tuncay.colak@tubitak.gov.tr">Tuncay ÇOLAK</a>
 *
 */

@Secured({"ROLE_ADMIN", "ROLE_COMPUTERS" })
@RestController
@RequestMapping("/packages")
public class PackagesController {
	
	private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

	@Autowired
	ConfigurationService configurationService;
	
	@Autowired
	private OperationLogService operationLogService;

	@RequestMapping(method=RequestMethod.POST ,value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PackageInfo> getPackageList(@RequestParam(value="type") String type,
			@RequestParam(value="url") String url,
			@RequestParam(value="component") String component) {
		List<PackageInfo> resultSet = new ArrayList<PackageInfo>();
		List<PackageInfo> items = RepoSourcesListParser.parseURL(url.trim(), component.split(" ")[0],
				Arrays.copyOfRange(component.split(" "), 1, component.split(" ").length), "amd64",
				type);
		if (items != null && !items.isEmpty())
			resultSet.addAll(items);
		return resultSet;
	}

	@RequestMapping(method=RequestMethod.POST, value = "/update/repoAddress", produces = MediaType.APPLICATION_JSON_VALUE)
	public HashMap<String, Object> updateRepoAddrSettings(@RequestParam (value = "pardusRepoAddress", required = true) String pardusRepoAddress,
			@RequestParam (value = "pardusRepoComponent", required = true) String pardusRepoComponent) {
		ConfigParams configParams = configurationService.getConfigParams();
		configParams.setPardusRepoAddress(pardusRepoAddress);
		configParams.setPardusRepoComponent(pardusRepoComponent);
		if(configurationService.updateConfigParams(configParams) != null) {
			HashMap<String, Object> repoMap = new HashMap<String, Object>();
			repoMap.put("pardusRepoAddress", configParams.getPardusRepoAddress());
			repoMap.put("pardusRepoComponent", configParams.getPardusRepoComponent());
			
			Map<String, Object> requestData = new HashMap<String, Object>();
			requestData.put("pardusRepoAddress",configParams.getPardusRepoAddress());
			requestData.put("pardusRepoComponent",configParams.getPardusRepoComponent());
			ObjectMapper dataMapper = new ObjectMapper();
			String jsonString = null ; 
			try {
				jsonString = dataMapper.writeValueAsString(requestData);
			} catch (JsonProcessingException e1) {
				logger.error("Error occured while mapping request data to json. Error: " +  e1.getMessage());
			}
			String log = pardusRepoAddress + " has been updated";
			operationLogService.saveOperationLog(OperationType.UPDATE, log, jsonString.getBytes(), null, null, null);
			
			return repoMap;
		} else {
			return null;
		}
	}
	
//	get directory server(Active Directory and OpenLDAP) configurations method for ldap-login task
	@RequestMapping(method=RequestMethod.GET, value = "/repoAddress", produces = MediaType.APPLICATION_JSON_VALUE)
	public HashMap<String, Object> getConfigParams() {
		HashMap<String, Object> repoMap = new HashMap<String, Object>();
		repoMap.put("pardusRepoAddress", configurationService.getPardusRepoAddress());
		repoMap.put("pardusRepoComponent", configurationService.getPardusRepoComponent());
		return repoMap;
	}
}
