package com.taskapproacher.service.task;

import com.taskapproacher.constant.ExceptionMessage;
import com.taskapproacher.constant.Priority;
import com.taskapproacher.constant.Role;
import com.taskapproacher.dao.task.TaskBoardDAO;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.entity.task.response.TaskResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.interfaces.matcher.TaskBoardMatcher;
import com.taskapproacher.service.user.UserService;
import com.taskapproacher.test.utils.TestApproacherUtils;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    private Task createDefaultTask(UUID taskID) {
        Task task = new Task();
        task.setID(taskID);
        task.setTitle("Default task");
        task.setDescription("Default task description");
        task.setPriority(Priority.STANDARD);
        task.setDeadline(LocalDate.now());
        task.setFinished(false);
        task.setTaskBoard(null);

        return task;
    }

    private List<Task> createDefaultListOfTasks() {
        Task firstTask = createDefaultTask(UUID.randomUUID());
        firstTask.setID(UUID.randomUUID());
        firstTask.setTitle("First task");
        Task secondTask = createDefaultTask(UUID.randomUUID());
        secondTask.setID(UUID.randomUUID());
        secondTask.setTitle("Second task");

        return List.of(firstTask, secondTask);
    }

    private TaskBoard createDefaultTaskBoard(UUID boardID, User user) {
        TaskBoard taskBoard = new TaskBoard();
        taskBoard.setID(boardID);
        taskBoard.setTitle("Test Task Board");
        taskBoard.setSorted(false);
        taskBoard.setTasks(null);
        taskBoard.setUser(user);

        return taskBoard;
    }

    private User createDefaultUser(UUID userID) {
        User user = new User();
        user.setID(userID);
        user.setUsername("User 1");
        user.setPassword("Userpassword1");
        user.setEmail("mail@mail.mail");
        user.setRole(Role.USER);
        user.setTaskBoards(null);

        return user;
    }

    private void assertTaskBoardEquals(TaskBoardMatcher expected, TaskBoardMatcher actual) {
        assertAll(() -> {
            TestApproacherUtils.assertEqualsIfNotNull(expected.getID(), actual.getID());
            TestApproacherUtils.assertEqualsIfNotNull(expected.getTitle(), actual.getTitle());
            TestApproacherUtils.assertEqualsIfNotNull(expected.isSorted(), actual.isSorted());
            TestApproacherUtils.assertEqualsIfNotNull(expected.getTasks(), actual.getTasks());
            TestApproacherUtils.assertEqualsIfNotNull(expected.getUser(), actual.getUser());
        });
    }

    @Test
    void findByID_ValidTaskBoardID_ReturnsTaskBoard() {
        UUID boardID = UUID.randomUUID();

        TaskBoard mockBoard = createDefaultTaskBoard(boardID, new User());

        when(taskBoardDAO.findByID(boardID)).thenReturn(Optional.of(mockBoard));

        TaskBoard taskBoard = taskBoardService.findByID(boardID);

        assertTaskBoardEquals(mockBoard, taskBoard);

        verify(taskBoardDAO, times(1)).findByID(boardID);
    }

    @Test
    void findByID_NullTaskBoardID_ThrowsIllegalArgumentException() {
        UUID boardID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.findByID(boardID);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).findByID(boardID);
    }

    @Test
    void findByID_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardID = UUID.randomUUID();

        when(taskBoardDAO.findByID(boardID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.findByID(boardID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(1)).findByID(boardID);
    }

    @Test
    void findByTaskBoard_ValidTaskBoardID_ReturnsTaskList() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = new TaskBoard();
        taskBoard.setID(boardID);

        List<Task> mockListOfTasks = createDefaultListOfTasks();

        when(taskBoardDAO.findByID(boardID)).thenReturn(Optional.of(taskBoard));
        when(taskBoardDAO.findRelatedEntitiesByID(boardID)).thenReturn(mockListOfTasks);

        List<TaskResponse> listOfTasks = taskBoardService.findByTaskBoard(boardID);

        assertEquals(2, listOfTasks.size());
        assertEquals(listOfTasks.get(0).getID(), mockListOfTasks.get(0).getID());
        assertEquals(listOfTasks.get(1).getID(), mockListOfTasks.get(1).getID());

        verify(taskBoardDAO, times(1)).findByID(boardID);
        verify(taskBoardDAO, times(1)).findRelatedEntitiesByID(boardID);
    }

    @Test
    void findByTaskBoard_NullTaskBoardID_ThrowsIllegalArgumentException() {
        UUID boardID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.findByTaskBoard(boardID);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).findRelatedEntitiesByID(boardID);
    }

    @Test
    void findByTaskBoard_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardID = UUID.randomUUID();

        when(taskBoardDAO.findByID(boardID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.findByTaskBoard(boardID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(1)).findByID(boardID);
    }

    @Test
    void create_ValidTaskBoard_ReturnsTaskBoardResponse() {
        UUID userID = UUID.randomUUID();

        User user = createDefaultUser(userID);
        TaskBoard taskBoard = createDefaultTaskBoard(null, null);

        when(userService.findByID(userID)).thenReturn(user);
        when(taskBoardDAO.save(ArgumentMatchers.any(TaskBoard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskBoardResponse response = taskBoardService.create(userID, taskBoard);

        assertTaskBoardEquals(taskBoard, response);
        assertEquals(user, response.getUser());

        verify(userService, times(1)).findByID(userID);
        verify(taskBoardDAO, times(1)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void create_EmptyTaskBoardTitle_ThrowsIllegalArgumentException() {
        UUID userID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(null, null);
        taskBoard.setTitle("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.create(userID, taskBoard);
        });

        String expectedMessage = ExceptionMessage.EMPTY.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void create_NullTaskBoardTitle_ThrowsIllegalArgumentException() {
        UUID userID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(null, null);
        taskBoard.setTitle(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.create(userID, taskBoard);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void create_InvalidUserID_ThrowsEntityNotFoundException() {
        UUID userID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(null, null);

        when(userService.findByID(userID)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.create(userID, taskBoard);
        });

        verify(userService, times(1)).findByID(userID);
        verify(taskBoardDAO, times(0)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void create_NullUserID_ThrowsIllegalArgumentException() {
        UUID userID = null;

        TaskBoard taskBoard = createDefaultTaskBoard(null, null);

        when(userService.findByID(userID)).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.create(userID, taskBoard);
        });

        verify(userService, times(1)).findByID(userID);
        verify(taskBoardDAO, times(0)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void update_ValidTaskBoard_ReturnsTaskBoardResponse() {
        UUID boardID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();

        User user = createDefaultUser(userID);
        List<Task> listOfTasks = createDefaultListOfTasks();

        TaskBoard existingTaskBoard = createDefaultTaskBoard(boardID, user);
        existingTaskBoard.setTasks(listOfTasks);

        TaskBoard copyOfExistingTaskBoard = new TaskBoard();
        BeanUtils.copyProperties(existingTaskBoard, copyOfExistingTaskBoard);

        TaskBoard updateData = createDefaultTaskBoard(null, user);
        updateData.setTitle("Board 1");
        updateData.setSorted(true);

        ArgumentCaptor<TaskBoard> captor = ArgumentCaptor.forClass(TaskBoard.class);

        when(taskBoardDAO.findByID(boardID)).thenReturn(Optional.of(copyOfExistingTaskBoard));
        when(taskBoardDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskBoardResponse response = taskBoardService.update(boardID, updateData);
        TaskBoard capturedBoard = captor.getValue();

        assertEquals(existingTaskBoard.getID(), capturedBoard.getID());
        assertNotEquals(existingTaskBoard.getTitle(), capturedBoard.getTitle());
        assertNotEquals(existingTaskBoard.isSorted(), capturedBoard.isSorted());
        assertEquals(existingTaskBoard.getTasks(), capturedBoard.getTasks());
        assertEquals(existingTaskBoard.getUser(), capturedBoard.getUser());

        assertNotNull(response);
        assertEquals(updateData.getTitle(), response.getTitle());
        assertEquals(updateData.isSorted(), response.isSorted());

        verify(taskBoardDAO, times(1)).findByID(boardID);
        verify(taskBoardDAO, times(1)).update(captor.capture());
    }

    @Test
    void update_NullTaskBoardID_ThrowsIllegalArgumentException() {
        UUID boardID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.update(boardID, new TaskBoard());
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).update(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void update_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardID = UUID.randomUUID();

        when(taskBoardDAO.findByID(boardID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.update(boardID, new TaskBoard());
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(1)).findByID(boardID);
        verify(taskBoardDAO, times(0)).update(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void update_EmptyTaskBoardTitle_ReturnsTaskBoardResponseWithSameTitle() {
        UUID boardID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();

        User user = createDefaultUser(userID);
        List<Task> listOfTasks = createDefaultListOfTasks();

        TaskBoard existingTaskBoard = createDefaultTaskBoard(boardID, user);
        existingTaskBoard.setTasks(listOfTasks);

        TaskBoard copyOfExistingTaskBoard = new TaskBoard();
        BeanUtils.copyProperties(existingTaskBoard, copyOfExistingTaskBoard);

        TaskBoard updateData = createDefaultTaskBoard(null, user);
        updateData.setTitle("");

        ArgumentCaptor<TaskBoard> captor = ArgumentCaptor.forClass(TaskBoard.class);

        when(taskBoardDAO.findByID(boardID)).thenReturn(Optional.of(copyOfExistingTaskBoard));
        when(taskBoardDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskBoardResponse response = taskBoardService.update(boardID, updateData);
        TaskBoard capturedTaskBoard = captor.getValue();

        assertTaskBoardEquals(existingTaskBoard, capturedTaskBoard);

        assertNotNull(response);
        assertNotEquals(updateData.getTitle(), response.getTitle());

        verify(taskBoardDAO, times(1)).findByID(boardID);
        verify(taskBoardDAO, times(1)).update(captor.capture());
    }

    @Test
    void update_NullTaskBoardTitle_ReturnsTaskBoardResponseWithSameTitle() {
        UUID boardID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();

        User user = createDefaultUser(userID);
        List<Task> listOfTasks = createDefaultListOfTasks();

        TaskBoard existingTaskBoard = createDefaultTaskBoard(boardID, user);
        existingTaskBoard.setID(boardID);
        existingTaskBoard.setTasks(listOfTasks);

        TaskBoard copyOfExistingTaskBoard = new TaskBoard();
        BeanUtils.copyProperties(existingTaskBoard, copyOfExistingTaskBoard);

        TaskBoard updateData = createDefaultTaskBoard(null, user);
        updateData.setTitle(null);

        ArgumentCaptor<TaskBoard> captor = ArgumentCaptor.forClass(TaskBoard.class);

        when(taskBoardDAO.findByID(boardID)).thenReturn(Optional.of(copyOfExistingTaskBoard));
        when(taskBoardDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskBoardResponse response = taskBoardService.update(boardID, updateData);
        TaskBoard capturedTaskBoard = captor.getValue();

        assertTaskBoardEquals(existingTaskBoard, capturedTaskBoard);

        assertNotNull(response);
        assertNotEquals(updateData.getTitle(), response.getTitle());

        verify(taskBoardDAO, times(1)).findByID(boardID);
        verify(taskBoardDAO, times(1)).update(captor.capture());
    }

    @Test
    void delete_ValidTaskBoardID_TaskBoardDeletedSuccessfully() {
        UUID boardID = UUID.randomUUID();

        when(taskBoardDAO.delete(boardID)).thenReturn(1);

        taskBoardService.delete(boardID);

        verify(taskBoardDAO, times(1)).delete(boardID);
    }

    @Test
    void delete_NullTaskBoardID_ThrowsIllegalArgumentException() {
        UUID boardID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.delete(boardID);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(0)).delete(boardID);
    }

    @Test
    void delete_NoTaskBoardsDeleted_ThrowsEntityNotFoundException() {
        UUID boardID = UUID.randomUUID();

        when(taskBoardDAO.delete(boardID)).thenReturn(0);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.delete(boardID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardDAO, times(1)).delete(boardID);
    }
}
