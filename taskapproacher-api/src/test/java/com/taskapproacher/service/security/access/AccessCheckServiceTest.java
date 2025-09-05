package com.taskapproacher.service.security.access;

import com.taskapproacher.enums.ErrorMessage;
import com.taskapproacher.dao.task.TaskDAO;
import com.taskapproacher.dao.task.TaskBoardDAO;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.task.TaskBoard;

import static org.junit.jupiter.api.Assertions.*;

import com.taskapproacher.enums.Priority;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.UUID;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AccessCheckServiceTest {
    @Mock
    private TaskDAO taskDAO;
    @Mock
    private TaskBoardDAO taskBoardDAO;
    @InjectMocks
    private AccessCheckService accessCheckService;

    private User createDefaultUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setUsername("Test user");
        user.setPassword("testUserPass");
        user.setEmail("mail@mail.mail");

        return user;
    }

    private Task createDefaultTask(UUID taskId, UUID boardId, UUID userId) {
        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Test task");
        task.setDescription("Test task description");
        task.setPriority(Priority.STANDARD);
        task.setDeadline(LocalDate.now());
        task.setFinished(true);
        task.setTaskBoard(createDefaultTaskBoard(boardId, userId));

        return task;
    }

    private TaskBoard createDefaultTaskBoard(UUID boardId, UUID userId) {
        TaskBoard taskBoard = new TaskBoard();
        taskBoard.setId(boardId);
        taskBoard.setSorted(false);
        taskBoard.setUser(createDefaultUser(userId));

        return taskBoard;
    }

    @Test
    void hasAccessToBoard_ValidIDs_ReturnsTrue() {
        UUID boardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardId, userId);

        when(taskBoardDAO.findById(boardId)).thenReturn(Optional.of(taskBoard));

        boolean hasAccess = accessCheckService.hasAccessToBoard(boardId, userId);

        assertTrue(hasAccess);

        verify(taskBoardDAO, times(1)).findById(boardId);
    }

    @Test
    void hasAccessToBoard_NullTaskBoardId_ThrowsIllegalArgumentException() {
        UUID boardId = null;
        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accessCheckService.hasAccessToBoard(boardId, userId);
        });

        String expectedMessage = "board id " + ErrorMessage.NULL;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).findById(boardId);
    }

    @Test
    void hasAccessToBoard_NullPrincipalId_ThrowsIllegalArgumentException() {
        UUID boardId = UUID.randomUUID();
        UUID userId = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accessCheckService.hasAccessToBoard(boardId, userId);
        });

        String expectedMessage = "Principal id " + ErrorMessage.NULL;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).findById(boardId);
    }

    @Test
    void hasAccessToBoard_InvalidTaskBoardId_ThrowsEntityNotFoundException() {
        UUID boardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(taskBoardDAO.findById(boardId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accessCheckService.hasAccessToBoard(boardId, userId);
        });

        String expectedMessage = ErrorMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(1)).findById(boardId);
    }

    @Test
    void hasAccessToBoard_InvalidPrincipalID_ReturnsFalse() {
        UUID boardId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardId, UUID.randomUUID());

        when(taskBoardDAO.findById(boardId)).thenReturn(Optional.of(taskBoard));

        boolean hasAccess = accessCheckService.hasAccessToBoard(boardId, otherUserId);

        assertFalse(hasAccess);

        verify(taskBoardDAO, times(1)).findById(boardId);
    }

    @Test
    void hasAccessToTask_ValidIDs_ReturnsTrue() {
        UUID taskId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Task task = createDefaultTask(taskId, boardId, userId);

        when(taskDAO.findById(taskId)).thenReturn(Optional.of(task));

        boolean hasAccess = accessCheckService.hasAccessToTask(taskId, userId);

        assertTrue(hasAccess);

        verify(taskDAO, times(1)).findById(taskId);
    }

    @Test
    void hasAccessToTask_NullTaskId_ThrowsIllegalArgumentException() {
        UUID taskId = null;
        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accessCheckService.hasAccessToTask(taskId, userId);
        });

        String expectedMessage = "Task id " + ErrorMessage.NULL;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskDAO, times(0)).findById(taskId);
    }

    @Test
    void hasAccessToTask_NullPrincipalId_ThrowsIllegalArgumentException() {
        UUID taskId = UUID.randomUUID();
        UUID userId = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accessCheckService.hasAccessToTask(taskId, userId);
        });

        String expectedMessage = "Principal id " + ErrorMessage.NULL;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskDAO, times(0)).findById(taskId);
    }

    @Test
    void hasAccessToTask_InvalidTaskId_ThrowsEntityNotFoundException() {
        UUID taskId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(taskDAO.findById(taskId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accessCheckService.hasAccessToTask(taskId, userId);
        });

        String expectedMessage = ErrorMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskDAO, times(1)).findById(taskId);
    }

    @Test
    void hasAccessToTask_InvalidPrincipalID_ReturnsFalse() {
        UUID taskId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Task task = createDefaultTask(taskId, boardId, userId);

        when(taskDAO.findById(taskId)).thenReturn(Optional.of(task));

        boolean hasAccess = accessCheckService.hasAccessToTask(taskId, UUID.randomUUID());

        assertFalse(hasAccess);

        verify(taskDAO, times(1)).findById(taskId);
    }
}
