package tr.org.lider.operation.logs;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class OperationLogTypeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OperationLogTypeService operationLogTypeService;

    @InjectMocks
    private OperationLogTypeController operationLogTypeController;

    @BeforeEach
    public void setMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(operationLogTypeController).build();
    }

    private OperationLogTypeImpl sampleOperationLogType() {
        return OperationLogTypeImpl.builder()
                .id(1L)
                .type("EXECUTE_TASK")
                .operationTypeId(7)
                .descriptionTr("Görev Çalıştırma")
                .descriptionEn("Execute Task")
                .createDate(LocalDateTime.now())
                .build();
    }

    @Test
    void operationLogTypeListAll_ExistingTypes_ReturnsAllTypes() throws Exception {
        OperationLogTypeImpl type1 = sampleOperationLogType();
        OperationLogTypeImpl type2 = OperationLogTypeImpl.builder()
                .id(2L)
                .type("CREATE")
                .operationTypeId(1)
                .descriptionTr("Oluşturma")
                .descriptionEn("Create")
                .createDate(LocalDateTime.now())
                .build();

        List<OperationLogTypeImpl> types = Arrays.asList(type1, type2);
        when(operationLogTypeService.listAll()).thenReturn(types);

        mockMvc.perform(get("/api/lider/operation-log-type")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("EXECUTE_TASK"))
                .andExpect(jsonPath("$[0].operationTypeId").value(7))
                .andExpect(jsonPath("$[0].descriptionTr").value("Görev Çalıştırma"))
                .andExpect(jsonPath("$[0].descriptionEn").value("Execute Task"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].type").value("CREATE"))
                .andExpect(jsonPath("$[1].operationTypeId").value(1))
                .andExpect(jsonPath("$[1].descriptionTr").value("Oluşturma"))
                .andExpect(jsonPath("$[1].descriptionEn").value("Create"));

        verify(operationLogTypeService, times(1)).listAll();
    }

    @Test
    void operationLogTypeListAll_NoTypes_ReturnsEmptyList() throws Exception {
        when(operationLogTypeService.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/lider/operation-log-type")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(operationLogTypeService, times(1)).listAll();
    }

    @Test
    void operationLogTypeListAll_SingleType_ReturnsOneType() throws Exception {
        OperationLogTypeImpl type = sampleOperationLogType();
        when(operationLogTypeService.listAll()).thenReturn(Arrays.asList(type));

        mockMvc.perform(get("/api/lider/operation-log-type")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("EXECUTE_TASK"))
                .andExpect(jsonPath("$[0].operationTypeId").value(7))
                .andExpect(jsonPath("$[0].descriptionTr").value("Görev Çalıştırma"))
                .andExpect(jsonPath("$[0].descriptionEn").value("Execute Task"));

        verify(operationLogTypeService, times(1)).listAll();
    }
}
