package tr.org.lider.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.entities.TaskImpl;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class CommandRepositoryTest {

    @InjectMocks
    private CommandRepository commandRepositoryMock;

    @BeforeEach
    void setUp() {
        commandRepositoryMock = mock(CommandRepository.class);
    }

    @Test
    void findByTask_WithValidTask_ReturnsCommandList() {
        TaskImpl task = new TaskImpl();
        CommandImpl command = new CommandImpl();
        command.setTask(task);

        when(commandRepositoryMock.findByTask(task)).thenReturn(Collections.singletonList(command));

        List<CommandImpl> result = commandRepositoryMock.findByTask(task);

        assertEquals(1, result.size());
        assertEquals(task, result.get(0).getTask());
        verify(commandRepositoryMock).findByTask(task);
    }

    @Test
    void findAllByDnListJsonStringContaining_WithValidDn_ReturnsCommandList() {
        String dn = "ou=Test";
        CommandImpl command = new CommandImpl();
        command.setDnListJsonString("ou=Test");

        when(commandRepositoryMock.findAllByDnListJsonStringContaining(dn))
                .thenReturn(Collections.singletonList(command));

        List<CommandImpl> result = commandRepositoryMock.findAllByDnListJsonStringContaining(dn);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getDnListJsonString().contains(dn));
        verify(commandRepositoryMock).findAllByDnListJsonStringContaining(dn);
    }

    @Test
    void findCommandsOfAgent_WithDnAndNullUsername_ReturnsObjectArrayList() {
        String dn = "cn=agent1";

        Object[] row = new Object[]{"task1", "execution1", "owner1", 123L};
        List<Object[]> expected = Collections.singletonList(row);

        when(commandRepositoryMock.findCommandsOfAgent(dn, null)).thenReturn(expected);

        List<Object[]> result = commandRepositoryMock.findCommandsOfAgent(dn, null);

        assertEquals(1, result.size());
        assertEquals("task1", result.get(0)[0]);
        verify(commandRepositoryMock).findCommandsOfAgent(dn, null);
    }

    @Test
    void findCommandsOfAgent_WithDnAndUsername_ReturnsObjectArrayList() {
        String dn = "cn=agent1";
        String username = "admin";

        Object[] row = new Object[]{"task2", "execution2", "admin", 456L};
        List<Object[]> expected = Collections.singletonList(row);

        when(commandRepositoryMock.findCommandsOfAgent(dn, username)).thenReturn(expected);

        List<Object[]> result = commandRepositoryMock.findCommandsOfAgent(dn, username);

        assertEquals(1, result.size());
        assertEquals("admin", result.get(0)[2]);
        verify(commandRepositoryMock).findCommandsOfAgent(dn, username);
    }
}
