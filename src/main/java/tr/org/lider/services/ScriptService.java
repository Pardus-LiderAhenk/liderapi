package tr.org.lider.services;

import java.util.Date;
import java.util.List;

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
			ScriptTemplate scriptTemplate = new ScriptTemplate();
			scriptTemplate.setContents(contents);
			scriptTemplate.setCreateDate(new Date());
			scriptTemplate.setLabel(label);
			scriptTemplate.setScriptType(scriptType);
			scriptRepository.save(scriptTemplate);
		}
	}

	public List<ScriptTemplate> list(){
		return scriptRepository.findAll();
	}

	public ScriptTemplate add(ScriptTemplate script) {
		ScriptTemplate scriptFile = scriptRepository.save(script);
		try {
			operationLogService.saveOperationLog(OperationType.CREATE, "Betik Tanımı oluşturuldu.", script.getContents().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return scriptFile;
	}

	public ScriptTemplate delete(ScriptTemplate script) {
		ScriptTemplate existFile = scriptRepository.findOne(script.getId());
		scriptRepository.deleteById(script.getId());
		try {
			operationLogService.saveOperationLog(OperationType.DELETE, "Betik Tanımı silindi.", existFile.getContents().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return script;
	}
	
	public ScriptTemplate update(ScriptTemplate script) {
		script.setModifyDate(new Date());
		ScriptTemplate scriptFile = scriptRepository.save(script);
		try {
			operationLogService.saveOperationLog(OperationType.UPDATE, "Betik Tanımı güncellendi.", script.getContents().getBytes());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return scriptFile;
	}
}