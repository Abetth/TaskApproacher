package com.taskapproacher.controller.task;

import com.taskapproacher.constant.Priority;
import com.taskapproacher.constant.Role;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.entity.task.response.TaskResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.user.UserResponse;
import com.taskapproacher.interfaces.TaskBoardMatcher;
import com.taskapproacher.service.task.TaskBoardService;
import com.taskapproacher.exception.GlobalExceptionHandler;
import com.taskapproacher.constant.ExceptionMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import org.hamcrest.core.StringContains;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


// Illegal***Data = Empty || Null or any other unsuitable data
//Tests naming convention: method_scenario_result
@ExtendWith(MockitoExtension.class)
public class TaskBoardControllerTest {
    @Mock
    private TaskBoardService taskBoardService;
    @InjectMocks
    private TaskBoardController taskBoardController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String PATH_TO_API = "/api/boards/";

    private TaskBoard createDefaultTaskBoard() {
        User user = createDefaultUser(UUID.randomUUID());
        TaskBoard taskBoard = new TaskBoard();
        taskBoard.setID(UUID.randomUUID());
        taskBoard.setTitle("Test Task Board");
        taskBoard.setSorted(false);
        taskBoard.setTasks(null);
        taskBoard.setUser(user);

        return taskBoard;
    }

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

    private ResultMatcher[] buildFailedMatchers(HttpStatus status, String path, ExceptionMessage exceptionMessage) {
        List<ResultMatcher> matchers = new ArrayList<>();
        int statusCode = status.value();

        matchers.add(status().is(statusCode));
        matchers.add(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        matchers.add(jsonPath("$.type").value("about:blank"));
        matchers.add(jsonPath("$.title").value("Bad Request"));
        matchers.add(jsonPath("$.status").value(statusCode));
        matchers.add(jsonPath("$.detail").value(StringContains.containsString(exceptionMessage.toString())));
        matchers.add(jsonPath("$.instance").value(path));

        return matchers.toArray(new ResultMatcher[0]);
    }

    private ResultMatcher[] buildSuccessfulMatchers(HttpMethod method, HttpStatus status, TaskBoardMatcher taskBoardMatcher) {
        List<ResultMatcher> matchers = new ArrayList<>();
        int statusCode = status.value();

        matchers.add(status().is(statusCode));
        if (method == HttpMethod.GET || method == HttpMethod.DELETE) {
            return matchers.toArray(new ResultMatcher[0]);
        }

        matchers.add(content().contentType(MediaType.APPLICATION_JSON));
        matchers.add(jsonPath("$.id").value(taskBoardMatcher.getID().toString()));
        matchers.add(jsonPath("$.title").value(taskBoardMatcher.getTitle()));
        matchers.add(jsonPath("$.sorted").value(taskBoardMatcher.isSorted()));
        matchers.add(jsonPath("$.tasks").value(taskBoardMatcher.getTasks()));
        matchers.add(jsonPath("$.user.id").value(taskBoardMatcher.getUser().getID().toString()));

        return matchers.toArray(new ResultMatcher[0]);
    }

    private MockHttpServletRequestBuilder buildRequest(HttpMethod method, String path, String objectJson) {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(method, path)
                .contentType(MediaType.APPLICATION_JSON);

        if (method != HttpMethod.GET & method != HttpMethod.DELETE) {
            builder.content(objectJson);
        }

        return builder;
    }

    private void performFailedRequest(HttpMethod method, HttpStatus status, String path, String objectJson, ExceptionMessage exceptionMessage) throws Exception {
        ResultMatcher[] matchers = buildFailedMatchers(status, path, exceptionMessage);
        MockHttpServletRequestBuilder builder = buildRequest(method, path, objectJson);

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    private void performSuccessfulRequest(HttpMethod method, HttpStatus status, String path, String objectJson, TaskBoardMatcher taskBoardMatcher) throws Exception {
        ResultMatcher[] matchers = buildSuccessfulMatchers(method, status, taskBoardMatcher);
        MockHttpServletRequestBuilder builder = buildRequest(method, path, objectJson);

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskBoardController)
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void anyRequest_NullPath_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        TaskBoard request = createDefaultTaskBoard();

        String taskBoardJson = objectMapper.writeValueAsString(request);
        String path = PATH_TO_API + null + "/tasks";

        performFailedRequest(HttpMethod.GET, HttpStatus.BAD_REQUEST, path, taskBoardJson, ExceptionMessage.INVALID_DATA);

        verify(taskBoardService, times(0)).findByID(ArgumentMatchers.any(UUID.class));
    }

    @Test
    void getTasksByBoard_ValidTaskBoardID_ReturnsStatusCodeOkAndListOfTaskResponse() throws Exception {
        UUID taskBoardID = UUID.randomUUID();
        List<TaskResponse> tasks = createDefaultListOfTasks().stream().map(TaskResponse::new).toList();

        String path = PATH_TO_API + taskBoardID + "/tasks";

        when(taskBoardService.findByTaskBoard(ArgumentMatchers.any(UUID.class))).thenReturn(tasks);

        mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.GET, path)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id").value(tasks.get(0).getID().toString()))
                .andExpect(jsonPath("$.[0].title").value(tasks.get(0).getTitle()))
                .andExpect(jsonPath("$.[1].id").value(tasks.get(1).getID().toString()))
                .andExpect(jsonPath("$.[1].title").value(tasks.get(1).getTitle()));
    }

    @Test
    void getTasksByBoard_InvalidTaskBoardID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID taskBoardID = UUID.randomUUID();

        String path = PATH_TO_API + taskBoardID + "/tasks";

        when(taskBoardService.findByTaskBoard(ArgumentMatchers.any(UUID.class)))
                .thenThrow(new EntityNotFoundException(ExceptionMessage.NOT_FOUND.toString()));

