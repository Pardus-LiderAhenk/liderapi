package tr.org.lider.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.dto.ScheduledTaskDTO;
import tr.org.lider.security.User;
import tr.org.lider.constant.RoleConstants;
import tr.org.lider.repositories.ScheduledTaskCriteriaBuilder;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ScheduledTaskReportServiceTest {

    @Mock
    private ScheduledTaskCriteriaBuilder scheduledTaskCB;

    @Mock
    private UserService userService;

    @InjectMocks
    private ScheduledTaskReportService scheduledTaskReportService;

    @Test
    void testFindAllCommandsFiltered_AsAdminUser_SeesAllTasks() {
        try (MockedStatic<AuthenticationService> mockedAuth = mockStatic(AuthenticationService.class)) {
            mockedAuth.when(AuthenticationService::getUserName).thenReturn("adminUser");

            User adminUser = new User();
            adminUser.setRoles(Collections.singletonList(RoleConstants.ROLE_ADMIN));

            when(userService.loadUserByUsername("adminUser")).thenReturn(adminUser);

            ScheduledTaskDTO dto = new ScheduledTaskDTO();

            Page<CommandImpl> mockPage = new PageImpl<>(Collections.emptyList());
            when(scheduledTaskCB.filterCommands(dto)).thenReturn(mockPage);

            Page<CommandImpl> result = scheduledTaskReportService.findAllCommandsFiltered(dto);

            assertNull(dto.getUsername());
            assertSame(mockPage, result);

            verify(userService).loadUserByUsername("adminUser");
            verify(scheduledTaskCB).filterCommands(dto);
        }
    }

    @Test
    void testFindAllCommandsFiltered_AsNormalUser_UsernameSet() {
        try (MockedStatic<AuthenticationService> mockedAuth = mockStatic(AuthenticationService.class)) {
            mockedAuth.when(AuthenticationService::getUserName).thenReturn("normalUser");

            User normalUser = new User();
            normalUser.setRoles(Collections.singletonList(RoleConstants.ROLE_USER));

            when(userService.loadUserByUsername("normalUser")).thenReturn(normalUser);

            ScheduledTaskDTO dto = new ScheduledTaskDTO();

            Page<CommandImpl> mockPage = new PageImpl<>(Collections.emptyList());
            when(scheduledTaskCB.filterCommands(dto)).thenReturn(mockPage);

            Page<CommandImpl> result = scheduledTaskReportService.findAllCommandsFiltered(dto);

            assertEquals("normalUser", dto.getUsername());
            assertSame(mockPage, result);

            verify(userService).loadUserByUsername("normalUser");
            verify(scheduledTaskCB).filterCommands(dto);
        }
    }
}
