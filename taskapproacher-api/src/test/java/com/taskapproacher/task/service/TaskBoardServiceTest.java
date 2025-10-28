package com.taskapproacher.task.service;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.interfaces.attributes.TaskBoardAttributes;
import com.taskapproacher.task.constant.Priority;
import com.taskapproacher.task.mapper.TaskBoardMapper;
import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.model.TaskBoardDTO;
import com.taskapproacher.task.model.TaskDTO;
import com.taskapproacher.task.repository.TaskBoardRepository;
import com.taskapproacher.user.constant.Role;
import com.taskapproacher.user.model.User;
import com.taskapproacher.user.service.UserService;

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
    private TaskBoardRepository taskBoardRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private TaskBoardService taskBoardService;

    private final TaskBoardMapper taskBoardMapper = new TaskBoardMapper();

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

    private List<Task> createDefaultListOfTasks(TaskBoard taskBoard) {
        Task firstTask = createDefaultTask(UUID.randomUUID());
        firstTask.setID(UUID.randomUUID());
        firstTask.setTitle("First task");
        firstTask.setTaskBoard(taskBoard);
        Task secondTask = createDefaultTask(UUID.randomUUID());
        secondTask.setID(UUID.randomUUID());
        secondTask.setTitle("Second task");
        secondTask.setTaskBoard(taskBoard);

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

    private TaskBoardDTO createDefaultTaskBoardDTO() {
        return new TaskBoardDTO(null, "Test Task Board", false, null, null);
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

    /*
    if (expected.get*() != null) is used because in some cases, the normal behavior of these values is to be null for
    expected, for example, when creating, the ID is null, and when updating, userID may not be passed at all, it doesn't
    update anyway.
     */
    private void assertTaskBoardEquals(TaskBoardAttributes expected, TaskBoardAttributes actual) {
        assertAll(() -> {
            if (expected.getID() != null) {
                assertEquals(expected.getID(), actual.getID());
            }
            assertEquals(expected.getTitle(), actual.getTitle());
            assertEquals(expected.isSorted(), actual.isSorted());
            assertEquals(expected.getTasks(), actual.getTasks());
            if (expected.getUserID() != null) {
                assertEquals(expected.getUserID(), actual.getUserID());
            }
        });
    }

    @Test
    void findByID_ValidTaskBoardID_ReturnsTaskBoard() {
        UUID boardID = UUID.randomUUID();

        TaskBoard mockBoard = createDefaultTaskBoard(boardID, new User());

        when(taskBoardRepository.findByID(boardID)).thenReturn(Optional.of(mockBoard));

        TaskBoard taskBoard = taskBoardService.findByID(boardID);

        assertTaskBoardEquals(mockBoard, taskBoard);

        verify(taskBoardRepository, times(1)).findByID(boardID);
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

        verify(taskBoardRepository, times(0)).findByID(boardID);
    }

    @Test
    void findByID_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardID = UUID.randomUUID();

        when(taskBoardRepository.findByID(boardID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.findByID(boardID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardRepository, times(1)).findByID(boardID);
    }

    @Test
    void findByTaskBoard_ValidTaskBoardID_ReturnsTaskDTOList() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = new TaskBoard();
        taskBoard.setID(boardID);

        List<Task> mockListOfTasks = createDefaultListOfTasks(taskBoard);

        when(taskBoardRepository.findByID(boardID)).thenReturn(Optional.of(taskBoard));
        when(taskBoardRepository.findRelatedEntitiesByID(boardID)).thenReturn(mockListOfTasks);

        List<TaskDTO> listOfTasks = taskBoardService.findByTaskBoard(boardID);

        assertEquals(2, listOfTasks.size());
        assertEquals(listOfTasks.get(0).getID(), mockListOfTasks.get(0).getID());
        assertEquals(listOfTasks.get(1).getID(), mockListOfTasks.get(1).getID());

        verify(taskBoardRepository, times(1)).findByID(boardID);
        verify(taskBoardRepository, times(1)).findRelatedEntitiesByID(boardID);
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

        verify(taskBoardRepository, times(0)).findRelatedEntitiesByID(boardID);
    }

    @Test
    void findByTaskBoard_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardID = UUID.randomUUID();

        when(taskBoardRepository.findByID(boardID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.findByTaskBoard(boardID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardRepository, times(1)).findByID(boardID);
    }

    @Test
    void createTaskBoard_ValidTaskBoard_ReturnsTaskBoardDTO() {
        UUID userID = UUID.randomUUID();

        User user = createDefaultUser(userID);

        TaskBoardDTO request = createDefaultTaskBoardDTO();

        when(userService.findByID(userID)).thenReturn(user);
        when(taskBoardRepository.save(ArgumentMatchers.any(TaskBoard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskBoardDTO response = taskBoardService.createTaskBoard(userID, request);

        assertTaskBoardEquals(request, response);
        assertEquals(user.getID(), response.getUserID());

        verify(userService, times(1)).findByID(userID);
        verify(taskBoardRepository, times(1)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void createTaskBoard_EmptyTaskBoardTitle_ThrowsIllegalArgumentException() {
        UUID userID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(null, new User());
        taskBoard.setTitle("");

        TaskBoardDTO request = taskBoardMapper.mapToTaskBoardDTO(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.createTaskBoard(userID, request);
        });

        String expectedMessage = ExceptionMessage.EMPTY.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardRepository, times(0)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void createTaskBoard_NullTaskBoardTitle_ThrowsIllegalArgumentException() {
        UUID userID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(null, new User());
        taskBoard.setTitle(null);

        TaskBoardDTO request = taskBoardMapper.mapToTaskBoardDTO(taskBoard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.createTaskBoard(userID, request);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardRepository, times(0)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void createTaskBoard_NullUserID_ThrowsIllegalArgumentException() {
        UUID userID = null;

        TaskBoardDTO request = createDefaultTaskBoardDTO();

        when(userService.findByID(userID)).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.createTaskBoard(userID, request);
        });

        verify(userService, times(1)).findByID(userID);
        verify(taskBoardRepository, times(0)).save(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void updateTaskBoard_ValidTaskBoard_ReturnsTaskBoardDTODataChanged() {
        UUID boardID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();

        User user = createDefaultUser(userID);

        TaskBoard existingTaskBoard = createDefaultTaskBoard(boardID, user);
        List<Task> listOfTasks = createDefaultListOfTasks(existingTaskBoard);
        existingTaskBoard.setTasks(listOfTasks);

        TaskBoard copyOfExistingTaskBoard = new TaskBoard();
        BeanUtils.copyProperties(existingTaskBoard, copyOfExistingTaskBoard);

        TaskBoard updateData = new TaskBoard();
        updateData.setTitle("Board 1");
        updateData.setSorted(true);
        updateData.setUser(new User());

        TaskBoardDTO request = taskBoardMapper.mapToTaskBoardDTO(updateData);

        ArgumentCaptor<TaskBoard> captor = ArgumentCaptor.forClass(TaskBoard.class);

        when(taskBoardRepository.findByID(boardID)).thenReturn(Optional.of(copyOfExistingTaskBoard));
        when(taskBoardRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskBoardDTO response = taskBoardService.updateTaskBoard(boardID, request);
        TaskBoard capturedBoard = captor.getValue();

        assertEquals(existingTaskBoard.getID(), capturedBoard.getID());
        assertNotEquals(existingTaskBoard.getTitle(), capturedBoard.getTitle());
        assertNotEquals(existingTaskBoard.isSorted(), capturedBoard.isSorted());
        assertEquals(existingTaskBoard.getTasks(), capturedBoard.getTasks());
        assertEquals(existingTaskBoard.getUser(), capturedBoard.getUser());

        assertNotNull(response);
        assertEquals(updateData.getTitle(), response.getTitle());
        assertEquals(updateData.isSorted(), response.isSorted());

        verify(taskBoardRepository, times(1)).findByID(boardID);
        verify(taskBoardRepository, times(1)).update(captor.capture());
    }

    @Test
    void updateTaskBoard_NullTaskBoardID_ThrowsIllegalArgumentException() {
        UUID boardID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.updateTaskBoard(boardID, new TaskBoardDTO(null, null, false, null, null));
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardRepository, times(0)).update(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void updateTaskBoard_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardID = UUID.randomUUID();

        when(taskBoardRepository.findByID(boardID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.updateTaskBoard(boardID, new TaskBoardDTO(null, null, false, null, null));
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardRepository, times(1)).findByID(boardID);
        verify(taskBoardRepository, times(0)).update(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void updateTaskBoard_EmptyTaskBoardTitle_ReturnsTaskBoardDTOWithSameTitle() {
        UUID boardID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();

        User user = createDefaultUser(userID);

        TaskBoard existingTaskBoard = createDefaultTaskBoard(boardID, user);
        List<Task> listOfTasks = createDefaultListOfTasks(existingTaskBoard);
        existingTaskBoard.setTasks(listOfTasks);

        TaskBoard copyOfExistingTaskBoard = new TaskBoard();
        BeanUtils.copyProperties(existingTaskBoard, copyOfExistingTaskBoard);

        TaskBoard updateData = new TaskBoard();
        updateData.setTitle("");
        updateData.setUser(new User());

        TaskBoardDTO request = taskBoardMapper.mapToTaskBoardDTO(updateData);

        ArgumentCaptor<TaskBoard> captor = ArgumentCaptor.forClass(TaskBoard.class);

        when(taskBoardRepository.findByID(boardID)).thenReturn(Optional.of(copyOfExistingTaskBoard));
        when(taskBoardRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskBoardDTO response = taskBoardService.updateTaskBoard(boardID, request);
        TaskBoard capturedTaskBoard = captor.getValue();

        assertTaskBoardEquals(existingTaskBoard, capturedTaskBoard);

        assertNotNull(response);
        assertNotEquals(updateData.getTitle(), response.getTitle());

        verify(taskBoardRepository, times(1)).findByID(boardID);
        verify(taskBoardRepository, times(1)).update(captor.capture());
    }

    @Test
    void updateTaskBoard_NullTaskBoardTitle_ReturnsTaskBoardDTOWithSameTitle() {
        UUID boardID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();

        User user = createDefaultUser(userID);

        TaskBoard existingTaskBoard = createDefaultTaskBoard(boardID, user);
        List<Task> listOfTasks = createDefaultListOfTasks(existingTaskBoard);
        existingTaskBoard.setTasks(listOfTasks);

        TaskBoard copyOfExistingTaskBoard = new TaskBoard();
        BeanUtils.copyProperties(existingTaskBoard, copyOfExistingTaskBoard);

        TaskBoard updateData = new TaskBoard();
        updateData.setTitle(null);
        updateData.setUser(new User());

        TaskBoardDTO request = taskBoardMapper.mapToTaskBoardDTO(updateData);

        ArgumentCaptor<TaskBoard> captor = ArgumentCaptor.forClass(TaskBoard.class);

        when(taskBoardRepository.findByID(boardID)).thenReturn(Optional.of(copyOfExistingTaskBoard));
        when(taskBoardRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskBoardDTO response = taskBoardService.updateTaskBoard(boardID, request);
        TaskBoard capturedTaskBoard = captor.getValue();

        assertTaskBoardEquals(existingTaskBoard, capturedTaskBoard);

        assertNotNull(response);
        assertNotEquals(updateData.getTitle(), response.getTitle());

        verify(taskBoardRepository, times(1)).findByID(boardID);
        verify(taskBoardRepository, times(1)).update(captor.capture());
    }

    @Test
    void deleteTaskBoard_ValidTaskBoardID_TaskBoardDeletedSuccessfully() {
        UUID boardID = UUID.randomUUID();

        TaskBoard taskBoard = createDefaultTaskBoard(boardID, new User());

        when(taskBoardRepository.findByID(boardID)).thenReturn(Optional.of(taskBoard));
        doNothing().when(taskBoardRepository).delete(ArgumentMatchers.any(TaskBoard.class));

        taskBoardService.deleteTaskBoard(boardID);

        verify(taskBoardRepository, times(1)).findByID(boardID);
        verify(taskBoardRepository, times(1)).delete(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void deleteTaskBoard_NullTaskBoardID_ThrowsIllegalArgumentException() {
        UUID boardID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskBoardService.deleteTaskBoard(boardID);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardRepository, times(0)).findByID(ArgumentMatchers.any(UUID.class));
        verify(taskBoardRepository, times(0)).delete(ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void deleteTaskBoard_InvalidTaskBoardID_ThrowsEntityNotFoundException() {
        UUID boardID = UUID.randomUUID();

        when(taskBoardRepository.findByID(boardID))
                .thenThrow(new EntityNotFoundException(ExceptionMessage.NOT_FOUND.toString()));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            taskBoardService.deleteTaskBoard(boardID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(taskBoardRepository, times(1)).findByID(boardID);
        verify(taskBoardRepository, times(0)).delete(ArgumentMatchers.any(TaskBoard.class));
    }
}
