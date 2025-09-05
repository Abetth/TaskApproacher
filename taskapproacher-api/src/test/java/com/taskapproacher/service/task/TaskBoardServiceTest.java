package com.taskapproacher.service.task;

import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.dao.task.TaskBoardDAO;
import com.taskapproacher.entity.user.UserResponse;
import com.taskapproacher.service.user.UserService;
import com.taskapproacher.enums.ErrorMessage;
import com.taskapproacher.enums.Role;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.BeanUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentMatchers;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

//Tests naming convention: method_scenario_result
@ExtendWith(MockitoExtension.class)
public class TaskBoardServiceTest {
    @Mock
    private TaskBoardDAO taskBoardDAO;
    @Mock
    private UserService userService;
    @InjectMocks
    private TaskBoardService taskBoardService;

    private List<Task> createDefaultListOfTasks() {
        Task firstTask = new Task();
        firstTask.setId(UUID.randomUUID());
        Task secondTask = new Task();
        secondTask.setId(UUID.randomUUID());

        return List.of(firstTask, secondTask);
    }

    private TaskBoard createDefaultTaskBoard(UUID boardId, User user) {
        TaskBoard taskBoard = new TaskBoard();
        taskBoard.setId(boardId);
        taskBoard.setTitle("Test Task Board");
        taskBoard.setSorted(false);
        taskBoard.setTasks(null);
        taskBoard.setUser(user);

        return taskBoard;
    }

    private User createDefaultUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setUsername("User 1");
        user.setPassword("Userpassword1");
        user.setEmail("mail@mail.mail");
        user.setRole(Role.USER);
        user.setTaskBoards(null);

