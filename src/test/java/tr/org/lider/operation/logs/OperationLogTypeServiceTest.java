package tr.org.lider.operation.logs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OperationLogTypeServiceTest {

    @Mock
    private OperationLogTypeRepository operationLogTypeRepository;

    @InjectMocks
    private OperationLogTypeService operationLogTypeService;

    @Test
    void listAll_WhenCalled_ReturnsAllOperationLogTypes() {
        // Arrange
        OperationLogTypeImpl type1 = new OperationLogTypeImpl();
        type1.setId(1L);
        type1.setType("EXECUTE_TASK");
        type1.setOperationTypeId(7);
        type1.setDescriptionTr("Görev Çalıştırma");
        type1.setDescriptionEn("Execute Task");
        type1.setCreateDate(LocalDateTime.now());

        OperationLogTypeImpl type2 = new OperationLogTypeImpl();
        type2.setId(2L);
        type2.setType("CREATE");
        type2.setOperationTypeId(1);
        type2.setDescriptionTr("Oluşturma");
        type2.setDescriptionEn("Create");
        type2.setCreateDate(LocalDateTime.now());

        List<OperationLogTypeImpl> expectedTypes = Arrays.asList(type1, type2);
        when(operationLogTypeRepository.findAll()).thenReturn(expectedTypes);

        // Act
        List<OperationLogTypeImpl> result = operationLogTypeService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("EXECUTE_TASK", result.get(0).getType());
        assertEquals("CREATE", result.get(1).getType());
        verify(operationLogTypeRepository, times(1)).findAll();
    }

    @Test
    void listAll_WhenNoTypesExist_ReturnsEmptyList() {
        // Arrange
        when(operationLogTypeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<OperationLogTypeImpl> result = operationLogTypeService.listAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(operationLogTypeRepository, times(1)).findAll();
    }

    @Test
    void listAll_WhenSingleTypeExists_ReturnsSingleType() {
        // Arrange
        OperationLogTypeImpl type = new OperationLogTypeImpl();
        type.setId(1L);
        type.setType("DELETE");
        type.setOperationTypeId(4);
        type.setDescriptionTr("Silme");
        type.setDescriptionEn("Delete");
        type.setCreateDate(LocalDateTime.now());

        List<OperationLogTypeImpl> expectedTypes = Arrays.asList(type);
        when(operationLogTypeRepository.findAll()).thenReturn(expectedTypes);

        // Act
        List<OperationLogTypeImpl> result = operationLogTypeService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("DELETE", result.get(0).getType());
        assertEquals("Silme", result.get(0).getDescriptionTr());
        assertEquals("Delete", result.get(0).getDescriptionEn());
        verify(operationLogTypeRepository, times(1)).findAll();
    }

    @Test
    @SuppressWarnings("unchecked")
    void init_WhenNoTypesExist_CreatesAllOperationTypes() {
        // Arrange - All types don't exist
        when(operationLogTypeRepository.findByType(anyString())).thenReturn(Optional.empty());

        ArgumentCaptor<List<OperationLogTypeImpl>> captor = ArgumentCaptor.forClass(List.class);

        // Act
        operationLogTypeService.init();

        // Assert
        verify(operationLogTypeRepository, atLeastOnce()).findByType(anyString());
        verify(operationLogTypeRepository, times(1)).saveAll(captor.capture());

        List<OperationLogTypeImpl> savedTypes = captor.getValue();
        assertNotNull(savedTypes);
        assertEquals(16, savedTypes.size()); // All OperationType enum values

        // Verify some specific types with Turkish and English descriptions
        OperationLogTypeImpl createType = savedTypes.stream()
                .filter(t -> t.getType().equals("CREATE"))
                .findFirst()
                .orElse(null);
        assertNotNull(createType);
        assertEquals(1, createType.getOperationTypeId());
        assertEquals("Oluşturma", createType.getDescriptionTr());
        assertEquals("Create", createType.getDescriptionEn());

        OperationLogTypeImpl loginType = savedTypes.stream()
                .filter(t -> t.getType().equals("LOGIN"))
                .findFirst()
                .orElse(null);
        assertNotNull(loginType);
        assertEquals(5, loginType.getOperationTypeId());
        assertEquals("Oturum Açma", loginType.getDescriptionTr());
        assertEquals("Login", loginType.getDescriptionEn());

        OperationLogTypeImpl executeTaskType = savedTypes.stream()
                .filter(t -> t.getType().equals("EXECUTE_TASK"))
                .findFirst()
                .orElse(null);
        assertNotNull(executeTaskType);
        assertEquals(7, executeTaskType.getOperationTypeId());
        assertEquals("Görev Çalıştırma", executeTaskType.getDescriptionTr());
        assertEquals("Execute Task", executeTaskType.getDescriptionEn());
    }

    @Test
    void init_WhenTypesAlreadyExist_DoesNotCreateDuplicates() {
        // Arrange - All types already exist
        OperationLogTypeImpl existingType = new OperationLogTypeImpl();
        existingType.setId(1L);
        existingType.setType("CREATE");
        existingType.setOperationTypeId(1);
        existingType.setDescriptionTr("Oluşturma");
        existingType.setDescriptionEn("Create");

        when(operationLogTypeRepository.findByType(anyString())).thenReturn(Optional.of(existingType));

        // Act
        operationLogTypeService.init();

        // Assert
        verify(operationLogTypeRepository, atLeastOnce()).findByType(anyString());
        verify(operationLogTypeRepository, never()).saveAll(anyList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void init_WhenSomeTypesExist_CreatesOnlyMissingTypes() {
        // Arrange - Some types exist, some don't
        OperationLogTypeImpl existingType = new OperationLogTypeImpl();
        existingType.setId(1L);
        existingType.setType("CREATE");
        existingType.setOperationTypeId(1);
        existingType.setDescriptionTr("Oluşturma");
        existingType.setDescriptionEn("Create");

        when(operationLogTypeRepository.findByType(anyString())).thenAnswer(invocation -> {
            String type = invocation.getArgument(0);
            if (type.equals("CREATE")) {
                return Optional.of(existingType);
            }
            return Optional.empty();
        });

        ArgumentCaptor<List<OperationLogTypeImpl>> captor = ArgumentCaptor.forClass(List.class);

        // Act
        operationLogTypeService.init();

        // Assert
        verify(operationLogTypeRepository, atLeastOnce()).findByType(anyString());
        verify(operationLogTypeRepository, times(1)).saveAll(captor.capture());

        List<OperationLogTypeImpl> savedTypes = captor.getValue();
        assertNotNull(savedTypes);
        assertTrue(savedTypes.size() > 0);
        assertTrue(savedTypes.size() < 16); // Less than all types since CREATE already exists

        // Verify CREATE is not in the saved list
        boolean createExists = savedTypes.stream()
                .anyMatch(t -> t.getType().equals("CREATE"));
        assertFalse(createExists, "CREATE should not be saved as it already exists");
    }
}