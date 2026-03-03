package tr.org.lider.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import tr.org.lider.entities.OperationType;
import tr.org.lider.entities.ScriptTemplate;
import tr.org.lider.entities.ScriptType;
import tr.org.lider.repositories.ScriptRepository;
import tr.org.lider.security.User;
import tr.org.lider.constant.RoleConstants;

@Service
public class ScriptService {
	

	@Autowired
	private ScriptRepository scriptRepository;
	
	@Autowired
	private OperationLogService operationLogService;

	@Autowired
	private UserService userService;
	
	@PostConstruct
	private void init() {
		if (scriptRepository.count() == 0) {
			ScriptType  scriptType = ScriptType.getType(1);
			String label = "Dosya Oluştur";
			String contents = "#!/bin/bash\n" + 
					"touch /tmp/test.txt";
			scriptRepository.save(new ScriptTemplate(scriptType, label, contents, new Date(), null, false, "lider", true));
		}
	}

	public Optional<ScriptTemplate> find(Long id){
		return scriptRepository.findById(id);
	}
	
	public List<ScriptTemplate> listAll(){
		return scriptRepository.findAllByDeleted(false);
	}
	
	public Page<ScriptTemplate> list(int pageNumber, int pageSize, Map<String, String> params) {
		PageRequest pageable = PageRequest.of(pageNumber - 1, pageSize);
		String scriptName = params.get("scriptName");

		String username = AuthenticationService.getUserName();
		User userDetails = userService.loadUserByUsername(username);

		if (userDetails.getRoles().contains(RoleConstants.ROLE_ADMIN)) {
			return scriptRepository.findByLabelContainingIgnoreCaseAndDeletedOrderByCreateDateDesc(
					pageable, scriptName, false);
		} else {
			return scriptRepository.findByLabelContainingIgnoreCaseAndDeletedAndCreatedByOrPublishedOrderByCreateDateDesc(
					pageable, scriptName, false, username);
		}
	}

	public ScriptTemplate add(ScriptTemplate script) {
		script.setDeleted(false);
		script.setIsPublished(false);

		String username = AuthenticationService.getUserName();
		script.setCreatedBy(username);

		ScriptTemplate savedScript = scriptRepository.save(script);
		try {
			operationLogService.saveOperationLog(OperationType.CREATE, "Created script.", script.getContents().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return savedScript;
	}

	public ScriptTemplate delete(Long id) {
		ScriptTemplate existingScript = scriptRepository.findOne(id);


		String username = AuthenticationService.getUserName();

		User userDetails = userService.loadUserByUsername(username);

		if (!userDetails.getRoles().contains(RoleConstants.ROLE_ADMIN) && !existingScript.getCreatedBy().equals(username)) {
			throw new AccessDeniedException("You do not have permission to delete this script.");
		}

		existingScript.setDeleted(true);
		ScriptTemplate savedScript = scriptRepository.save(existingScript);
		try {
			operationLogService.saveOperationLog(OperationType.DELETE, "Deleted script", existingScript.getContents().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return savedScript;
	}
	
	public ScriptTemplate update(ScriptTemplate script) {
		ScriptTemplate existingScript = scriptRepository.findOne(script.getId());

		String username = AuthenticationService.getUserName();

		User userDetails = userService.loadUserByUsername(username);

		if (!userDetails.getRoles().contains(RoleConstants.ROLE_ADMIN) && !existingScript.getCreatedBy().equals(username)) {
			throw new AccessDeniedException("You do not have permission to update this script.");
		}

		script.setModifyDate(new Date());
		script.setDeleted(false);

		script.setIsPublished(existingScript.getIsPublished());
		
		if (existingScript.getCreatedBy() == null || existingScript.getCreatedBy().isEmpty()) {
			script.setCreatedBy(username);
		} else {
			script.setCreatedBy(existingScript.getCreatedBy());
		}
		
		ScriptTemplate savedScript = scriptRepository.save(script);
		try {
			operationLogService.saveOperationLog(OperationType.UPDATE, "Updated script.", script.getContents().getBytes());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return savedScript;
	}
}