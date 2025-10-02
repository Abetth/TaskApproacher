package com.taskapproacher.task.service;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.interfaces.matcher.TaskMatcher;
import com.taskapproacher.common.utils.TestApproacherUtils;
import com.taskapproacher.task.constant.Priority;
import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.model.TaskRequest;
import com.taskapproacher.task.model.TaskResponse;
import com.taskapproacher.task.repository.TaskRepository;

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
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//Tests naming convention: method_scenario_result
@ExtendWith(MockitoExtension.class)
public class TaskServiceTest<T> {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskBoardService taskBoardService;
    @InjectMocks
    private TaskService taskService;

    private final String DEFAULT_TIME_ZONE = TimeZone.getDefault().getID();

    private TaskBoard createDefaultTaskBoard(UUID boardID) {
        TaskBoard board = new TaskBoard();
        board.setID(boardID);
        board.setTitle("Task board");
        board.setSorted(false);

        return board;
    }

    private TaskBoard createDefaultTaskBoard(UUID boardID, String title) {
        TaskBoard board = new TaskBoard();
        board.setID(boardID);
        board.setTitle(title);
        board.setSorted(false);

        return board;
    }

    private TaskRequest createDefaultTaskRequest(TaskBoard board) {
        TaskRequest newTask = new TaskRequest();
        newTask.setTitle("Task 1");
        newTask.setDescription("Task description");
        newTask.setPriority("CRITICAL");
        newTask.setDeadline(LocalDate.now());
        newTask.setFinished(false);
        newTask.setTaskBoard(board);

        return newTask;
    }

    private Task createDefaultTask(UUID taskID, TaskBoard board) {
        Task task = new Task();
        task.setID(taskID);
        task.setTitle("Default task");
        task.setDescription("Default task description");
        task.setPriority(Priority.STANDARD);
        task.setDeadline(LocalDate.now());
        task.setFinished(false);
        task.setTaskBoard(board);

        return task;
    }

    private void assertTaskEquals(TaskMatcher expected, TaskMatcher actual) {
        assertAll(() -> {
            TestApproacherUtils.assertEqualsIfNotNull(expected.getID(), actual.getID());
            TestApproacherUtils.assertEqualsIfNotNull(expected.getTitle(), actual.getTitle());
            TestApproacherUtils.assertEqualsIfNotNull(expected.getDescription(), actual.getDescription());
            TestApproacherUtils.assertEqualsIfNotNull(expected.getPriority(), actual.getPriority());
            TestApproacherUtils.assertEqualsIfNotNull(expected.getDeadline(), actual.getDeadline());
            TestApproacherUtils.assertEqualsIfNotNull(expected.isFinished(), actual.isFinished());
            TestApproacherUtils.assertEqualsIfNotNull(expected.getTaskBoard(), actual.getTaskBoard());
        });
    }

    @Test
    void findByID_ValidTaskID_ReturnsTask() {
        UUID taskID = UUID.randomUUID();

        Task mockTask = createDefaultTask(taskID, new TaskBoard());

        when(taskRepository.findByID(taskID)).thenReturn(Optional.of(mockTask));

        Task task = taskService.findByID(taskID);

        assertTaskEquals(mockTask, task);

        verify(taskRepository, times(1)).findByID(taskID);
    }

    @Test
    void findByID_InvalidTaskID_ThrowsEntityNotFoundException() {
        UUID taskID = UUID.randomUUID();

        when(taskRepository.findByID(taskID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskService.findByID(taskID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskRepository, times(1)).findByID(taskID);
    }

    @Test
    void findByID_NullTaskID_ThrowsIllegalArgumentException() {
        UUID taskID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.findByID(taskID);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskRepository, times(0)).findByID(taskID);
    }

    @Test
    void createTask_ValidTask_ReturnsTaskResponse() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);

        when(taskRepository.save(ArgumentMatchers.any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);

        TaskResponse response = taskService.createTask(boardID, request, DEFAULT_TIME_ZONE);

        assertTaskEquals(request, response);
        assertEquals(taskBoard, response.getTaskBoard());

        verify(taskRepository, times(1)).save(ArgumentMatchers.any(Task.class));
        verify(taskBoardService, times(1)).findByID(boardID);
    }


    @Test
    void createTask_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardID = UUID.randomUUID();

