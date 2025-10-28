package com.taskapproacher.task.service;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.interfaces.attributes.TaskAttributes;
import com.taskapproacher.task.constant.Priority;
import com.taskapproacher.task.mapper.TaskMapper;
import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.model.TaskDTO;
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
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//Tests naming convention: method_scenario_result
@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskBoardService taskBoardService;
    @InjectMocks
    private TaskService taskService;

    private final TaskMapper taskMapper = new TaskMapper();
    private final String DEFAULT_TIME_ZONE = TimeZone.getDefault().getID();

    private TaskBoard createDefaultTaskBoard(UUID boardID, String title) {
        TaskBoard board = new TaskBoard();
        board.setID(boardID);
        board.setTitle(Objects.requireNonNullElse(title, "Task board"));
        board.setSorted(false);

        return board;
    }

    private TaskDTO createDefaultTaskDTO() {
        return new TaskDTO(null, "Task 1", "Task description", Priority.valueOf("CRITICAL"),
                           LocalDate.now(), false, null);
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

        return new Task(taskID, "Default Task", "Default task description", Priority.STANDARD,
                        LocalDate.now(), false, board);
    }

    /*
    if (expected.get*() != null) is used because in some cases, the normal behavior of these values is to be null for
    expected, for example, when creating, the ID is null, and when updating, taskBoardID may not be passed at all if
    the board does not need to be changed. Same for description (if description is null/empty - value is not
    updated at all)
     */
    private void assertTaskEquals(TaskAttributes expected, TaskAttributes actual) {
        assertAll(() -> {
            if (expected.getID() != null) {
                assertEquals(expected.getID(), actual.getID());
            }

            assertEquals(expected.getTitle(), actual.getTitle());

            if (expected.getDescription() != null && !expected.getDescription().isEmpty()) {
                assertEquals(expected.getDescription(), actual.getDescription());
            }

            assertEquals(expected.getPriority(), actual.getPriority());
            assertEquals(expected.getDeadline(), actual.getDeadline());
            assertEquals(expected.isFinished(), actual.isFinished());

            if (expected.getTaskBoardID() != null) {
                assertEquals(expected.getTaskBoardID(), actual.getTaskBoardID());
            }
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
    void createTask_ValidTask_ReturnsTaskDTO() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, null);

        TaskDTO request = createDefaultTaskDTO();

        when(taskRepository.save(ArgumentMatchers.any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);

        TaskDTO response = taskService.createTask(boardID, request, DEFAULT_TIME_ZONE);

        assertTaskEquals(request, response);
        assertEquals(taskBoard.getID(), response.getTaskBoardID());

        verify(taskRepository, times(1)).save(ArgumentMatchers.any(Task.class));
        verify(taskBoardService, times(1)).findByID(boardID);
    }


    @Test
    void createTask_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardID = UUID.randomUUID();

        TaskDTO request = createDefaultTaskDTO();

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

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, null);

        Task task = createDefaultTask(UUID.randomUUID(), taskBoard);
        task.setTitle("");

        TaskDTO request = taskMapper.mapToTaskDTO(task);

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

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, null);

        Task task = createDefaultTask(UUID.randomUUID(), taskBoard);
        task.setTitle(null);

        TaskDTO request = taskMapper.mapToTaskDTO(task);

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
    void createTask_NullTaskPriority_ThrowsIllegalArgumentException() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, null);

        Task task = createDefaultTask(UUID.randomUUID(), taskBoard);
        task.setPriority(null);

        TaskDTO request = taskMapper.mapToTaskDTO(task);

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
    void createTask_NullTaskDeadline_ThrowsIllegalArgumentException() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, null);

        Task task = createDefaultTask(UUID.randomUUID(), taskBoard);
        task.setDeadline(null);

        TaskDTO request = taskMapper.mapToTaskDTO(task);

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
    void createTask_ValidTaskDeadlineUTCMinusTen_ReturnsTaskDTO() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, null);

        LocalDate updatedDateTime = ZonedDateTime.now().minusHours(10).toLocalDate();

        Task task = createDefaultTask(UUID.randomUUID(), taskBoard);
        task.setDeadline(updatedDateTime);

        TaskDTO request = taskMapper.mapToTaskDTO(task);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);
        when(taskRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        String testTimeZone = "Pacific/Tahiti";

        TaskDTO response = taskService.createTask(boardID, request, testTimeZone);
        Task capturedTask = captor.getValue();

        assertTaskEquals(request, capturedTask);
        assertEquals(taskBoard, capturedTask.getTaskBoard());

        assertTaskEquals(request, response);
        assertEquals(taskBoard.getID(), response.getTaskBoardID());

        verify(taskBoardService, times(1)).findByID(boardID);
        verify(taskRepository, times(1)).save(captor.capture());
    }

    @Test
    void createTask_InvalidTaskDeadlineMinusOneDay_ThrowsIllegalArgumentException() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, null);

        Task task = createDefaultTask(UUID.randomUUID(), taskBoard);
        task.setDeadline(LocalDate.now().minusDays(1));

        TaskDTO request = taskMapper.mapToTaskDTO(task);

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
    void updateTask_ValidTask_ReturnsTaskDTODataChanged() {
        UUID taskID = UUID.randomUUID();
        UUID firstBoardID = UUID.randomUUID();
        UUID secondBoardID = UUID.randomUUID();

        TaskBoard firstTaskBoard = createDefaultTaskBoard(firstBoardID, "First task board");
        TaskBoard secondTaskBoard = createDefaultTaskBoard(secondBoardID, "Second task board");

        Task existingTask = createDefaultTask(taskID, firstTaskBoard);

        Task copyOfExistingTask = new Task();
        BeanUtils.copyProperties(existingTask, copyOfExistingTask);

        Task updateData = new Task();
        updateData.setTitle("Updated Task Title");
        updateData.setDescription("Updated Task Description");
        updateData.setPriority(Priority.CRITICAL);
        updateData.setDeadline(LocalDate.now().plusDays(10));
        updateData.setFinished(true);
        updateData.setTaskBoard(secondTaskBoard);

        TaskDTO request = taskMapper.mapToTaskDTO(updateData);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskRepository.findByID(taskID)).thenReturn(Optional.of(copyOfExistingTask));
        when(taskRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskBoardService.findByID(ArgumentMatchers.any(UUID.class))).thenReturn(secondTaskBoard);

        TaskDTO response = taskService.updateTask(taskID, request, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertAll(() -> {
            assertEquals(existingTask.getID(), capturedTask.getID());
            assertNotEquals(existingTask.getTitle(), capturedTask.getTitle());
            assertNotEquals(existingTask.getDescription(), capturedTask.getDescription());
            assertNotEquals(existingTask.getPriority(), capturedTask.getPriority());
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
    void updateTask_OptionalTaskFieldsAreEmpty_ReturnsTaskDTODataDidNotChanged() {
        UUID taskID = UUID.randomUUID();
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, "First task board");

        Task existingTask = createDefaultTask(taskID, taskBoard);

        Task copyOfExistingTask = new Task();
        BeanUtils.copyProperties(existingTask, copyOfExistingTask);

        Task updateData = new Task();
        updateData.setTitle("");
        updateData.setDescription("");
        updateData.setPriority(Priority.valueOf("STANDARD"));
        updateData.setTaskBoard(taskBoard);

        TaskDTO request = taskMapper.mapToTaskDTO(updateData);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskRepository.findByID(taskID)).thenReturn(Optional.of(copyOfExistingTask));
        when(taskRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskBoardService.findByID(boardID)).thenReturn(taskBoard);

        TaskDTO response = taskService.updateTask(taskID, request, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertTaskEquals(existingTask, capturedTask);

        assertEquals(taskID, response.getID());
        assertNotEquals(updateData.getTitle(), response.getTitle());
        assertEquals(updateData.getTaskBoardID(), response.getTaskBoardID());

        verify(taskRepository, times(1)).findByID(taskID);
        verify(taskRepository, times(1)).update(captor.capture());
    }

    @Test
    void updateTask_OptionalTaskFieldsAreNull_ReturnsTaskDTO() {
        UUID boardID = UUID.randomUUID();
        UUID taskID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, null);

        Task existingTask = createDefaultTask(taskID, taskBoard);

        Task copyOfExistingTask = new Task();
        BeanUtils.copyProperties(existingTask, copyOfExistingTask);

        TaskDTO updateData = new TaskDTO(null, null, null, null,
                                         null, false, null);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        when(taskRepository.findByID(taskID)).thenReturn(Optional.of(copyOfExistingTask));
        when(taskRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskDTO response = taskService.updateTask(taskID, updateData, DEFAULT_TIME_ZONE);
        Task capturedTask = captor.getValue();

        assertTaskEquals(existingTask, capturedTask);

        assertNotEquals(updateData.getTitle(), response.getTitle());
        assertNotEquals(updateData.getDescription(), response.getDescription());

        verify(taskRepository, times(1)).findByID(taskID);
        verify(taskRepository, times(1)).update(captor.capture());
    }

    @Test
    void updateTask_InvalidTaskDeadline_ThrowsIllegalArgumentException() {
        UUID taskID = UUID.randomUUID();
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, null);

        Task existingTask = createDefaultTask(taskID, taskBoard);

        Task updateData = new Task();
        updateData.setDeadline(LocalDate.now().minusDays(1));
        updateData.setTaskBoard(taskBoard);

        TaskDTO request = taskMapper.mapToTaskDTO(updateData);

        when(taskRepository.findByID(taskID)).thenReturn(Optional.of(existingTask));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(taskID, request, DEFAULT_TIME_ZONE);
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
