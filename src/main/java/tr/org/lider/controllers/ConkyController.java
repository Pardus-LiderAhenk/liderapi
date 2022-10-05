package tr.org.lider.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	
//	get conky list with pagging
	@Secured({"ROLE_ADMIN", "ROLE_CONKY_DEFINITION", "ROLE_COMPUTERS" })
	@RequestMapping(method=RequestMethod.POST ,value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<ConkyTemplate> conkyList(
			@RequestParam (value = "pageSize") int pageSize,
			@RequestParam (value = "pageNumber") int pageNumber
			) {
		return conkyService.list(pageNumber, pageSize);
	}
	
//	get conky list all as no pagging
	@Secured({"ROLE_ADMIN", "ROLE_CONKY_DEFINITION", "ROLE_COMPUTERS" })
	@RequestMapping(method=RequestMethod.POST ,value = "/list-all", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ConkyTemplate> conkyListAll() {
		return conkyService.listAll();
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
