package com.taskapproacher.auth.service;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.task.constant.Priority;
import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.repository.TaskBoardRepository;
import com.taskapproacher.task.repository.TaskRepository;
import com.taskapproacher.user.model.User;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccessCheckServiceTest {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskBoardRepository taskBoardRepository;
    @InjectMocks
    private AccessCheckService accessCheckService;

    private User createDefaultUser(UUID userID) {
        User user = new User();
        user.setID(userID);
        user.setUsername("Test user");
        user.setPassword("testUserPass");
        user.setEmail("mail@mail.mail");

        return user;
    }

    private Task createDefaultTask(UUID taskID, UUID boardID, UUID userID) {
        Task task = new Task();
        task.setID(taskID);
        task.setTitle("Test task");
        task.setDescription("Test task description");
        task.setPriority(Priority.STANDARD);
        task.setDeadline(LocalDate.now());
        task.setFinished(true);
        task.setTaskBoard(createDefaultTaskBoard(boardID, userID));

        return task;
    }

    private TaskBoard createDefaultTaskBoard(UUID boardID, UUID userID) {
        TaskBoard taskBoard = new TaskBoard();
        taskBoard.setID(boardID);
        taskBoard.setSorted(false);
        taskBoard.setUser(createDefaultUser(userID));

        return taskBoard;
    }

    @Test
    void hasAccessToBoard_ValidIDs_ReturnsTrue() {
        UUID boardID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, userID);

        when(taskBoardRepository.findByID(boardID)).thenReturn(Optional.of(taskBoard));

        boolean hasAccess = accessCheckService.hasAccessToBoard(boardID, userID);

        assertTrue(hasAccess);

        verify(taskBoardRepository, times(1)).findByID(boardID);
    }

    @Test
    void hasAccessToBoard_NullTaskBoardID_ThrowsIllegalArgumentException() {
        UUID boardID = null;
        UUID userID = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accessCheckService.hasAccessToBoard(boardID, userID);
        });

        String expectedMessage = "board id " + ExceptionMessage.NULL;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardRepository, times(0)).findByID(boardID);
    }

    @Test
    void hasAccessToBoard_NullPrincipalID_ThrowsIllegalArgumentException() {
        UUID boardID = UUID.randomUUID();
        UUID userID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accessCheckService.hasAccessToBoard(boardID, userID);
        });

        String expectedMessage = "Principal id " + ExceptionMessage.NULL;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardRepository, times(0)).findByID(boardID);
    }

    @Test
    void hasAccessToBoard_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();

        when(taskBoardRepository.findByID(boardID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accessCheckService.hasAccessToBoard(boardID, userID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardRepository, times(1)).findByID(boardID);
    }

    @Test
    void hasAccessToBoard_InvalidPrincipalID_ReturnsFalse() {
        UUID boardID = UUID.randomUUID();
        UUID otherUserID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, UUID.randomUUID());

        when(taskBoardRepository.findByID(boardID)).thenReturn(Optional.of(taskBoard));

        boolean hasAccess = accessCheckService.hasAccessToBoard(boardID, otherUserID);

        assertFalse(hasAccess);

        verify(taskBoardRepository, times(1)).findByID(boardID);
    }

    @Test
    void hasAccessToTask_ValidIDs_ReturnsTrue() {
        UUID taskID = UUID.randomUUID();
        UUID boardID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();

        Task task = createDefaultTask(taskID, boardID, userID);

        when(taskRepository.findByID(taskID)).thenReturn(Optional.of(task));

        boolean hasAccess = accessCheckService.hasAccessToTask(taskID, userID);

        assertTrue(hasAccess);

        verify(taskRepository, times(1)).findByID(taskID);
    }

    @Test
    void hasAccessToTask_NullTaskID_ThrowsIllegalArgumentException() {
        UUID taskID = null;
        UUID userID = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accessCheckService.hasAccessToTask(taskID, userID);
        });

        String expectedMessage = "Task id " + ExceptionMessage.NULL;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskRepository, times(0)).findByID(taskID);
    }

    @Test
    void hasAccessToTask_NullPrincipalID_ThrowsIllegalArgumentException() {
        UUID taskID = UUID.randomUUID();
        UUID userID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accessCheckService.hasAccessToTask(taskID, userID);
        });

        String expectedMessage = "Principal id " + ExceptionMessage.NULL;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskRepository, times(0)).findByID(taskID);
    }

    @Test
    void hasAccessToTask_InvalidTaskID_ThrowsEntityNotFoundException() {
        UUID taskID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();

        when(taskRepository.findByID(taskID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accessCheckService.hasAccessToTask(taskID, userID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskRepository, times(1)).findByID(taskID);
    }

    @Test
    void hasAccessToTask_InvalidPrincipalID_ReturnsFalse() {
        UUID taskID = UUID.randomUUID();
        UUID boardID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();

        Task task = createDefaultTask(taskID, boardID, userID);

        when(taskRepository.findByID(taskID)).thenReturn(Optional.of(task));

        boolean hasAccess = accessCheckService.hasAccessToTask(taskID, UUID.randomUUID());

        assertFalse(hasAccess);

        verify(taskRepository, times(1)).findByID(taskID);
    }
}
