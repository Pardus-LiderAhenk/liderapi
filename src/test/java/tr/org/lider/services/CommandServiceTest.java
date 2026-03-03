package tr.org.lider.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.org.lider.constant.RoleConstants;
import tr.org.lider.entities.CommandExecutionImpl;
import tr.org.lider.entities.CommandExecutionResultImpl;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.entities.TaskImpl;
import tr.org.lider.repositories.CommandRepository;
import tr.org.lider.security.User;


@ExtendWith(MockitoExtension.class)
class CommandServiceTest {

    @InjectMocks
    private CommandService commandService;

    @Mock
    private CommandRepository commandRepository;

    @Mock
    private UserService userService;

    @Test
    void testGetExecutedTasks_AdminRole() {
        String dn = "agentDn";
        String username = "adminUser";

        User adminUser = new User();
        adminUser.setRoles(Collections.singletonList(RoleConstants.ROLE_ADMIN));

        TaskImpl task = new TaskImpl();
        CommandExecutionImpl commandExecution = new CommandExecutionImpl();
        CommandExecutionResultImpl resultImpl = new CommandExecutionResultImpl();
        byte[] responseData = "testResponse".getBytes();
        resultImpl.setResponseData(responseData);
        commandExecution.setCommandExecutionResults(Collections.singletonList(resultImpl));

        String commandOwnerUid = "ownerUid";
        Long commandId = 123L;

        Object[] row = new Object[]{task, commandExecution, commandOwnerUid, commandId};
        List<Object[]> repoResult = Collections.singletonList(row);

        try (MockedStatic<AuthenticationService> mockedStatic = mockStatic(AuthenticationService.class)) {
            mockedStatic.when(AuthenticationService::getUserName).thenReturn(username);
            when(userService.loadUserByUsername(username)).thenReturn(adminUser);
            when(commandRepository.findCommandsOfAgent(dn, null)).thenReturn(repoResult);

            List<CommandImpl> result = commandService.getExecutedTasks(dn);

            assertNotNull(result);
            assertEquals(1, result.size());
            CommandImpl command = result.get(0);
            assertEquals(commandOwnerUid, command.getCommandOwnerUid());
            assertEquals(commandId, command.getId());
            assertEquals(task, command.getTask());
            assertNotNull(command.getCommandExecutions());
            assertEquals(1, command.getCommandExecutions().size());
            CommandExecutionImpl ce = command.getCommandExecutions().get(0);
            assertEquals(commandExecution, ce);
            assertEquals("testResponse", ce.getCommandExecutionResults().get(0).getResponseDataStr());
        }
    }

    @Test
    void testGetExecutedTasks_UserRole_ResponseDataNull() {
        String dn = "agentDn";
        String username = "normalUser";

        User normalUser = new User();
        normalUser.setRoles(Collections.singletonList(RoleConstants.ROLE_USER));

        TaskImpl task = new TaskImpl();
        CommandExecutionImpl commandExecution = new CommandExecutionImpl();
        CommandExecutionResultImpl resultImpl = new CommandExecutionResultImpl();
        resultImpl.setResponseData(null);
        commandExecution.setCommandExecutionResults(Collections.singletonList(resultImpl));

        String commandOwnerUid = "ownerUid";
        Long commandId = 456L;

        Object[] row = new Object[]{task, commandExecution, commandOwnerUid, commandId};
        List<Object[]> repoResult = Collections.singletonList(row);

        try (MockedStatic<AuthenticationService> mockedStatic = mockStatic(AuthenticationService.class)) {
            mockedStatic.when(AuthenticationService::getUserName).thenReturn(username);
            when(userService.loadUserByUsername(username)).thenReturn(normalUser);
            when(commandRepository.findCommandsOfAgent(dn, username)).thenReturn(repoResult);

            List<CommandImpl> result = commandService.getExecutedTasks(dn);

            assertNotNull(result);
            assertEquals(1, result.size());
            CommandExecutionImpl ce = result.get(0).getCommandExecutions().get(0);
            assertNull(ce.getCommandExecutionResults().get(0).getResponseDataStr());
        }
    }

    @Test
    void testGetExecutedTasks_ResultIsNull() {
        String dn = "agentDn";
        String username = "user";

        User user = new User();
        user.setRoles(Collections.singletonList(RoleConstants.ROLE_USER));

        try (MockedStatic<AuthenticationService> mockedStatic = mockStatic(AuthenticationService.class)) {
            mockedStatic.when(AuthenticationService::getUserName).thenReturn(username);
            when(userService.loadUserByUsername(username)).thenReturn(user);
            when(commandRepository.findCommandsOfAgent(dn, username)).thenReturn(null);

            List<CommandImpl> result = commandService.getExecutedTasks(dn);

            assertNull(result);
        }
    }

    @Test
    void testGetExecutedTasks_EmptyResultList() {
        String dn = "agentDn";
        String username = "user";

        User user = new User();
        user.setRoles(Collections.singletonList(RoleConstants.ROLE_USER));

        try (MockedStatic<AuthenticationService> mockedStatic = mockStatic(AuthenticationService.class)) {
            mockedStatic.when(AuthenticationService::getUserName).thenReturn(username);
            when(userService.loadUserByUsername(username)).thenReturn(user);
            when(commandRepository.findCommandsOfAgent(dn, username)).thenReturn(Collections.emptyList());

            List<CommandImpl> result = commandService.getExecutedTasks(dn);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetExecutedTasks_CommandExecutionResultsEmpty() {
        String dn = "agentDn";
        String username = "user";

        User user = new User();
        user.setRoles(Collections.singletonList(RoleConstants.ROLE_USER));

        TaskImpl task = new TaskImpl();
        CommandExecutionImpl commandExecution = new CommandExecutionImpl();
        commandExecution.setCommandExecutionResults(Collections.emptyList());

        String commandOwnerUid = "ownerUid";
        Long commandId = 789L;

        Object[] row = new Object[]{task, commandExecution, commandOwnerUid, commandId};
        List<Object[]> repoResult = Collections.singletonList(row);

        try (MockedStatic<AuthenticationService> mockedStatic = mockStatic(AuthenticationService.class)) {
            mockedStatic.when(AuthenticationService::getUserName).thenReturn(username);
            when(userService.loadUserByUsername(username)).thenReturn(user);
            when(commandRepository.findCommandsOfAgent(dn, username)).thenReturn(repoResult);

            List<CommandImpl> result = commandService.getExecutedTasks(dn);

            assertNotNull(result);
            assertEquals(1, result.size());
            CommandExecutionImpl ce = result.get(0).getCommandExecutions().get(0);
            assertTrue(ce.getCommandExecutionResults().isEmpty());
        }
    }
}
