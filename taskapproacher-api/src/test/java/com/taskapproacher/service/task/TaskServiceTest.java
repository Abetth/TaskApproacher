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

    private TaskBoard createDefaultTaskBoard(UUID boardId) {
        TaskBoard board = new TaskBoard();
        board.setId(boardId);
        board.setTitle("Task board");
        board.setSorted(false);

        return board;
    }

    private TaskBoard createDefaultTaskBoard(UUID boardId, String title) {
        TaskBoard board = new TaskBoard();
        board.setId(boardId);
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

    private Task createDefaultTask(UUID taskId, TaskBoard board) {
        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Default task");
        task.setDescription("Default task description");
        task.setPriority(Priority.STANDARD);
        task.setDeadline(LocalDate.now());
        task.setFinished(false);
        task.setTaskBoard(board);

        return task;
    }

    @Test
    void findById_ValidTaskID_ReturnsTask() {
        UUID taskId = UUID.randomUUID();

        Task mockTask = new Task();
        mockTask.setId(taskId);

        when(taskDAO.findById(taskId)).thenReturn(Optional.of(mockTask));

        Task task = taskService.findById(taskId);

        assertEquals(mockTask.getId(), task.getId());

        verify(taskDAO, times(1)).findById(taskId);
    }

    @Test
    void findById_InvalidTaskID_ThrowsEntityNotFoundException() {
        UUID taskId = UUID.randomUUID();

        when(taskDAO.findById(taskId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskService.findById(taskId);
        });

        String expectedMessage = ErrorMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskDAO, times(1)).findById(taskId);
    }

    @Test
    void findById_NullTaskID_ThrowsIllegalArgumentException() {
        UUID taskId = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.findById(taskId);
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskDAO, times(0)).findById(taskId);
    }

    @Test
    void create_ValidTask_ReturnsTaskResponse() {
        UUID boardId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardId);

        TaskRequest request = createDefaultTaskRequest(null);

        when(taskDAO.save(ArgumentMatchers.any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskBoardService.findById(boardId)).thenReturn(taskBoard);

        TaskResponse response = taskService.create(boardId, request, DEFAULT_TIME_ZONE);

        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.getDescription(), response.getDescription());
        assertEquals(request.getPriority(), response.getPriority());
        assertEquals(request.getDeadline(), response.getDeadline());
        assertEquals(request.isFinished(), response.isFinished());
        assertEquals(taskBoard, response.getTaskBoard());

        verify(taskDAO, times(1)).save(ArgumentMatchers.any(Task.class));
        verify(taskBoardService, times(1)).findById(boardId);
    }


    @Test
    void create_InvalidTaskBoardId_ThrowsEntityNotFoundException() {
        UUID boardId = UUID.randomUUID();

        TaskRequest request = createDefaultTaskRequest(null);

        when(taskBoardService.findById(boardId)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> {
            taskService.create(boardId, request, DEFAULT_TIME_ZONE);
        });

        verify(taskBoardService, times(1)).findById(boardId);
        verify(taskDAO, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void create_EmptyTaskTitle_ThrowsIllegalArgumentException() {
        UUID boardId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardId);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setTitle("");

        when(taskBoardService.findById(boardId)).thenReturn(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.create(boardId, request, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ErrorMessage.EMPTY.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardService, times(1)).findById(boardId);
        verify(taskDAO, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void create_NullTaskTitle_ThrowsIllegalArgumentException() {
        UUID boardId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardId);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setTitle(null);

        when(taskBoardService.findById(boardId)).thenReturn(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.create(boardId, request, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardService, times(1)).findById(boardId);
        verify(taskDAO, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void create_EmptyTaskPriority_ReturnsTaskResponseWithStandardPriority() {
        UUID boardId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardId);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setPriority("");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskBoardService.findById(boardId)).thenReturn(taskBoard);
        when(taskDAO.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.create(boardId, request, DEFAULT_TIME_ZONE);
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

        verify(taskBoardService, times(1)).findById(boardId);
        verify(taskDAO, times(1)).save(captor.capture());
    }

    @Test
    void create_NullTaskPriority_ReturnsTaskResponseWithStandardPriority() {
        UUID boardId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardId);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setPriority(null);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskBoardService.findById(boardId)).thenReturn(taskBoard);
        when(taskDAO.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.create(boardId, request, DEFAULT_TIME_ZONE);
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

        verify(taskBoardService, times(1)).findById(boardId);
        verify(taskDAO, times(1)).save(captor.capture());
    }

    @Test
    void create_NullTaskDeadline_ThrowsIllegalArgumentException() {
        UUID boardId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardId);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setDeadline(null);

        when(taskBoardService.findById(boardId)).thenReturn(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.create(boardId, request, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardService, times(1)).findById(boardId);
        verify(taskDAO, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void create_ValidTaskDeadlineUTCMinusTen_ReturnsTaskResponse() {
        UUID boardId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardId);

        LocalDate updatedDateTime = ZonedDateTime.now().minusHours(10).toLocalDate();

        TaskRequest request = createDefaultTaskRequest(null);
        request.setDeadline(updatedDateTime);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskBoardService.findById(boardId)).thenReturn(taskBoard);
        when(taskDAO.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        String testTimeZone = "Pacific/Tahiti";

        TaskResponse response = taskService.create(boardId, request, testTimeZone);
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

        verify(taskBoardService, times(1)).findById(boardId);
        verify(taskDAO, times(1)).save(captor.capture());
    }

    @Test
    void create_InvalidTaskDeadlineMinusOneDay_ThrowsIllegalArgumentException() {
        UUID boardId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardId);

        TaskRequest request = createDefaultTaskRequest(null);
        request.setDeadline(LocalDate.now().minusDays(1));

        when(taskBoardService.findById(boardId)).thenReturn(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.create(boardId, request, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ErrorMessage.BEFORE_CURRENT_DATE.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardService, times(1)).findById(boardId);
        verify(taskDAO, times(0)).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    void update_ValidTask_ReturnsTaskResponse() {
        UUID taskId = UUID.randomUUID();
        UUID firstBoardId = UUID.randomUUID();
        UUID secondBoardId = UUID.randomUUID();

        TaskBoard firstTaskBoard = createDefaultTaskBoard(firstBoardId, "First task board");
        TaskBoard secondTaskBoard = createDefaultTaskBoard(secondBoardId, "Second task board");

        Task existingTask = createDefaultTask(taskId, firstTaskBoard);

        Task copyOfExistingTask = new Task();
        BeanUtils.copyProperties(existingTask, copyOfExistingTask);

        TaskRequest updateData = createDefaultTaskRequest(secondTaskBoard);
        updateData.setFinished(true);
        updateData.setDeadline(LocalDate.now().plusDays(10));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskDAO.findById(taskId)).thenReturn(Optional.of(copyOfExistingTask));
        when(taskDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.update(taskId, updateData, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertEquals(existingTask.getId(), capturedTask.getId());
        assertNotEquals(existingTask.getTitle(), capturedTask.getTitle());
        assertNotEquals(existingTask.getDescription(), capturedTask.getDescription());
        assertNotEquals(existingTask.getPriority(), capturedTask.getPriority());
        assertNotEquals(existingTask.getDeadline(), capturedTask.getDeadline());
        assertNotEquals(existingTask.isFinished(), capturedTask.isFinished());
        assertNotEquals(existingTask.getTaskBoard(), capturedTask.getTaskBoard());

        assertNotNull(response);
        assertEquals(taskId, response.getId());
        assertEquals(updateData.getTitle(), response.getTitle());
        assertEquals(updateData.getPriority(), response.getPriority());
        assertEquals(updateData.getDeadline(), response.getDeadline());
        assertEquals(updateData.isFinished(), response.isFinished());
        assertEquals(updateData.getTaskBoard(), response.getTaskBoard());

        verify(taskDAO, times(1)).findById(taskId);
        verify(taskDAO, times(1)).update(captor.capture());
    }

    @Test
    void update_TaskFieldsAreEmptyExceptForFinishedAndTaskBoard_ReturnsTaskResponseTaskDataChanged() {
        UUID taskId = UUID.randomUUID();
        UUID firstBoardId = UUID.randomUUID();
        UUID secondBoardId = UUID.randomUUID();

        TaskBoard firstTaskBoard = createDefaultTaskBoard(firstBoardId, "First task board");
        TaskBoard secondTaskBoard = createDefaultTaskBoard(secondBoardId, "Second task board");

        Task existingTask = createDefaultTask(taskId, firstTaskBoard);

        Task copyOfExistingTask = new Task();
        BeanUtils.copyProperties(existingTask, copyOfExistingTask);

        TaskRequest updateData = createDefaultTaskRequest(secondTaskBoard);
        updateData.setTitle("");
        updateData.setDescription("");
        updateData.setPriority("");
        updateData.setFinished(true);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskDAO.findById(taskId)).thenReturn(Optional.of(copyOfExistingTask));
        when(taskDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.update(taskId, updateData, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertEquals(existingTask.getId(), capturedTask.getId());
        assertEquals(existingTask.getTitle(), capturedTask.getTitle());
        assertEquals(existingTask.getDescription(), capturedTask.getDescription());
        assertEquals(existingTask.getPriority(), capturedTask.getPriority());
        assertEquals(existingTask.getDeadline(), capturedTask.getDeadline());
        assertNotEquals(existingTask.isFinished(), capturedTask.isFinished());
        assertNotEquals(existingTask.getTaskBoard(), capturedTask.getTaskBoard());

        assertNotNull(response);
        assertEquals(taskId, response.getId());
        assertNotEquals(updateData.getTitle(), response.getTitle());
        assertEquals(updateData.getTaskBoard(), response.getTaskBoard());

        verify(taskDAO, times(1)).findById(taskId);
        verify(taskDAO, times(1)).update(captor.capture());
    }

    @Test
    void update_TaskFieldsAreNullFinishedIsFalse_ReturnsTaskResponseTaskDataDidNotChanged() {
        UUID boardId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardId);

        Task existingTask = createDefaultTask(taskId, taskBoard);

        Task copyOfExistingTask = new Task();
        BeanUtils.copyProperties(existingTask, copyOfExistingTask);

        TaskRequest updateData = new TaskRequest();

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskDAO.findById(taskId)).thenReturn(Optional.of(copyOfExistingTask));
        when(taskDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.update(taskId, updateData, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertEquals(existingTask.getId(), capturedTask.getId());
        assertEquals(existingTask.getTitle(), capturedTask.getTitle());
        assertEquals(existingTask.getDescription(), capturedTask.getDescription());
        assertEquals(existingTask.getPriority(), capturedTask.getPriority());
        assertEquals(existingTask.getDeadline(), capturedTask.getDeadline());
        assertEquals(existingTask.isFinished(), capturedTask.isFinished());
        assertEquals(existingTask.getTaskBoard(), capturedTask.getTaskBoard());

        assertNotNull(response);
        assertNotEquals(updateData.getId(), response.getId());
        assertNotEquals(updateData.getTitle(), response.getTitle());

        verify(taskDAO, times(1)).findById(taskId);
        verify(taskDAO, times(1)).update(captor.capture());
    }

    @Test
    void update_InvalidTaskDeadline_ThrowsIllegalArgumentException() {
        UUID taskId = UUID.randomUUID();
        UUID boardId = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardId);

        Task existingTask = createDefaultTask(taskId, taskBoard);

        TaskRequest updateData = createDefaultTaskRequest(taskBoard);
        updateData.setDeadline(LocalDate.now().minusDays(1));

        when(taskDAO.findById(taskId)).thenReturn(Optional.of(existingTask));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.update(taskId, updateData, DEFAULT_TIME_ZONE);
        });

        String expectedMessage = ErrorMessage.BEFORE_CURRENT_DATE.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskDAO, times(1)).findById(taskId);
        verify(taskDAO, times(0)).update(ArgumentMatchers.any(Task.class));
    }

    @Test
    void delete_ValidTaskId_TaskDeletedSuccessfully() {
        UUID taskId = UUID.randomUUID();

        doNothing().when(taskDAO).delete(taskId);

        taskService.delete(taskId);

        verify(taskDAO, times(1)).delete(taskId);
    }

    @Test
    void delete_NullTaskId_ThrowsIllegalArgumentException() {
        UUID taskId = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.delete(taskId);
        });

        String expectedMessage = ErrorMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskDAO, times(0)).delete(taskId);
    }
}
