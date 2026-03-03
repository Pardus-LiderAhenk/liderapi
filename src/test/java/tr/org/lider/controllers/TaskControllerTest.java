package tr.org.lider.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import tr.org.lider.entities.PluginTask;
import tr.org.lider.repositories.PluginTaskRepository;
import tr.org.lider.services.TaskService;
import tr.org.lider.utils.IRestResponse;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private PluginTaskRepository pluginTaskRepository;

    @InjectMocks
    private TaskController taskController;

    @Test
    void executeResourceUsageTask_WhenValidCommandId_ReturnsOk() {
        PluginTask requestBody = new PluginTask();
        requestBody.setCommandId("RESOURCE_INFO_FETCHER");
        PluginTask storedTask = new PluginTask();

        storedTask.setRole("ROLE_RESOURCE_USAGE");
        when(pluginTaskRepository.findByCommandId("RESOURCE_INFO_FETCHER")).thenReturn(Collections.singletonList(storedTask));
        when(taskService.execute(requestBody)).thenReturn(mock(IRestResponse.class));

        ResponseEntity<IRestResponse> response = taskController.executeResourceUsageTask(requestBody);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(taskService).execute(requestBody);
    }
}