        TaskRequest request = createDefaultTaskRequest(null);

        when(taskBoardService.findByID(boardID)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> {
            taskService.createTask(boardID, request, DEFAULT_TIME_ZONE);
        });

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskRepository, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void createTask_EmptyTaskTitle_ThrowsIllegalArgumentException() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setTitle("");

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(boardID, request, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ExceptionMessage.EMPTY.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskRepository, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void createTask_NullTaskTitle_ThrowsIllegalArgumentException() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setTitle(null);

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(boardID, request, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskRepository, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void createTask_EmptyTaskPriority_ReturnsTaskResponseWithStandardPriority() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setPriority("");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);
        when(taskRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.createTask(boardID, request, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertTaskEquals(request, capturedTask);
        assertEquals(Priority.STANDARD, capturedTask.getEnumPriority());
        assertEquals(taskBoard, capturedTask.getTaskBoard());

        assertTaskEquals(request, response);
        assertEquals(taskBoard, response.getTaskBoard());

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskRepository, times(1)).save(captor.capture());
    }

    @Test
    void createTask_NullTaskPriority_ReturnsTaskResponseWithStandardPriority() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setPriority(null);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);
        when(taskRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.createTask(boardID, request, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertTaskEquals(request, capturedTask);
        assertEquals(Priority.STANDARD, capturedTask.getEnumPriority());
        assertEquals(taskBoard, capturedTask.getTaskBoard());

        assertTaskEquals(request, response);
        assertEquals(taskBoard, response.getTaskBoard());

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskRepository, times(1)).save(captor.capture());
    }

