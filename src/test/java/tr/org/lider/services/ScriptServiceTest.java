package tr.org.lider.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import tr.org.lider.constant.RoleConstants;
import tr.org.lider.entities.*;
import tr.org.lider.repositories.ScriptRepository;
import tr.org.lider.security.User;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ScriptServiceTest {

    @Mock
    private ScriptRepository scriptRepository;

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ScriptService scriptService;

    @Test
    void find_ExistingScript_ReturnsScript() {
        ScriptTemplate script = new ScriptTemplate();
        script.setId(1L);
        when(scriptRepository.findById(1L)).thenReturn(Optional.of(script));
        Optional<ScriptTemplate> result = scriptService.find(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void listAll_WhenCalled_ReturnsAllScripts() {
        List<ScriptTemplate> scripts = Arrays.asList(new ScriptTemplate(), new ScriptTemplate());
        when(scriptRepository.findAllByDeleted(false)).thenReturn(scripts);
        List<ScriptTemplate> result = scriptService.listAll();
        assertEquals(2, result.size());
    }

    @Test
    void list_AdminUser_ReturnsAllScriptsPaged() {
        String username = "admin";
        User user = mock(User.class);
        when(user.getRoles()).thenReturn(Arrays.asList(RoleConstants.ROLE_ADMIN));
        when(userService.loadUserByUsername(username)).thenReturn(user);
        Page<ScriptTemplate> page = new PageImpl<>(Arrays.asList(new ScriptTemplate()));
        when(scriptRepository.findByLabelContainingIgnoreCaseAndDeletedOrderByCreateDateDesc(any(PageRequest.class), anyString(), eq(false))).thenReturn(page);

        Map<String, String> params = Collections.singletonMap("scriptName", "test");
        try (MockedStatic<AuthenticationService> mocked = mockStatic(AuthenticationService.class)) {
            mocked.when(AuthenticationService::getUserName).thenReturn(username);
            Page<ScriptTemplate> result = scriptService.list(1, 10, params);
            assertEquals(1, result.getTotalElements());
        }
    }

    @Test
    void list_NonAdminUser_ReturnsOwnAndPublishedScriptsPaged() {
        String username = "user";
        User user = mock(User.class);
        when(user.getRoles()).thenReturn(Arrays.asList("ROLE_USER"));
        when(userService.loadUserByUsername(username)).thenReturn(user);
        Page<ScriptTemplate> page = new PageImpl<>(Arrays.asList(new ScriptTemplate()));
        when(scriptRepository.findByLabelContainingIgnoreCaseAndDeletedAndCreatedByOrPublishedOrderByCreateDateDesc(any(PageRequest.class), anyString(), eq(false), eq(username))).thenReturn(page);

        Map<String, String> params = Collections.singletonMap("scriptName", "test");
        try (MockedStatic<AuthenticationService> mocked = mockStatic(AuthenticationService.class)) {
            mocked.when(AuthenticationService::getUserName).thenReturn(username);
            Page<ScriptTemplate> result = scriptService.list(1, 10, params);
            assertEquals(1, result.getTotalElements());
        }
    }

    @Test
    void add_ValidScript_SetsCreatedByAndDefaultsAndLogsOperation() {
        ScriptTemplate script = new ScriptTemplate();
        script.setContents("echo test");
        String username = "user";
        when(scriptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<AuthenticationService> mocked = mockStatic(AuthenticationService.class)) {
            mocked.when(AuthenticationService::getUserName).thenReturn(username);
            ScriptTemplate saved = scriptService.add(script);
            assertEquals(username, saved.getCreatedBy());
            assertFalse(saved.isDeleted());
            assertFalse(saved.getIsPublished());
            verify(operationLogService).saveOperationLog(eq(OperationType.CREATE), anyString(), any());
        }
    }

    @Test
    void delete_AdminUser_MarksScriptDeletedAndLogsOperation() {
        ScriptTemplate script = new ScriptTemplate();
        script.setId(1L);
        script.setCreatedBy("someone");
        script.setContents("echo test");
        String username = "admin";
        User user = mock(User.class);
        when(user.getRoles()).thenReturn(Arrays.asList(RoleConstants.ROLE_ADMIN));
        when(userService.loadUserByUsername(username)).thenReturn(user);
        when(scriptRepository.findOne(1L)).thenReturn(script);
        when(scriptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<AuthenticationService> mocked = mockStatic(AuthenticationService.class)) {
            mocked.when(AuthenticationService::getUserName).thenReturn(username);
            ScriptTemplate deleted = scriptService.delete(1L);
            assertTrue(deleted.isDeleted());
            verify(operationLogService).saveOperationLog(eq(OperationType.DELETE), anyString(), any());
        }
    }

    @Test
    void delete_NonAdminOwner_MarksScriptDeleted() {
        ScriptTemplate script = new ScriptTemplate();
        script.setId(1L);
        script.setCreatedBy("user");
        script.setContents("echo test");
        String username = "user";
        User user = mock(User.class);
        when(user.getRoles()).thenReturn(Arrays.asList("ROLE_USER"));
        when(userService.loadUserByUsername(username)).thenReturn(user);
        when(scriptRepository.findOne(1L)).thenReturn(script);
        when(scriptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<AuthenticationService> mocked = mockStatic(AuthenticationService.class)) {
            mocked.when(AuthenticationService::getUserName).thenReturn(username);
            ScriptTemplate deleted = scriptService.delete(1L);
            assertTrue(deleted.isDeleted());
        }
    }

    @Test
    void delete_NonAdminNotOwner_ThrowsAccessDeniedException() {
        ScriptTemplate script = new ScriptTemplate();
        script.setId(1L);
        script.setCreatedBy("other");
        script.setContents("echo test");
        String username = "user";
        User user = mock(User.class);
        when(user.getRoles()).thenReturn(Arrays.asList("ROLE_USER"));
        when(userService.loadUserByUsername(username)).thenReturn(user);
        when(scriptRepository.findOne(1L)).thenReturn(script);

        try (MockedStatic<AuthenticationService> mocked = mockStatic(AuthenticationService.class)) {
            mocked.when(AuthenticationService::getUserName).thenReturn(username);
            assertThrows(AccessDeniedException.class, () -> scriptService.delete(1L));
        }
    }

    @Test
    void update_AdminUser_UpdatesScriptAndLogsOperation() {
        ScriptTemplate script = new ScriptTemplate();
        script.setId(1L);
        script.setContents("echo test");
        ScriptTemplate existing = new ScriptTemplate();
        existing.setId(1L);
        existing.setCreatedBy("someone");
        existing.setIsPublished(true);
        String username = "admin";
        User user = mock(User.class);
        when(user.getRoles()).thenReturn(Arrays.asList(RoleConstants.ROLE_ADMIN));
        when(userService.loadUserByUsername(username)).thenReturn(user);
        when(scriptRepository.findOne(1L)).thenReturn(existing);
        when(scriptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<AuthenticationService> mocked = mockStatic(AuthenticationService.class)) {
            mocked.when(AuthenticationService::getUserName).thenReturn(username);
            ScriptTemplate updated = scriptService.update(script);
            assertEquals(existing.getCreatedBy(), updated.getCreatedBy());
            assertEquals(existing.getIsPublished(), updated.getIsPublished());
            assertFalse(updated.isDeleted());
            verify(operationLogService).saveOperationLog(eq(OperationType.UPDATE), anyString(), any());
        }
    }

    @Test
    void update_NonAdminNotOwner_ThrowsAccessDeniedException() {
        ScriptTemplate script = new ScriptTemplate();
        script.setId(1L);
        script.setContents("echo test");
        ScriptTemplate existing = new ScriptTemplate();
        existing.setId(1L);
        existing.setCreatedBy("other");
        existing.setIsPublished(true);
        String username = "user";
        User user = mock(User.class);
        when(user.getRoles()).thenReturn(Arrays.asList("ROLE_USER"));
        when(userService.loadUserByUsername(username)).thenReturn(user);
        when(scriptRepository.findOne(1L)).thenReturn(existing);

        try (MockedStatic<AuthenticationService> mocked = mockStatic(AuthenticationService.class)) {
            mocked.when(AuthenticationService::getUserName).thenReturn(username);
            assertThrows(AccessDeniedException.class, () -> scriptService.update(script));
        }
    }

    @Test
    void update_NonAdminOwner_UpdatesScript() {
        ScriptTemplate script = new ScriptTemplate();
        script.setId(1L);
        script.setContents("echo test");
        ScriptTemplate existing = new ScriptTemplate();
        existing.setId(1L);
        existing.setCreatedBy("user");
        existing.setIsPublished(true);
        String username = "user";
        User user = mock(User.class);
        when(user.getRoles()).thenReturn(Arrays.asList("ROLE_USER"));
        when(userService.loadUserByUsername(username)).thenReturn(user);
        when(scriptRepository.findOne(1L)).thenReturn(existing);
        when(scriptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<AuthenticationService> mocked = mockStatic(AuthenticationService.class)) {
            mocked.when(AuthenticationService::getUserName).thenReturn(username);
            ScriptTemplate updated = scriptService.update(script);
            assertEquals(existing.getCreatedBy(), updated.getCreatedBy());
            assertEquals(existing.getIsPublished(), updated.getIsPublished());
            assertFalse(updated.isDeleted());
        }
    }
}