        performFailedRequest(HttpMethod.GET, HttpStatus.BAD_REQUEST, path, null, ExceptionMessage.NOT_FOUND);

        verify(taskBoardService, times(1)).findByTaskBoard(ArgumentMatchers.any(UUID.class));
    }

    @Test
    void getTasksByBoard_IllegalTaskBoardID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID taskBoardID = UUID.randomUUID();

        String path = PATH_TO_API + taskBoardID + "/tasks";

        when(taskBoardService.findByTaskBoard(ArgumentMatchers.any(UUID.class)))
                .thenThrow(new IllegalArgumentException(ExceptionMessage.NULL.toString()));

        performFailedRequest(HttpMethod.GET, HttpStatus.BAD_REQUEST, path, null, ExceptionMessage.NULL);

        verify(taskBoardService, times(1)).findByTaskBoard(ArgumentMatchers.any(UUID.class));
    }

    @Test
    void create_ValidTaskBoardData_ReturnsStatusCodeCreatedAndTaskBoardResponse() throws Exception {
        UUID userID = UUID.randomUUID();
        User user = createDefaultUser(userID);

        TaskBoard request = createDefaultTaskBoard();
        TaskBoardResponse response = new TaskBoardResponse(request);
        response.setID(UUID.randomUUID());
        response.setUser(new UserResponse(user));

        String requestJson = objectMapper.writeValueAsString(request);
        String path = PATH_TO_API + userID;

        when(taskBoardService.create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskBoard.class)))
                .thenReturn(response);

        performSuccessfulRequest(HttpMethod.POST, HttpStatus.CREATED, path, requestJson, response);

        verify(taskBoardService, times(1))
                .create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void create_InvalidUserID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID userID = UUID.randomUUID();

        TaskBoard requestBoard = createDefaultTaskBoard();

        String requestJson = objectMapper.writeValueAsString(requestBoard);
        String path = PATH_TO_API + userID;

        when(taskBoardService.create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskBoard.class)))
                .thenThrow(new EntityNotFoundException(ExceptionMessage.NOT_FOUND.toString()));

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, path, requestJson, ExceptionMessage.NOT_FOUND);

        verify(taskBoardService, times(1))
                .create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void create_IllegalTaskBoardData_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID userID = UUID.randomUUID();

        TaskBoard request = createDefaultTaskBoard();

        String requestJson = objectMapper.writeValueAsString(request);
        String path = PATH_TO_API + userID;

        when(taskBoardService.create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskBoard.class)))
                .thenThrow(new IllegalArgumentException(ExceptionMessage.EMPTY.toString()));

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, path, requestJson, ExceptionMessage.EMPTY);

        verify(taskBoardService, times(1))
                .create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void update_ValidTaskBoardData_ReturnsStatusCodeOkAndTaskBoardResponse() throws Exception {
        UUID boardID = UUID.randomUUID();
        UUID userID = UUID.randomUUID();
        User user = createDefaultUser(userID);

        TaskBoard updateData = createDefaultTaskBoard();
        updateData.setTitle("Updated title");
        updateData.setSorted(true);

        TaskBoardResponse response = new TaskBoardResponse(updateData);
        response.setID(boardID);
        response.setUser(new UserResponse(user));

        String updateDataJson = objectMapper.writeValueAsString(updateData);
        String path = PATH_TO_API + boardID;

        when(taskBoardService.update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskBoard.class)))
                .thenReturn(response);

        performSuccessfulRequest(HttpMethod.PATCH, HttpStatus.OK, path, updateDataJson, response);

        verify(taskBoardService, times(1))
                .update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void update_InvalidTaskBoardID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID boardID = UUID.randomUUID();
        TaskBoard updateData = createDefaultTaskBoard();

        String updateDataJson = objectMapper.writeValueAsString(updateData);
        String path = PATH_TO_API + boardID;

        when(taskBoardService.update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskBoard.class)))
                .thenThrow(new EntityNotFoundException(ExceptionMessage.NOT_FOUND.toString()));

        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, path, updateDataJson, ExceptionMessage.NOT_FOUND);

        verify(taskBoardService, times(1)).
                update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void update_IllegalTaskBoardData_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID boardID = UUID.randomUUID();
        TaskBoard updateData = createDefaultTaskBoard();

        String updateDataJson = objectMapper.writeValueAsString(updateData);
        String path = PATH_TO_API + boardID;

        when(taskBoardService.update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskBoard.class)))
                .thenThrow(new IllegalArgumentException(ExceptionMessage.EMPTY.toString()));

        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, path, updateDataJson, ExceptionMessage.EMPTY);

        verify(taskBoardService, times(1))
                .update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskBoard.class));
    }

    @Test
    void delete_ValidTaskBoardID_ReturnsStatusCodeNoContent() throws Exception {
        UUID boardID = UUID.randomUUID();

        String path = PATH_TO_API + boardID;

        doNothing().when(taskBoardService).delete(ArgumentMatchers.any(UUID.class));

        performSuccessfulRequest(HttpMethod.DELETE, HttpStatus.NO_CONTENT, path, null, null);

        verify(taskBoardService, times(1)).delete(ArgumentMatchers.any(UUID.class));
    }

    @Test
    void delete_IllegalTaskID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID boardID = UUID.randomUUID();

        String path = PATH_TO_API + boardID;

        doThrow(new IllegalArgumentException(ExceptionMessage.NULL.toString()))
                .when(taskBoardService).delete(ArgumentMatchers.any(UUID.class));

        performFailedRequest(HttpMethod.DELETE, HttpStatus.BAD_REQUEST, path, null, ExceptionMessage.NULL);
    }
}
