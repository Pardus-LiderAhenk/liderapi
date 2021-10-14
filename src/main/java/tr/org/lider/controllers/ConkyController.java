package tr.org.lider.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import tr.org.lider.entities.ConkyTemplate;
import tr.org.lider.services.ConkyService;

/**
 * 
 * Return the Conky Template list, saved, edited and deleted Conky
 * @author <a href="mailto:tuncay.colak@tubitak.gov.tr">Tuncay ÇOLAK</a>
 *
 */

@RestController
@RequestMapping("/conky")
public class ConkyController {

	@Autowired
	private ConkyService conkyService;
	
//	get conky list
	@Secured({"ROLE_ADMIN", "ROLE_CONKY_DEFINITION", "ROLE_COMPUTERS" })
	@RequestMapping(method=RequestMethod.POST ,value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ConkyTemplate> notifyList() {
		return conkyService.list();
	}

	@Secured({"ROLE_ADMIN", "ROLE_CONKY_DEFINITION" })
	@RequestMapping(method=RequestMethod.POST ,value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public ConkyTemplate notifyAdd(@RequestBody ConkyTemplate template){
		return conkyService.add(template);
	}
	
	@Secured({"ROLE_ADMIN", "ROLE_CONKY_DEFINITION" })
	@RequestMapping(method=RequestMethod.POST ,value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public ConkyTemplate notifyDel(@RequestBody ConkyTemplate template){
		return conkyService.delete(template);
	}
	
	@Secured({"ROLE_ADMIN", "ROLE_CONKY_DEFINITION" })
	@RequestMapping(method=RequestMethod.POST ,value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ConkyTemplate notifyUpdate(@RequestBody ConkyTemplate template){
		return conkyService.update(template);
	}
}
