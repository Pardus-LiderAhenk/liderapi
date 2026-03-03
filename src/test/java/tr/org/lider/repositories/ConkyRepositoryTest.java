package tr.org.lider.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tr.org.lider.entities.ConkyTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ConkyRepositoryTest {


    @InjectMocks
    private ConkyRepository conkyRepositoryMock;

    @BeforeEach
    void setUp() {
        conkyRepositoryMock = mock(ConkyRepository.class);
    }

    @Test
    void testFindByDeletedOrderByCreateDateDesc() {
        Pageable pageable = PageRequest.of(1, 10);
        Boolean deleted = false;
        Page<ConkyTemplate> mockPage = mock(Page.class);

        when(conkyRepositoryMock.findByDeletedOrderByCreateDateDesc(pageable, deleted)).thenReturn(mockPage);
        Page<ConkyTemplate> result = conkyRepositoryMock.findByDeletedOrderByCreateDateDesc(pageable, deleted);

        assertEquals(mockPage, result);
        verify(conkyRepositoryMock).findByDeletedOrderByCreateDateDesc(pageable, deleted);
    }

    @Test
    void testFindAllByDeleted() {
        boolean deleted = true;
        ConkyTemplate mockTemplate = new ConkyTemplate();
        mockTemplate.setId(1L);
        mockTemplate.setDeleted(false);
        mockTemplate.setLabel("Test Template");
        mockTemplate.setContents("Test Content");

        when(conkyRepositoryMock.findAllByDeleted(deleted)).thenReturn(Collections.singletonList(mockTemplate));
        List<ConkyTemplate> result = conkyRepositoryMock.findAllByDeleted(deleted);

        assertEquals(1, result.size());
        assertEquals(mockTemplate.getId(), result.get(0).getId());
        assertEquals(mockTemplate.getLabel(), result.get(0).getLabel());
        verify(conkyRepositoryMock).findAllByDeleted(deleted);
    }
}
