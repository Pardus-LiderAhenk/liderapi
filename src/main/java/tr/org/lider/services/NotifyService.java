package tr.org.lider.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tr.org.lider.entities.NotifyTemplate;
import tr.org.lider.entities.OperationType;
import tr.org.lider.repositories.NotifyRepository;

@Service
public class NotifyService {

	@Autowired
	private NotifyRepository notifyRepository;

	@Autowired
	private OperationLogService operationLogService;

	public List<NotifyTemplate> list(){
		return notifyRepository.findAll();
	}

	public NotifyTemplate add(NotifyTemplate file) {
		NotifyTemplate notifyFile = notifyRepository.save(file);
		try {
			operationLogService.saveOperationLog(OperationType.CREATE, "ETA Mesaj Tanımı oluşturuldu.", file.getContents().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return notifyFile;
	}

	public NotifyTemplate delete(NotifyTemplate file) {
		NotifyTemplate existNotify = notifyRepository.findOne(file.getId());
		existNotify.setDeleted(true);
		NotifyTemplate notifyFile = notifyRepository.save(existNotify);
		try {
			operationLogService.saveOperationLog(OperationType.CREATE, "ETA Mesaj Tanımı silindi.", existNotify.getContents().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return notifyFile;
	}

	public NotifyTemplate update(NotifyTemplate file) {
		file.setModifyDate(new Date());
		file.setDeleted(false);
		NotifyTemplate notifyFile = notifyRepository.save(file);
		try {
			operationLogService.saveOperationLog(OperationType.CREATE, "ETA Mesaj Tanımı güncellendi.", file.getContents().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return notifyFile;
	}
}