package tr.org.lider.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tr.org.lider.entities.ScriptTemplate;
import tr.org.lider.services.ScriptService;

/**
 * 
 * Return the script templates, saved, edited and deleted script
 * @author <a href="mailto:tuncay.colak@tubitak.gov.tr">Tuncay ÇOLAK</a>
 *
 */
@RestController
@RequestMapping("/script")
public class ScriptController {

	@Autowired
	private ScriptService scriptService;
	
	@Secured({"ROLE_ADMIN", "ROLE_SCRIPT_DEFINITION", "ROLE_COMPUTERS" })
	@RequestMapping(method=RequestMethod.POST ,value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<ScriptTemplate> scriptList(
			@RequestParam (value = "pageSize") int pageSize,
			@RequestParam (value = "pageNumber") int pageNumber
			) {
		return scriptService.list(pageNumber, pageSize);
	}

	@Secured({"ROLE_ADMIN", "ROLE_SCRIPT_DEFINITION" })
	@RequestMapping(method=RequestMethod.POST ,value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public ScriptTemplate scriptAdd(@RequestBody ScriptTemplate script){
		return scriptService.add(script);
	}
	
	@Secured({"ROLE_ADMIN", "ROLE_SCRIPT_DEFINITION" })
	@RequestMapping(method=RequestMethod.POST ,value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ScriptTemplate scriptDel(@RequestBody ScriptTemplate script){
		return scriptService.delete(script);
	}
	
	@Secured({"ROLE_ADMIN", "ROLE_SCRIPT_DEFINITION" })
	@RequestMapping(method=RequestMethod.POST ,value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ScriptTemplate scriptUpdate(@RequestBody ScriptTemplate script){
		return scriptService.update(script);
	}
}
