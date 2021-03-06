package tr.org.lider.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tr.org.lider.entities.OperationType;
import tr.org.lider.entities.ScriptTemplate;
import tr.org.lider.entities.ScriptType;
import tr.org.lider.repositories.ScriptRepository;

@Service
public class ScriptService {
	

	@Autowired
	private ScriptRepository scriptRepository;
	
	@Autowired
	private OperationLogService operationLogService;
	
	@PostConstruct
	private void init() {
		if (scriptRepository.count() == 0) {
			ScriptType  scriptType = ScriptType.getType(1);
			String label = "Dosya Oluştur";
			String contents = "#!/bin/bash\n" + 
					"touch /tmp/test.txt";
			scriptRepository.save(new ScriptTemplate(scriptType, label, contents, new Date(), null, false));
		}
	}

	public Optional<ScriptTemplate> find(Long id){
		return scriptRepository.findById(id);
	}
	
	public List<ScriptTemplate> list(){
		return scriptRepository.findByDeletedOrderByCreateDateDesc(false);
	}

	public ScriptTemplate add(ScriptTemplate script) {
		script.setDeleted(false);
		ScriptTemplate savedScript = scriptRepository.save(script);
		try {
			operationLogService.saveOperationLog(OperationType.CREATE, "Betik Tanımı oluşturuldu.", script.getContents().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return savedScript;
	}

	public ScriptTemplate delete(ScriptTemplate script) {
		ScriptTemplate existScript = scriptRepository.findOne(script.getId());
		existScript.setDeleted(true);
		ScriptTemplate savedScript = scriptRepository.save(existScript);
		try {
			operationLogService.saveOperationLog(OperationType.DELETE, "Betik Tanımı silindi.", existScript.getContents().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return savedScript;
	}
	
	public ScriptTemplate update(ScriptTemplate script) {
		script.setModifyDate(new Date());
		script.setDeleted(false);
		ScriptTemplate savedScript = scriptRepository.save(script);
		try {
			operationLogService.saveOperationLog(OperationType.UPDATE, "Betik Tanımı güncellendi.", script.getContents().getBytes());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return savedScript;
	}
}