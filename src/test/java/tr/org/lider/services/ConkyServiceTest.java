package tr.org.lider.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.org.lider.entities.ConkyTemplate;
import tr.org.lider.entities.OperationType;
import tr.org.lider.repositories.ConkyRepository;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConkyServiceTest {

    @InjectMocks
    private ConkyService conkyService;

    @Mock
    private ConkyRepository conkyRepository;

    @Mock
    private OperationLogService operationLogService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void add() {
        ConkyTemplate conkyTemplate = new ConkyTemplate();
        conkyTemplate.setId(1L);
        conkyTemplate.setLabel("Test Conky");
        conkyTemplate.setContents("Test Contents");
        conkyTemplate.setSettings("Test Settings");
        conkyTemplate.setDeleted(false);
        conkyTemplate.setCreateDate(new Date());

        // Arrange: Mock save method
        when(conkyRepository.save(any(ConkyTemplate.class))).thenReturn(conkyTemplate);
        // Act: Call the add method
        ConkyTemplate result = conkyService.add(conkyTemplate);
        // Assert: Check if the template is saved correctly
        assertNotNull(result);
        assertEquals("Test Conky", result.getLabel());
        assertEquals("Test Contents", result.getContents());

        verify(conkyRepository, times(1)).save(any(ConkyTemplate.class));

        verify(operationLogService, times(1)).saveOperationLog(eq(OperationType.CREATE),
                eq("Created system monitoring definition"), any(byte[].class));
    }

    @Test
    void delete() {
    }
}
