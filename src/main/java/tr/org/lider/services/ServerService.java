package tr.org.lider.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tr.org.lider.entities.OperationType;
import tr.org.lider.entities.ServerImpl;
import tr.org.lider.repositories.ServerRepository;

@Service
public class ServerService {
	
	@Autowired
	private ServerRepository serverRepository;
	
	@Autowired
	private OperationLogService operationLogService;

	
	public ServerImpl add(ServerImpl server) {
		
		ServerImpl savedServer = serverRepository.save(server);
		return serverRepository.save(savedServer);
	}
	
	public ServerImpl delete(Long id,ServerImpl server) {
		
		ServerImpl savedServer = serverRepository.save(server);
		try {
			operationLogService.saveOperationLog(OperationType.DELETE, "Sunucu silindi",null,null,null,savedServer.getId());
			
		} catch (Exception e) {
			e.printStackTrace();		
		}
		return savedServer;	
	}
	
	
	//public List<ServerImpl> listAll(){
	//	return serverRepository.findAllByDeleted(false);
	//}
	
	//public Page<ServerImpl> list(int pageNumber, int pageSize){
	//	PageRequest pageable = PageRequest.of(pageNumber  - 1, pageSize);
	//	return serverRepository.findByDeletedOrderByCreateDateDesc(pageable, false);
	//}
}
