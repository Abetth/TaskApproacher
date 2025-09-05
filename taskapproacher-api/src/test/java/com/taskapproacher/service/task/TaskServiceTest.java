package com.taskapproacher.service.task;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.dao.task.TaskDAO;
import com.taskapproacher.entity.task.response.TaskResponse;
import com.taskapproacher.entity.task.request.TaskRequest;

import com.taskapproacher.enums.ErrorMessage;
import com.taskapproacher.enums.Priority;

import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

//Tests naming convention: method_scenario_result
@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskDAO taskDAO;
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

    @Test
    void findByID_ValidTaskID_ReturnsTask() {
        UUID taskID = UUID.randomUUID();

        Task mockTask = new Task();
        mockTask.setID(taskID);

        when(taskDAO.findByID(taskID)).thenReturn(Optional.of(mockTask));

        Task task = taskService.findByID(taskID);

        assertEquals(mockTask.getID(), task.getID());

        verify(taskDAO, times(1)).findByID(taskID);
    }

    @Test
    void findByID_InvalidTaskID_ThrowsEntityNotFoundException() {
        UUID taskID = UUID.randomUUID();

        when(taskDAO.findByID(taskID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskService.findByID(taskID);
        });

        String expectedMessage = ErrorMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskDAO, times(1)).findByID(taskID);
    }

    @Test
    void findByID_NullTaskID_ThrowsIllegalArgumentException() {
        UUID taskID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.findByID(taskID);
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskDAO, times(0)).findByID(taskID);
    }

    @Test
    void create_ValidTask_ReturnsTaskResponse() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);

        when(taskDAO.save(ArgumentMatchers.any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);

        TaskResponse response = taskService.create(boardID, request, DEFAULT_TIME_ZONE);

        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.getDescription(), response.getDescription());
        assertEquals(request.getPriority(), response.getPriority());
        assertEquals(request.getDeadline(), response.getDeadline());
        assertEquals(request.isFinished(), response.isFinished());
        assertEquals(taskBoard, response.getTaskBoard());

        verify(taskDAO, times(1)).save(ArgumentMatchers.any(Task.class));
        verify(taskBoardService, times(1)).findByID(boardID);
    }


    @Test
    void create_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardID = UUID.randomUUID();

        TaskRequest request = createDefaultTaskRequest(null);

        when(taskBoardService.findByID(boardID)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> {
            taskService.create(boardID, request, DEFAULT_TIME_ZONE);
        });

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskDAO, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void create_EmptyTaskTitle_ThrowsIllegalArgumentException() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setTitle("");

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.create(boardID, request, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ErrorMessage.EMPTY.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskDAO, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void create_NullTaskTitle_ThrowsIllegalArgumentException() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setTitle(null);

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.create(boardID, request, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskDAO, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void create_EmptyTaskPriority_ReturnsTaskResponseWithStandardPriority() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setPriority("");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);
        when(taskDAO.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.create(boardID, request, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertEquals(request.getTitle(), capturedTask.getTitle());
        assertEquals(request.getDescription(), capturedTask.getDescription());
        assertEquals(Priority.STANDARD, capturedTask.getPriority());
        assertEquals(request.getDeadline(), capturedTask.getDeadline());
        assertEquals(request.isFinished(), capturedTask.isFinished());
        assertEquals(taskBoard, capturedTask.getTaskBoard());

        assertNotNull(response);
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(taskBoard, response.getTaskBoard());

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskDAO, times(1)).save(captor.capture());
    }

    @Test
    void create_NullTaskPriority_ReturnsTaskResponseWithStandardPriority() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setPriority(null);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);
        when(taskDAO.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.create(boardID, request, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertEquals(request.getTitle(), capturedTask.getTitle());
        assertEquals(request.getDescription(), capturedTask.getDescription());
        assertEquals(Priority.STANDARD, capturedTask.getPriority());
        assertEquals(request.getDeadline(), capturedTask.getDeadline());
        assertEquals(request.isFinished(), capturedTask.isFinished());
        assertEquals(taskBoard, capturedTask.getTaskBoard());

        assertNotNull(response);
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(taskBoard, response.getTaskBoard());

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskDAO, times(1)).save(captor.capture());
    }

    @Test
    void create_NullTaskDeadline_ThrowsIllegalArgumentException() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setDeadline(null);

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.create(boardID, request, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskDAO, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void create_ValidTaskDeadlineUTCMinusTen_ReturnsTaskResponse() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        LocalDate updatedDateTime = ZonedDateTime.now().minusHours(10).toLocalDate();

        TaskRequest request = createDefaultTaskRequest(null);
        request.setDeadline(updatedDateTime);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);
        when(taskDAO.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        String testTimeZone = "Pacific/Tahiti";

        TaskResponse response = taskService.create(boardID, request, testTimeZone);
        Task capturedTask = captor.getValue();

        assertEquals(request.getTitle(), capturedTask.getTitle());
        assertEquals(request.getDescription(), capturedTask.getDescription());
        assertEquals(request.getPriority(), capturedTask.getPriority().toString());
        assertEquals(updatedDateTime, capturedTask.getDeadline());
        assertEquals(request.getDeadline(), capturedTask.getDeadline());
        assertEquals(request.isFinished(), capturedTask.isFinished());
        assertEquals(taskBoard, capturedTask.getTaskBoard());

        assertNotNull(response);
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(taskBoard, response.getTaskBoard());

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskDAO, times(1)).save(captor.capture());
    }

    @Test
    void create_InvalidTaskDeadlineMinusOneDay_ThrowsIllegalArgumentException() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setDeadline(LocalDate.now().minusDays(1));

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.create(boardID, request, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ErrorMessage.BEFORE_CURRENT_DATE.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskDAO, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void update_ValidTask_ReturnsTaskResponse() {
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

        when(taskDAO.findByID(taskID)).thenReturn(Optional.of(copyOfExistingTask));
        when(taskDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.update(taskID, updateData, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertEquals(existingTask.getID(), capturedTask.getID());
        assertNotEquals(existingTask.getTitle(), capturedTask.getTitle());
        assertNotEquals(existingTask.getDescription(), capturedTask.getDescription());
        assertNotEquals(existingTask.getPriority(), capturedTask.getPriority());
        assertNotEquals(existingTask.getDeadline(), capturedTask.getDeadline());
        assertNotEquals(existingTask.isFinished(), capturedTask.isFinished());
        assertNotEquals(existingTask.getTaskBoard(), capturedTask.getTaskBoard());

        assertNotNull(response);
        assertEquals(taskID, response.getID());
        assertEquals(updateData.getTitle(), response.getTitle());
        assertEquals(updateData.getPriority(), response.getPriority());
        assertEquals(updateData.getDeadline(), response.getDeadline());
        assertEquals(updateData.isFinished(), response.isFinished());
        assertEquals(updateData.getTaskBoard(), response.getTaskBoard());

        verify(taskDAO, times(1)).findByID(taskID);
        verify(taskDAO, times(1)).update(captor.capture());
    }

    @Test
    void update_TaskFieldsAreEmptyExceptForFinishedAndTaskBoard_ReturnsTaskResponseTaskDataChanged() {
        UUID taskID = UUID.randomUUID();
        UUID firstBoardID = UUID.randomUUID();
        UUID secondBoardID = UUID.randomUUID();

        TaskBoard firstTaskBoard = createDefaultTaskBoard(firstBoardID, "First task board");
        TaskBoard secondTaskBoard = createDefaultTaskBoard(secondBoardID, "Second task board");

        Task existingTask = createDefaultTask(taskID, firstTaskBoard);

        Task copyOfExistingTask = new Task();
        BeanUtils.copyProperties(existingTask, copyOfExistingTask);

        TaskRequest updateData = createDefaultTaskRequest(secondTaskBoard);
        updateData.setTitle("");
        updateData.setDescription("");
        updateData.setPriority("");
        updateData.setFinished(true);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskDAO.findByID(taskID)).thenReturn(Optional.of(copyOfExistingTask));
        when(taskDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.update(taskID, updateData, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertEquals(existingTask.getID(), capturedTask.getID());
        assertEquals(existingTask.getTitle(), capturedTask.getTitle());
        assertEquals(existingTask.getDescription(), capturedTask.getDescription());
        assertEquals(existingTask.getPriority(), capturedTask.getPriority());
        assertEquals(existingTask.getDeadline(), capturedTask.getDeadline());
        assertNotEquals(existingTask.isFinished(), capturedTask.isFinished());
        assertNotEquals(existingTask.getTaskBoard(), capturedTask.getTaskBoard());

        assertNotNull(response);
        assertEquals(taskID, response.getID());
        assertNotEquals(updateData.getTitle(), response.getTitle());
        assertEquals(updateData.getTaskBoard(), response.getTaskBoard());

        verify(taskDAO, times(1)).findByID(taskID);
        verify(taskDAO, times(1)).update(captor.capture());
    }

    @Test
    void update_TaskFieldsAreNullFinishedIsFalse_ReturnsTaskResponseTaskDataDidNotChanged() {
        UUID boardID = UUID.randomUUID();
        UUID taskID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        Task existingTask = createDefaultTask(taskID, taskBoard);

        Task copyOfExistingTask = new Task();
        BeanUtils.copyProperties(existingTask, copyOfExistingTask);

        TaskRequest updateData = new TaskRequest();

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskDAO.findByID(taskID)).thenReturn(Optional.of(copyOfExistingTask));
        when(taskDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.update(taskID, updateData, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertEquals(existingTask.getID(), capturedTask.getID());
        assertEquals(existingTask.getTitle(), capturedTask.getTitle());
        assertEquals(existingTask.getDescription(), capturedTask.getDescription());
        assertEquals(existingTask.getPriority(), capturedTask.getPriority());
        assertEquals(existingTask.getDeadline(), capturedTask.getDeadline());
        assertEquals(existingTask.isFinished(), capturedTask.isFinished());
        assertEquals(existingTask.getTaskBoard(), capturedTask.getTaskBoard());

        assertNotNull(response);
        assertNotEquals(updateData.getID(), response.getID());
        assertNotEquals(updateData.getTitle(), response.getTitle());

        verify(taskDAO, times(1)).findByID(taskID);
        verify(taskDAO, times(1)).update(captor.capture());
    }

    @Test
    void update_InvalidTaskDeadline_ThrowsIllegalArgumentException() {
        UUID taskID = UUID.randomUUID();
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID);

        Task existingTask = createDefaultTask(taskID, taskBoard);

        TaskRequest updateData = createDefaultTaskRequest(taskBoard);
        updateData.setDeadline(LocalDate.now().minusDays(1));

        when(taskDAO.findByID(taskID)).thenReturn(Optional.of(existingTask));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.update(taskID, updateData, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ErrorMessage.BEFORE_CURRENT_DATE.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskDAO, times(1)).findByID(taskID);
        verify(taskDAO, times(0)).update(ArgumentMatchers.any(Task.class));
    }

    @Test
    void delete_ValidTaskID_TaskDeletedSuccessfully() {
        UUID taskID = UUID.randomUUID();

        doNothing().when(taskDAO).delete(taskID);

        taskService.delete(taskID);

        verify(taskDAO, times(1)).delete(taskID);
    }

    @Test
    void delete_NullTaskID_ThrowsIllegalArgumentException() {
        UUID taskID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.delete(taskID);
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskDAO, times(0)).delete(taskID);
    }
}
