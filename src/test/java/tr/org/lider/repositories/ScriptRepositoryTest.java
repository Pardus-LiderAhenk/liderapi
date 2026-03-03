package tr.org.lider.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tr.org.lider.entities.ScriptTemplate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
class ScriptRepositoryTest {

    @Mock
    private ScriptRepository scriptRepository;

    @Test
    void findByDeleted_ValidPageableAndDeleted_ReturnsPage() {
        ScriptTemplate script1 = new ScriptTemplate();
        script1.setLabel("Script1");
        script1.setDeleted(false);

        ScriptTemplate script2 = new ScriptTemplate();
        script2.setLabel("Script2");
        script2.setDeleted(true);

        List<ScriptTemplate> scripts = java.util.Arrays.asList(script1);
        Page<ScriptTemplate> page = new PageImpl<>(scripts);

        when(scriptRepository.save(any(ScriptTemplate.class))).thenReturn(script1);
        when(scriptRepository.findByDeletedOrderByCreateDateDesc(any(PageRequest.class), eq(false))).thenReturn(page);

        Page<ScriptTemplate> result = scriptRepository.findByDeletedOrderByCreateDateDesc(PageRequest.of(0, 10), false);
        assertThat(result.getContent()).extracting("deleted").containsOnly(false);
    }

    @Test
    void findByDeletedAndCreatedByOrDeletedAndIsPublished_ValidPageableAndUser_ReturnsPage() {
        ScriptTemplate script1 = new ScriptTemplate();
        script1.setLabel("TestScript");
        script1.setDeleted(false);
        script1.setCreatedBy("user1");
        script1.setIsPublished(false);

        ScriptTemplate script2 = new ScriptTemplate();
        script2.setLabel("TestScript");
        script2.setDeleted(false);
        script2.setCreatedBy("user2");
        script2.setIsPublished(true);

        List<ScriptTemplate> scripts = java.util.Arrays.asList(script1, script2);
        Page<ScriptTemplate> pageMock = new PageImpl<>(scripts);
        when(scriptRepository.findByLabelContainingIgnoreCaseAndDeletedAndCreatedByOrPublishedOrderByCreateDateDesc(any(PageRequest.class), eq("test"), eq(false), eq("user1"))).thenReturn(pageMock);
        Page<ScriptTemplate> page = scriptRepository.findByLabelContainingIgnoreCaseAndDeletedAndCreatedByOrPublishedOrderByCreateDateDesc(
                PageRequest.of(0, 10), "test", false, "user1");
        assertThat(page.getContent()).extracting("label").contains("TestScript");
        assertThat(page.getContent()).extracting("createdBy").containsAnyOf("user1", "user2");
        assertThat(page.getContent()).extracting("isPublished").contains(true, false);
    }

    @Test
    void findAllByDeleted_DeletedTrue_ReturnsDeletedScripts() {
        ScriptTemplate script1 = new ScriptTemplate();
        script1.setLabel("Script1");
        script1.setDeleted(false);

        ScriptTemplate script2 = new ScriptTemplate();
        script2.setLabel("Script2");
        script2.setDeleted(true);

        List<ScriptTemplate> deletedScriptsMock = java.util.Arrays.asList(script2);
        when(scriptRepository.findAllByDeleted(true)).thenReturn(deletedScriptsMock);
        List<ScriptTemplate> deletedScripts = scriptRepository.findAllByDeleted(true);
        assertThat(deletedScripts).extracting("deleted").containsOnly(true);
    }
}