    @Test
    void createTask_NullTaskDeadline_ThrowsIllegalArgumentException() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setDeadline(null);

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(boardID, request, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskRepository, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void createTask_ValidTaskDeadlineUTCMinusTen_ReturnsTaskResponse() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        LocalDate updatedDateTime = ZonedDateTime.now().minusHours(10).toLocalDate();

        TaskRequest request = createDefaultTaskRequest(null);
        request.setDeadline(updatedDateTime);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);
        when(taskRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        String testTimeZone = "Pacific/Tahiti";

        TaskResponse response = taskService.createTask(boardID, request, testTimeZone);
        Task capturedTask = captor.getValue();

        assertTaskEquals(request, capturedTask);
        assertEquals(taskBoard, capturedTask.getTaskBoard());

        assertTaskEquals(request, response);
        assertEquals(taskBoard, response.getTaskBoard());

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskRepository, times(1)).save(captor.capture());
    }

    @Test
    void createTask_InvalidTaskDeadlineMinusOneDay_ThrowsIllegalArgumentException() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setDeadline(LocalDate.now().minusDays(1));

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(boardID, request, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ExceptionMessage.BEFORE_CURRENT_DATE.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskRepository, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void updateTask_ValidTask_ReturnsTaskResponse() {
        UUID taskID = UUID.randomUUID();
        UUID firstBoardID = UUID.randomUUID();
        UUID secondBoardID = UUID.randomUUID();

        TaskBoard firstTaskBoard = createDefaultTaskBoard(firstBoardID, "First task board");
        TaskBoard secondTaskBoard = createDefaultTaskBoard(secondBoardID, "Second task board");

        Task existingTask = createDefaultTask(taskID, firstTaskBoard);

        Task copyOfExistingTask = new Task();
        BeanUtils.copyProperties(existingTask, copyOfExistingTask);

        TaskRequest updateData = createDefaultTaskRequest(secondTaskBoard);
        updateData.setFinished(true);
        updateData.setDeadline(LocalDate.now().plusDays(10));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskRepository.findByID(taskID)).thenReturn(Optional.of(copyOfExistingTask));
        when(taskRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTask(taskID, updateData, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertAll(() -> {
            assertEquals(existingTask.getID(), capturedTask.getID());
            assertNotEquals(existingTask.getTitle(), capturedTask.getTitle());
            assertNotEquals(existingTask.getDescription(), capturedTask.getDescription());
            assertNotEquals(existingTask.getEnumPriority(), capturedTask.getEnumPriority());
            assertNotEquals(existingTask.getDeadline(), capturedTask.getDeadline());
            assertNotEquals(existingTask.isFinished(), capturedTask.isFinished());
            assertNotEquals(existingTask.getTaskBoard(), capturedTask.getTaskBoard());
        });

        assertTaskEquals(updateData, response);
        assertEquals(taskID, response.getID());

        verify(taskRepository, times(1)).findByID(taskID);
        verify(taskRepository, times(1)).update(captor.capture());
    }

    @Test
    void updateTask_TaskFieldsAreEmptyFinishedIsFalse_ReturnsTaskResponseTaskDataChanged() {
        UUID taskID = UUID.randomUUID();
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, "First task board");

        Task existingTask = createDefaultTask(taskID, taskBoard);

        Task copyOfExistingTask = new Task();
        BeanUtils.copyProperties(existingTask, copyOfExistingTask);

        TaskRequest updateData = createDefaultTaskRequest(taskBoard);
        updateData.setTitle("");
        updateData.setDescription("");
        updateData.setPriority("");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskRepository.findByID(taskID)).thenReturn(Optional.of(copyOfExistingTask));
        when(taskRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTask(taskID, updateData, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertTaskEquals(existingTask, capturedTask);

        assertEquals(taskID, response.getID());
        assertNotEquals(updateData.getTitle(), response.getTitle());
        assertEquals(updateData.getTaskBoard(), response.getTaskBoard());

        verify(taskRepository, times(1)).findByID(taskID);
        verify(taskRepository, times(1)).update(captor.capture());
    }

    @Test
    void updateTask_TaskFieldsAreNullFinishedIsFalse_ReturnsTaskResponseTaskDataDidNotChanged() {
        UUID boardID = UUID.randomUUID();
        UUID taskID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        Task existingTask = createDefaultTask(taskID, taskBoard);

        Task copyOfExistingTask = new Task();
        BeanUtils.copyProperties(existingTask, copyOfExistingTask);

        TaskRequest updateData = new TaskRequest();

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskRepository.findByID(taskID)).thenReturn(Optional.of(copyOfExistingTask));
        when(taskRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTask(taskID, updateData, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertTaskEquals(existingTask, capturedTask);

        assertNotEquals(updateData.getID(), response.getID());
        assertNotEquals(updateData.getTitle(), response.getTitle());

        verify(taskRepository, times(1)).findByID(taskID);
        verify(taskRepository, times(1)).update(captor.capture());
    }

    @Test
    void updateTask_InvalidTaskDeadline_ThrowsIllegalArgumentException() {
        UUID taskID = UUID.randomUUID();
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        Task existingTask = createDefaultTask(taskID, taskBoard);

        TaskRequest updateData = createDefaultTaskRequest(taskBoard);
        updateData.setDeadline(LocalDate.now().minusDays(1));

        when(taskRepository.findByID(taskID)).thenReturn(Optional.of(existingTask));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(taskID, updateData, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ExceptionMessage.BEFORE_CURRENT_DATE.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskRepository, times(1)).findByID(taskID);
        verify(taskRepository, times(0)).update(ArgumentMatchers.any(Task.class));
    }

    @Test
    void deleteTask_ValidTaskID_TaskDeletedSuccessfully() {
        UUID taskID = UUID.randomUUID();

        Task task = createDefaultTask(taskID, new TaskBoard());

        when(taskRepository.findByID(taskID)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(ArgumentMatchers.any(Task.class));

        taskService.deleteTask(taskID);

        verify(taskRepository, times(1)).findByID(taskID);
        verify(taskRepository, times(1)).delete(ArgumentMatchers.any(Task.class));
    }

    @Test
    void deleteTask_NullTaskID_ThrowsIllegalArgumentException() {
        UUID taskID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.deleteTask(taskID);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskRepository, times(0)).findByID(ArgumentMatchers.any(UUID.class));
        verify(taskRepository, times(0)).delete(ArgumentMatchers.any(Task.class));
    }

    @Test
    void deleteTask_InvalidTaskID_ThrowsEntityNotFoundException() {
        UUID taskID = UUID.randomUUID();

        when(taskRepository.findByID(taskID))
                .thenThrow(new EntityNotFoundException(ExceptionMessage.NOT_FOUND.toString()));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskService.deleteTask(taskID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskRepository, times(1)).findByID(taskID);
        verify(taskRepository, times(0)).delete(ArgumentMatchers.any(Task.class));
    }
}
