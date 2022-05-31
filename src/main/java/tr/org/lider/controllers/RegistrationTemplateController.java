package tr.org.lider.controllers;

import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

import tr.org.lider.entities.RegistrationTemplateImpl;
import tr.org.lider.models.RegistrationTemplateType;
import tr.org.lider.services.RegistrationTemplateService;

/**
 * REST controller for managing registration template
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 * 
 */
@Secured({"ROLE_ADMIN", "ROLE_REGISTRATION_TEMPLATE" })
@RestController
@RequestMapping("/api")
public class RegistrationTemplateController {
	
	private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RegistrationTemplateService registrationTemplateService;
	
	//find registration template by id
	@GetMapping(value = "/registration-templates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> findByID(@PathVariable Long id) {
		logger.debug("Request to get template by id : {}", id);
		Optional<RegistrationTemplateImpl> result = registrationTemplateService.findByID(id);
		if(!result.isPresent()) {
			logger.error("Request to get template by id : {} but id not found!", id);
    		return ResponseEntity
    				.status(HttpStatus.NOT_FOUND)
    				.body(Arrays.asList("Template ID not found !"));
		}
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(result);
	}
	
	//find all registration templates by type
	@GetMapping(value = "/registration-templates/type/{templateType}/page-count/{pageCount}/page-size/{pageSize}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> findAll(@PathVariable RegistrationTemplateType templateType,
			@PathVariable int pageCount, @PathVariable int pageSize) {
		logger.debug("Request to get all templates for type : {}", templateType);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(registrationTemplateService.findAllByType(pageCount, pageSize, templateType));
	}

	//add new registration template
	@PostMapping(value = "/registration-templates", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createTemplate(@RequestBody RegistrationTemplateImpl template) {
		logger.debug("Request to save template : {}", template);
        if (template.getId() != null) {
        	logger.error("Request to save template : {} but template id has to be null.", template);
    		return ResponseEntity
    				.status(HttpStatus.BAD_REQUEST)
    				.body(Arrays.asList("A new template can not have an ID !"));
        }
        RegistrationTemplateImpl result = registrationTemplateService.save(template);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(result);
	}
	
	//edit registration template
	@PutMapping(value = "/registration-templates", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateTemplate(@RequestBody RegistrationTemplateImpl template) {
		logger.debug("Request to update template : {}", template);
		Optional<RegistrationTemplateImpl> existingTemplate = registrationTemplateService.findByID(template.getId());
		if(!existingTemplate.isPresent()) {
			logger.error("Request to update template {} but template not found!", template);
    		return ResponseEntity
    				.status(HttpStatus.NOT_FOUND)
    				.body(Arrays.asList("Template ID not found !"));
		}
		//update allowed fields
		existingTemplate.get().setAuthGroup(template.getAuthGroup());
		existingTemplate.get().setUnitId(template.getUnitId());
		existingTemplate.get().setParentDn(template.getParentDn());
		RegistrationTemplateImpl result = registrationTemplateService.save(existingTemplate.get());
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(result);
	}
	
	//delete registration template
	@DeleteMapping(value = "/registration-templates/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
		logger.debug("Request to delete template {} ", id);
		if(!registrationTemplateService.findByID(id).isPresent()) {
			logger.error("Request to delete template {} but id not found!", id);
    		return ResponseEntity
    				.status(HttpStatus.NOT_FOUND)
    				.body(Arrays.asList("Template ID not found !"));
		}
		registrationTemplateService.delete(id);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(null);
	}
	
}