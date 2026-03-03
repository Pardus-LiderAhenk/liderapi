package tr.org.lider.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.repositories.ExecutedTaskCriteriaBuilder;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import tr.org.lider.security.User;
import tr.org.lider.constant.RoleConstants;


@ExtendWith(MockitoExtension.class)
class ExecutedTaskReportServiceTest {

    @Mock
    private ExecutedTaskCriteriaBuilder executedTaskCB;
    
    @Mock
    private UserService userService;

    @InjectMocks
    private ExecutedTaskReportService executedTaskReportService;

    @Test
    void testFindAllCommandsFiltered_ReturnsPage() {
        int pageNumber = 0;
        int pageSize = 10;
        Optional<String> taskCommand = Optional.of("testCommand");
        Optional<Date> startDate = Optional.of(new Date());
        Optional<Date> endDate = Optional.of(new Date());

        CommandImpl command = new CommandImpl();
        Page<CommandImpl> mockPage = new PageImpl<>(Collections.singletonList(command));

        try (MockedStatic<AuthenticationService> mockedAuth = mockStatic(AuthenticationService.class)) {
            mockedAuth.when(AuthenticationService::getUserName).thenReturn("testUser");

            User mockUser = new User();
            mockUser.setRoles(Collections.singletonList(RoleConstants.ROLE_USER));
            when(userService.loadUserByUsername("testUser")).thenReturn(mockUser);

            when(executedTaskCB.filterCommands(
                    eq(pageNumber), eq(pageSize), eq(taskCommand), eq(startDate), eq(endDate), eq("testUser")))
                    .thenReturn(mockPage);

            Page<CommandImpl> result = executedTaskReportService.findAllCommandsFiltered(
                    pageNumber, pageSize, taskCommand, startDate, endDate);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(userService).loadUserByUsername("testUser"); 
            verify(executedTaskCB, times(1)).filterCommands(
                    pageNumber, pageSize, taskCommand, startDate, endDate, "testUser");
        }
    }

    @Test
    void testFindAllCommandsFiltered_EmptyResult() {
        int pageNumber = 1;
        int pageSize = 5;
        Optional<String> taskCommand = Optional.empty();
        Optional<Date> startDate = Optional.empty();
        Optional<Date> endDate = Optional.empty();

        Page<CommandImpl> mockPage = new PageImpl<>(Collections.emptyList());

        try (MockedStatic<AuthenticationService> mockedAuth = mockStatic(AuthenticationService.class)) {
            mockedAuth.when(AuthenticationService::getUserName).thenReturn("testUser");

            User mockUser = new User();
            mockUser.setRoles(Collections.singletonList(RoleConstants.ROLE_USER));
            when(userService.loadUserByUsername("testUser")).thenReturn(mockUser);

            when(executedTaskCB.filterCommands(
                    eq(pageNumber), eq(pageSize), eq(taskCommand), eq(startDate), eq(endDate), eq("testUser")))
                    .thenReturn(mockPage);

            Page<CommandImpl> result = executedTaskReportService.findAllCommandsFiltered(
                    pageNumber, pageSize, taskCommand, startDate, endDate);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            verify(userService).loadUserByUsername("testUser"); 
            verify(executedTaskCB, times(1)).filterCommands(
                    pageNumber, pageSize, taskCommand, startDate, endDate, "testUser");
        }
    }
}