        return user;
    }

    @Test
    void findById_ValidTaskBoardID_ReturnsTaskBoard() {
        UUID boardId = UUID.randomUUID();

        TaskBoard mockBoard = new TaskBoard();
        mockBoard.setId(boardId);

        when(taskBoardDAO.findById(boardId)).thenReturn(Optional.of(mockBoard));

        TaskBoard taskBoard = taskBoardService.findById(boardId);

        assertEquals(boardId, taskBoard.getId());

        verify(taskBoardDAO, times(1)).findById(boardId);
    }

    @Test
    void findById_NullTaskBoardID_ThrowsIllegalArgumentException() {
        UUID boardId = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.findById(boardId);
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).findById(boardId);
    }

    @Test
    void findById_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardId = UUID.randomUUID();

        when(taskBoardDAO.findById(boardId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.findById(boardId);
        });

        String expectedMessage = ErrorMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(1)).findById(boardId);
    }

    @Test
    void findByTaskBoard_ValidTaskBoardID_ReturnsTaskList() {
        UUID boardId = UUID.randomUUID();

        TaskBoard taskBoard = new TaskBoard();
        taskBoard.setId(boardId);

        List<Task> mockListOfTasks = createDefaultListOfTasks();

        when(taskBoardDAO.findById(boardId)).thenReturn(Optional.of(taskBoard));
        when(taskBoardDAO.findRelatedEntitiesByID(boardId)).thenReturn(mockListOfTasks);

        List<Task> listOfTasks = taskBoardService.findByTaskBoard(boardId);

        assertEquals(2, listOfTasks.size());
        assertEquals(listOfTasks.get(0).getId(), mockListOfTasks.get(0).getId());
        assertEquals(listOfTasks.get(1).getId(), mockListOfTasks.get(1).getId());

        verify(taskBoardDAO, times(1)).findById(boardId);
        verify(taskBoardDAO, times(1)).findRelatedEntitiesByID(boardId);
    }

    @Test
    void findByTaskBoard_NullTaskBoardID_ThrowsIllegalArgumentException() {
        UUID boardId = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.findByTaskBoard(boardId);
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).findRelatedEntitiesByID(boardId);
    }

    @Test
    void findByTaskBoard_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardId = UUID.randomUUID();

        when(taskBoardDAO.findById(boardId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.findByTaskBoard(boardId);
        });

        String expectedMessage = ErrorMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(1)).findById(boardId);
    }

    @Test
    void create_ValidTaskBoard_ReturnsTaskBoardResponse() {
        UUID userId = UUID.randomUUID();

        User user = createDefaultUser(userId);
        TaskBoard taskBoard = createDefaultTaskBoard(null, null);

        UserResponse userResponseForTaskBoard = new UserResponse(user);

        when(userService.findById(userId)).thenReturn(user);
        when(taskBoardDAO.save(ArgumentMatchers.any(TaskBoard.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskBoardResponse response = taskBoardService.create(userId, taskBoard);

        assertEquals(taskBoard.getTitle(), response.getTitle());
        assertEquals(taskBoard.isSorted(), response.isSorted());
        assertEquals(userResponseForTaskBoard, response.getUser());

        verify(userService, times(1)).findById(userId);
        verify(taskBoardDAO, times(1)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void create_EmptyTaskBoardTitle_ThrowsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(null, null);
        taskBoard.setTitle("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.create(userId, taskBoard);
        });

        String expectedMessage = ErrorMessage.EMPTY.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void create_NullTaskBoardTitle_ThrowsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(null, null);
        taskBoard.setTitle(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.create(userId, taskBoard);
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void create_InvalidUserID_ThrowsEntityNotFoundException() {
        UUID userId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(null, null);

        when(userService.findById(userId)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.create(userId, taskBoard);
        });

        verify(userService, times(1)).findById(userId);
        verify(taskBoardDAO, times(0)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void create_NullUserID_ThrowsIllegalArgumentException() {
        UUID userId = null;

        TaskBoard taskBoard = createDefaultTaskBoard(null, null);

        when(userService.findById(userId)).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.create(userId, taskBoard);
        });

        verify(userService, times(1)).findById(userId);
        verify(taskBoardDAO, times(0)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void update_ValidTaskBoard_ReturnsTaskBoardResponse() {
        UUID boardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = createDefaultUser(userId);
        List<Task> listOfTasks = createDefaultListOfTasks();

        TaskBoard existingTaskBoard = createDefaultTaskBoard(boardId, user);
        existingTaskBoard.setTasks(listOfTasks);

        TaskBoard copyOfExistingTaskBoard = new TaskBoard();
        BeanUtils.copyProperties(existingTaskBoard, copyOfExistingTaskBoard);

        TaskBoard updateData = createDefaultTaskBoard(null, user);
        updateData.setTitle("Board 1");
        updateData.setSorted(true);

        ArgumentCaptor<TaskBoard> captor = ArgumentCaptor.forClass(TaskBoard.class);

        when(taskBoardDAO.findById(boardId)).thenReturn(Optional.of(copyOfExistingTaskBoard));
        when(taskBoardDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskBoardResponse response = taskBoardService.update(boardId, updateData);
        TaskBoard capturedBoard = captor.getValue();

        assertEquals(existingTaskBoard.getId(), capturedBoard.getId());
        assertNotEquals(existingTaskBoard.getTitle(), capturedBoard.getTitle());
        assertNotEquals(existingTaskBoard.isSorted(), capturedBoard.isSorted());
        assertEquals(existingTaskBoard.getTasks(), capturedBoard.getTasks());
        assertEquals(existingTaskBoard.getUser(), capturedBoard.getUser());

        assertNotNull(response);
        assertEquals(updateData.getTitle(), response.getTitle());
        assertEquals(updateData.isSorted(), response.isSorted());

        verify(taskBoardDAO, times(1)).findById(boardId);
        verify(taskBoardDAO, times(1)).update(captor.capture());
    }

    @Test
    void update_NullTaskBoardId_ThrowsIllegalArgumentException() {
        UUID boardId = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.update(boardId, new TaskBoard());
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).update(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void update_InvalidTaskBoardId_ThrowsEntityNotFoundException() {
        UUID boardId = UUID.randomUUID();

        when(taskBoardDAO.findById(boardId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.update(boardId, new TaskBoard());
        });

        String expectedMessage = ErrorMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(1)).findById(boardId);
        verify(taskBoardDAO, times(0)).update(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void update_EmptyTaskBoardTitle_ReturnsTaskBoardResponseWithSameTitle() {
        UUID boardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = createDefaultUser(userId);
        List<Task> listOfTasks = createDefaultListOfTasks();

        TaskBoard existingTaskBoard = createDefaultTaskBoard(boardId, user);
        existingTaskBoard.setTasks(listOfTasks);

        TaskBoard copyOfExistingTaskBoard = new TaskBoard();
        BeanUtils.copyProperties(existingTaskBoard, copyOfExistingTaskBoard);

        TaskBoard updateData = createDefaultTaskBoard(null, user);
        updateData.setTitle("");

        ArgumentCaptor<TaskBoard> captor = ArgumentCaptor.forClass(TaskBoard.class);

        when(taskBoardDAO.findById(boardId)).thenReturn(Optional.of(copyOfExistingTaskBoard));
        when(taskBoardDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskBoardResponse response = taskBoardService.update(boardId, updateData);
        TaskBoard capturedBoard = captor.getValue();

        assertEquals(existingTaskBoard.getId(), capturedBoard.getId());
        assertEquals(existingTaskBoard.getTitle(), capturedBoard.getTitle());
        assertEquals(existingTaskBoard.isSorted(), capturedBoard.isSorted());
        assertEquals(existingTaskBoard.getTasks(), capturedBoard.getTasks());
        assertEquals(existingTaskBoard.getUser(), capturedBoard.getUser());

        assertNotNull(response);
        assertNotEquals(updateData.getTitle(), response.getTitle());

        verify(taskBoardDAO, times(1)).findById(boardId);
        verify(taskBoardDAO, times(1)).update(captor.capture());
    }

    @Test
    void update_NullTaskBoardTitle_ReturnsTaskBoardResponseWithSameTitle() {
        UUID boardId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = createDefaultUser(userId);
        List<Task> listOfTasks = createDefaultListOfTasks();

        TaskBoard existingTaskBoard = createDefaultTaskBoard(boardId, user);
        existingTaskBoard.setId(boardId);
        existingTaskBoard.setTasks(listOfTasks);

        TaskBoard copyOfExistingTaskBoard = new TaskBoard();
        BeanUtils.copyProperties(existingTaskBoard, copyOfExistingTaskBoard);

        TaskBoard updateData = createDefaultTaskBoard(null, user);
        updateData.setTitle(null);

        ArgumentCaptor<TaskBoard> captor = ArgumentCaptor.forClass(TaskBoard.class);

        when(taskBoardDAO.findById(boardId)).thenReturn(Optional.of(copyOfExistingTaskBoard));
        when(taskBoardDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskBoardResponse response = taskBoardService.update(boardId, updateData);
        TaskBoard capturedBoard = captor.getValue();

        assertEquals(existingTaskBoard.getId(), capturedBoard.getId());
        assertEquals(existingTaskBoard.getTitle(), capturedBoard.getTitle());
        assertEquals(existingTaskBoard.isSorted(), capturedBoard.isSorted());
        assertEquals(existingTaskBoard.getTasks(), capturedBoard.getTasks());
        assertEquals(existingTaskBoard.getUser(), capturedBoard.getUser());

        assertNotNull(response);
        assertNotEquals(updateData.getTitle(), response.getTitle());

        verify(taskBoardDAO, times(1)).findById(boardId);
        verify(taskBoardDAO, times(1)).update(captor.capture());
    }

    @Test
    void delete_ValidTaskBoardID_TaskBoardDeletedSuccessfully() {
        UUID boardId = UUID.randomUUID();

        doNothing().when(taskBoardDAO).delete(boardId);

        taskBoardService.delete(boardId);

        verify(taskBoardDAO, times(1)).delete(boardId);
    }

    @Test
    void delete_NullTaskBoardID_ThrowsIllegalArgumentException() {
        UUID boardId = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.delete(boardId);
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).delete(boardId);
    }
}
