package tr.org.lider.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

import tr.org.lider.entities.RdpClient;
import tr.org.lider.repositories.RdpClientRepository;
import tr.org.lider.services.RdpClientService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/rdp-client")

public class RdpClientController {

    Logger logger = LoggerFactory.getLogger(LiderGuacamoleTunnelServlet.class);

    @Autowired
    RdpClientRepository rdpClientRepository;

    @Autowired
    RdpClientService rdpClientService;

    @GetMapping("/list")
    public ResponseEntity<Page<RdpClient>> getSavedRdpClient(
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size) {

        try {
            Page<RdpClient> rdpClientPage = rdpClientService.getSavedRdpClientPage(page, size);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(rdpClientPage);
        } catch (Exception e) {
            logger.error(" Error occured when getting RdpClients. Error:" + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping(value = "/save")
    public ResponseEntity<Boolean> saveRdpClient(@RequestBody Map<String, String> data) {
        String host = data.get("host");
        String username = data.get("username");
        String hostname = data.get("hostname");
        String description = data.get("description");

        try {
            rdpClientService.saveRdpClient(host, username, hostname, description);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(true);
        } catch (Exception e) {
            logger.error(" Error occured when saving RdpClient. Error:" + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(false);
        }
    }

    @PatchMapping(value = "/update")
    public ResponseEntity<Boolean> updateSavedRdpClient(@RequestBody Map<String, String> data) {
        String id = data.get("id");
        String host = data.get("host");
        String username = data.get("username");
        String hostname = data.get("hostname");
        String description = data.get("description");

        tr.org.lider.entities.RdpClient rdpClient = rdpClientService.getSavedRdpClientById(id);

        rdpClient.setUsername(username);
        rdpClient.setHost(host);
        rdpClient.setHostname(hostname);
        rdpClient.setDescription(description);

        try {
            rdpClientService.updateSavedRdpClient(id, host, username, hostname, description);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(true);
        } catch (Exception e) {
            logger.error(" Error occured when updating RdpClient. Error:" + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(false);
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<Boolean> deleteSavedRdpClient(@PathVariable String id) {
        try {
            rdpClientService.deleteSavedRdpClient(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(true);
        } catch (Exception e) {
            logger.error(" Error occured when deleting RdpClient. Error:" + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(false);
        }
    }

}
