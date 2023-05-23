package tr.org.lider.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import tr.org.lider.entities.OperationType;
import tr.org.lider.entities.ServerImpl;
import tr.org.lider.repositories.ServerRepository;

public class ServerService {
	
	@Autowired
	private ServerRepository serverRepository;
	
	@Autowired
	private OperationLogService operationLogService;

	
	public ServerImpl add(ServerImpl server) {
		
		ServerImpl savedServer = serverRepository.save(server);
		try {
			operationLogService.saveOperationLog(OperationType.CREATE, "Sunucu eklendi",null,null,null,savedServer.getId());
			
		} catch (Exception e) {
			e.printStackTrace();		
		}
		return savedServer;
	}
	
	public ServerImpl delete(Long id,ServerImpl server) {
		
		ServerImpl savedServer = serverRepository.save(server);
		try {
			operationLogService.saveOperationLog(OperationType.DELETE, "Sunucu silindi",null,null,null,savedServer.getId());
			
		} catch (Exception e) {
			e.printStackTrace();		
		}
		return savedServer;	}
	
	public List<ServerImpl> listAll(){
		return serverRepository.findAllByDeleted(false);
	}
	
	public Page<ServerImpl> list(int pageNumber, int pageSize){
		PageRequest pageable = PageRequest.of(pageNumber  - 1, pageSize);
		return serverRepository.findByDeletedOrderByCreateDateDesc(pageable, false);
	}
}
