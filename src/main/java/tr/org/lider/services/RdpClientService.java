package tr.org.lider.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tr.org.lider.entities.RdpClient;
import tr.org.lider.repositories.RdpClientRepository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class RdpClientService {

    @Autowired
    RdpClientRepository rdpClientRepository;

    public List<RdpClient> getSavedRdpClientList() {
        return rdpClientRepository.findAll();
    }

    public Page<RdpClient> getSavedRdpClientPage(int page, int size) {
        page = page - 1;
        Pageable pageable = PageRequest.of(page, size);
        return rdpClientRepository.findAll(pageable);
    }

    public RdpClient getSavedRdpClientById(String id) {
        int idInt = Integer.parseInt(id);
        return rdpClientRepository.findById(idInt);
    }

    public RdpClient getSavedRdpClientByHost(String host) {
        return rdpClientRepository.findByHost(host);
    }

    public RdpClient getSavedRdpClientByUsername(String username) {
        return rdpClientRepository.findByUsername(username);
    }

    public RdpClient getSavedRdpClientByHostAndUsername(String host, String username) {
        return rdpClientRepository.findByHostAndUsername(host, username);
    }

    public String saveRdpClient(String host, String username, String hostname, String description) {
        rdpClientRepository.save(new tr.org.lider.entities.RdpClient(host, username, hostname, description));
        return "Saved";
    }

    public String updateSavedRdpClient(String id, String host, String username, String hostname, String description) {
        int idInt = Integer.parseInt(id);
        tr.org.lider.entities.RdpClient rdpClient = rdpClientRepository.findById(idInt);
        rdpClient.setHost(host);
        rdpClient.setUsername(username);
        rdpClient.setHostname(hostname);
        rdpClient.setDescription(description);
        rdpClientRepository.save(rdpClient);
        return "Updated";
    }

    public String deleteSavedRdpClient(String id) {
        int idInt = Integer.parseInt(id);
        tr.org.lider.entities.RdpClient rdpClient = rdpClientRepository.findById(idInt);
        rdpClientRepository.delete(rdpClient);
        return "Deleted";
    }

    public String deleteAllSavedRdpClient() {
        rdpClientRepository.deleteAll();
        return "Deleted All";
    }

}
