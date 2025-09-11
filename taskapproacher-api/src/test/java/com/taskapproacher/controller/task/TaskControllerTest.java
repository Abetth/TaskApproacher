package com.taskapproacher.controller.task;

import com.taskapproacher.constant.ExceptionMessage;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.request.TaskRequest;
import com.taskapproacher.entity.task.response.TaskResponse;
import com.taskapproacher.interfaces.TaskMatcher;
import com.taskapproacher.service.task.TaskService;
import com.taskapproacher.exception.GlobalExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

//Tests naming convention: method_scenario_result
@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {
    @Mock
    private TaskService taskService;
    @InjectMocks
    private TaskController taskController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final String PATH_TO_API = "/api/tasks/";
    private final String TIME_ZONE = "Europe/London";

    private TaskBoard createDefaultTaskBoard() {
        TaskBoard taskBoard = new TaskBoard();
        taskBoard.setID(UUID.randomUUID());
        taskBoard.setTitle("Board 1");
        taskBoard.setSorted(true);
        taskBoard.setUser(new User());

        return taskBoard;
    }

    private TaskRequest createDefaultTaskRequest() {
        TaskRequest request = new TaskRequest();
        request.setID(null);
        request.setTitle("Task 1");
        request.setDescription("Task description");
        request.setPriority("STANDARD");
        request.setDeadline(LocalDate.now());
        request.setFinished(false);
        request.setTaskBoard(createDefaultTaskBoard());

        return request;
    }

    private MockHttpServletRequestBuilder buildRequest(HttpMethod method, String path, String objectJson) {
        HttpHeaders headers = new HttpHeaders();
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(method, path);

        if (method != HttpMethod.DELETE) {
            headers.add("TimeZone", TIME_ZONE);
            builder.content(objectJson);
        }
        builder.headers(headers);
        builder.contentType(MediaType.APPLICATION_JSON);

        return builder;
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

    private ResultMatcher[] buildSuccessfulMatchers(HttpMethod method, HttpStatus status, TaskMatcher taskMatcher) {
        List<ResultMatcher> matchers = new ArrayList<>();
        int statusCode = status.value();

        matchers.add(status().is(statusCode));

        if (method == HttpMethod.DELETE) {
            return matchers.toArray(new ResultMatcher[0]);
        }

        if (taskMatcher.getID() != null) {
            matchers.add(jsonPath("$.id").value(taskMatcher.getID().toString()));
        }
        matchers.add(jsonPath("$.title").value(taskMatcher.getTitle()));
        matchers.add(jsonPath("$.description").value(taskMatcher.getDescription()));
        matchers.add(jsonPath("$.priority").value(taskMatcher.getPriority()));
        matchers.add(jsonPath("$.deadline[0]", Matchers.is(taskMatcher.getDeadline().getYear())));
        matchers.add(jsonPath("$.deadline[1]", Matchers.is(taskMatcher.getDeadline().getMonthValue())));
        matchers.add(jsonPath("$.deadline[2]", Matchers.is(taskMatcher.getDeadline().getDayOfMonth())));
        matchers.add(jsonPath("$.finished").value(taskMatcher.isFinished()));
        matchers.add(jsonPath("$.taskBoard.id").value(taskMatcher.getTaskBoard().getID().toString()));
        matchers.add(jsonPath("$.taskBoard.title").value(taskMatcher.getTaskBoard().getTitle()));


        return matchers.toArray(new ResultMatcher[0]);
    }

    private void performFailedRequest(HttpMethod method, HttpStatus status, String path, String objectJson, ExceptionMessage exceptionMessage) throws Exception {
        MockHttpServletRequestBuilder builder = buildRequest(method, path, objectJson);
        ResultMatcher[] matchers = buildFailedMatchers(status, path, exceptionMessage);

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    private void performSuccessfulRequest(HttpMethod method, HttpStatus status, String path, String objectJson, TaskMatcher taskMatcher) throws Exception {
        MockHttpServletRequestBuilder builder = buildRequest(method, path, objectJson);
        ResultMatcher[] matchers = buildSuccessfulMatchers(method, status, taskMatcher);

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void anyRequest_NullPath_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        TaskRequest request = createDefaultTaskRequest();
        String taskJson = objectMapper.writeValueAsString(request);
        String path = PATH_TO_API + "board/" + null;

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, path, taskJson, ExceptionMessage.INVALID_DATA);

        verify(taskService, times(0))
                .create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class));
    }

    @Test
    void create_ValidData_ReturnsStatusCodeCreatedWithCreatedTask() throws Exception {
        UUID boardID = UUID.randomUUID();
        UUID createdTaskID = UUID.randomUUID();

        TaskRequest request = createDefaultTaskRequest();
        String taskJson = objectMapper.writeValueAsString(request);

        TaskResponse response = new TaskResponse(new Task(request));
        response.setID(createdTaskID);

        String path = PATH_TO_API + "board/" + boardID;

        when(taskService.create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class)))
                .thenReturn(response);


        performSuccessfulRequest(HttpMethod.POST, HttpStatus.CREATED, path, taskJson, request);

        verify(taskService, times(1))
                .create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class));
    }

    @Test
    void create_InvalidBoardID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID boardID = UUID.randomUUID();

        TaskRequest request = createDefaultTaskRequest();
        String taskJson = objectMapper.writeValueAsString(request);
        String path = PATH_TO_API + "board/" + boardID;

        when(taskService.create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class)))
                .thenThrow(new EntityNotFoundException(ExceptionMessage.NOT_FOUND.toString()));

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, path, taskJson, ExceptionMessage.NOT_FOUND);

        verify(taskService, times(1))
                .create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class));
    }

    @Test
    void create_IllegalTaskData_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID boardID = UUID.randomUUID();

        TaskRequest request = createDefaultTaskRequest();
        String taskJson = objectMapper.writeValueAsString(request);
        String path = PATH_TO_API + "board/" + boardID;

        when(taskService
                .create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class)))
                .thenThrow(new IllegalArgumentException(ExceptionMessage.NULL.toString()));


        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, path, taskJson, ExceptionMessage.NULL);

        verify(taskService, times(1))
                .create(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class));
    }

    @Test
    void update_ValidData_ReturnsStatusCodeOkWithUpdatedTask() throws Exception {
        UUID taskID = UUID.randomUUID();
        TaskRequest request = createDefaultTaskRequest();
        String taskJson = objectMapper.writeValueAsString(request);

        TaskResponse response = new TaskResponse(new Task(request));
        response.setID(taskID);
        response.setTitle("Updated board");
        response.setDescription("Updated description");
        response.setPriority("CRITICAL");
        response.setDeadline(LocalDate.now().plusDays(10));
        response.setFinished(true);

        String path = PATH_TO_API + taskID;

        when(taskService.update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class)))
                .thenReturn(response);


        performSuccessfulRequest(HttpMethod.PATCH, HttpStatus.OK, path, taskJson, response);

        verify(taskService, times(1))
                .update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class));
    }

    @Test
    void update_InvalidTaskID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID taskID = UUID.randomUUID();
        TaskRequest request = createDefaultTaskRequest();
        String taskJson = objectMapper.writeValueAsString(request);
        String path = PATH_TO_API + taskID;

        when(taskService
                .update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class)))
                .thenThrow(new EntityNotFoundException(ExceptionMessage.NOT_FOUND.toString()));


        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, path, taskJson, ExceptionMessage.NOT_FOUND);

        verify(taskService, times(1))
                .update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class));
    }

    @Test
    void update_IllegalTaskData_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID taskID = UUID.randomUUID();
        TaskRequest request = createDefaultTaskRequest();
        String taskJson = objectMapper.writeValueAsString(request);
        String path = PATH_TO_API + taskID;

        when(taskService
                .update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class)))
                .thenThrow(new IllegalArgumentException(ExceptionMessage.NULL.toString()));

        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, path, taskJson, ExceptionMessage.NULL);

        verify(taskService, times(1))
                .update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(TaskRequest.class), ArgumentMatchers.any(String.class));
    }

    @Test
    void delete_ValidTaskID_ReturnsStatusCodeNoContent() throws Exception {
        UUID taskID = UUID.randomUUID();
        String path = PATH_TO_API + taskID;

        doNothing().when(taskService).delete(ArgumentMatchers.any(UUID.class));

        performSuccessfulRequest(HttpMethod.DELETE, HttpStatus.NO_CONTENT, path, null, null);

        verify(taskService, times(1)).delete(ArgumentMatchers.any(UUID.class));
    }

    @Test
    void delete_IllegalTaskID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID taskID = UUID.randomUUID();
        String path = PATH_TO_API + taskID;

        doThrow(new IllegalArgumentException(ExceptionMessage.NULL.toString()))
                .when(taskService)
                .delete(ArgumentMatchers.any(UUID.class));

        performFailedRequest(HttpMethod.DELETE, HttpStatus.BAD_REQUEST, path, null, ExceptionMessage.NULL);

        verify(taskService, times(1)).delete(ArgumentMatchers.any(UUID.class));
    }
}