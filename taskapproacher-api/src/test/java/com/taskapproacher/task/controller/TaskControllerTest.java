package com.taskapproacher.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.taskapproacher.auth.model.AuthRequest;
import com.taskapproacher.auth.model.AuthResponse;
import com.taskapproacher.common.constant.EntityNumber;
import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.interfaces.attributes.TaskAttributes;
import com.taskapproacher.common.utils.TestApproacherDataUtils;
import com.taskapproacher.task.constant.Priority;
import com.taskapproacher.task.constant.TaskConstants;
import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.user.model.User;

import org.hamcrest.core.StringContains;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//Tests naming convention: method_scenario_result
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private final String PATH_TO_API = "/api/task/";
    private final String TIME_ZONE = "Europe/London";
    private String token;

    private Task createDefaultTask() {
        return new Task(null,"Task 1", "Task description",
                           Priority.valueOf("STANDARD"), LocalDate.now(), false, new TaskBoard());
    }

    private ResultMatcher[] buildFailedMatchers(HttpStatus status, String path, ExceptionMessage exceptionMessage) {
        List<ResultMatcher> matchers = new ArrayList<>();
        int statusCode = status.value();

        matchers.add(status().is(statusCode));
        matchers.add(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        matchers.add(jsonPath("$.type").value("about:blank"));
        matchers.add(jsonPath("$.title").value(status.getReasonPhrase()));
        matchers.add(jsonPath("$.status").value(statusCode));
        matchers.add(jsonPath("$.detail").value(StringContains.containsString(exceptionMessage.toString())));
        matchers.add(jsonPath("$.instance").value(path));

        return matchers.toArray(new ResultMatcher[0]);
    }

    private ResultMatcher[] buildSuccessfulMatchers(HttpMethod method, HttpStatus status, TaskAttributes taskAttributes) {
        List<ResultMatcher> matchers = new ArrayList<>();
        int statusCode = status.value();

        matchers.add(status().is(statusCode));

        if (method == HttpMethod.DELETE) {
            return matchers.toArray(new ResultMatcher[0]);
        }

        matchers.add(content().contentType(MediaType.APPLICATION_JSON));
        if (method != HttpMethod.POST) {
            matchers.add(jsonPath("$.id").value(taskAttributes.getID().toString()));
            matchers.add(jsonPath("$.taskBoardID").value(taskAttributes.getTaskBoardID().toString()));
        } else {
            matchers.add(jsonPath("$.id").isNotEmpty());
            matchers.add(jsonPath("$.taskBoardID").isNotEmpty());
        }
        matchers.add(jsonPath("$.title").value(taskAttributes.getTitle()));
        matchers.add(jsonPath("$.description").value(taskAttributes.getDescription()));
        matchers.add(jsonPath("$.priority").value(taskAttributes.getPriority().toString()));
        matchers.add(jsonPath("$.deadline").value(taskAttributes.getDeadline().toString()));
        matchers.add(jsonPath("$.finished").value(taskAttributes.isFinished()));


        return matchers.toArray(new ResultMatcher[0]);
    }

    private MockHttpServletRequestBuilder buildRequest(HttpMethod method, String token,
                                                       String path, String objectJson) {
        HttpHeaders headers = new HttpHeaders();
        MockHttpServletRequestBuilder builder = request(method, path)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token);

        if (method != HttpMethod.DELETE) {
            headers.add("TimeZone", TIME_ZONE);
            builder.content(objectJson);
        }
        builder.headers(headers);

        return builder;
    }

    private void performFailedRequest(HttpMethod method, HttpStatus status, String token, String path,
                                      String objectJson, ExceptionMessage exceptionMessage) throws Exception {
        MockHttpServletRequestBuilder builder = buildRequest(method, token, path, objectJson);
        ResultMatcher[] matchers = buildFailedMatchers(status, path, exceptionMessage);

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    private void performSuccessfulRequest(HttpMethod method, HttpStatus status, String token, String path,
                                          String objectJson, TaskAttributes taskAttributes) throws Exception {
        MockHttpServletRequestBuilder builder = buildRequest(method, token, path, objectJson);
        ResultMatcher[] matchers = buildSuccessfulMatchers(method, status, taskAttributes);

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    private String getAccessToken(String username, String password) throws Exception {
        AuthRequest request = new AuthRequest(username, password);

        String path = "/api/auth/login";

        String requestJson = objectMapper.writeValueAsString(request);

        String tokenJson = mockMvc.perform(request(HttpMethod.POST, path)
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content(requestJson))
                                  .andReturn()
                                  .getResponse()
                                  .getContentAsString();

        AuthResponse response = objectMapper.readValue(tokenJson, AuthResponse.class);

        return response.getToken();
    }

    @BeforeEach
    public void setUp() throws Exception {
        User preInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        token = getAccessToken(preInsertedUser.getUsername(), preInsertedUser.getPassword());
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void anyRequest_NullInPath_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        Task requestData = createDefaultTask();

        String path = PATH_TO_API + "board/" + null;

        String requestJson = objectMapper.writeValueAsString(requestData);

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, token,
                             path, requestJson, ExceptionMessage.INVALID_DATA_ID);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void anyRequest_NullToken_ReturnsStatusCodeForbiddenAndErrorResponse() throws Exception {
        Task preInsertedTask = TestApproacherDataUtils.createPreInsertedTask(EntityNumber.FIRST);
        UUID taskID = preInsertedTask.getID();

        Task requestData = preInsertedTask;
        requestData.setTitle("Updated task");
        requestData.setDescription("Updated description");
        requestData.setPriority(Priority.valueOf("CRITICAL"));

        String path = PATH_TO_API + taskID;

        String requestJson = objectMapper.writeValueAsString(requestData);

        String token = null;

        performFailedRequest(HttpMethod.PATCH, HttpStatus.FORBIDDEN, token,
                             path, requestJson, ExceptionMessage.INVALID_AUTH_TOKEN);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void createTask_ValidTaskData_ReturnsStatusCodeCreatedAndCreatedTask() throws Exception {
        TaskBoard preInsertedTaskBoard = TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.FIRST);
        UUID taskBoardID = preInsertedTaskBoard.getID();

        Task requestData = createDefaultTask();

        String path = PATH_TO_API + "board/" + taskBoardID;

        String requestJson = objectMapper.writeValueAsString(requestData);

        performSuccessfulRequest(HttpMethod.POST, HttpStatus.CREATED, token,
                                 path, requestJson, requestData);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void createTask_NonExistentBoardID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID taskBoardID = UUID.randomUUID();

        Task requestData = createDefaultTask();

        String path = PATH_TO_API + "board/" + taskBoardID;

        String requestJson = objectMapper.writeValueAsString(requestData);

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, token,
                             path, requestJson, ExceptionMessage.NOT_FOUND);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void createTask_InvalidBoardID_ReturnsStatusCodeForbiddenAndErrorResponse() throws Exception {
        TaskBoard preInsertedTaskBoard = TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.THIRD);
        UUID taskBoardID = preInsertedTaskBoard.getID();

        Task requestData = createDefaultTask();

        String path = PATH_TO_API + "board/" + taskBoardID;

        String requestJson = objectMapper.writeValueAsString(requestData);

        performFailedRequest(HttpMethod.POST, HttpStatus.FORBIDDEN, token,
                             path, requestJson, ExceptionMessage.ACCESS_DENIED);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void createTask_IllegalTaskTitleLength_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        TaskBoard preInsertedTaskBoard = TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.FIRST);
        UUID taskBoardID = preInsertedTaskBoard.getID();

        Task requestData = createDefaultTask();
        String invalidTitle = "A".repeat(TaskConstants.MAX_TASK_TITLE_LENGTH + 20);
        requestData.setTitle(invalidTitle);

        String path = PATH_TO_API + "board/" + taskBoardID;

        String requestJson = objectMapper.writeValueAsString(requestData);

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, token,
                             path, requestJson, ExceptionMessage.INVALID_TASK_FIELDS_LENGTH);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void createTask_IllegalTaskTitle_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        TaskBoard preInsertedTaskBoard = TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.FIRST);
        UUID taskBoardID = preInsertedTaskBoard.getID();

        Task requestData = createDefaultTask();
        requestData.setTitle("");

        String path = PATH_TO_API + "board/" + taskBoardID;

        String requestJson = objectMapper.writeValueAsString(requestData);

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, token,
                             path, requestJson, ExceptionMessage.EMPTY);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void createTask_IllegalTaskPriority_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        TaskBoard preInsertedTaskBoard = TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.FIRST);
        UUID taskBoardID = preInsertedTaskBoard.getID();

        Task requestData = createDefaultTask();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("title", requestData.getTitle());
        requestMap.put("description", requestData.getDescription());
        requestMap.put("priority", "");
        requestMap.put("deadline", requestData.getDeadline());
        requestMap.put("finished", requestData.isFinished());
        requestMap.put("taskBoardID", requestData.getTaskBoardID());

        String path = PATH_TO_API + "board/" + taskBoardID;

        String requestJson = objectMapper.writeValueAsString(requestMap);

        performFailedRequest(HttpMethod.POST, HttpStatus.INTERNAL_SERVER_ERROR, token,
                             path, requestJson, ExceptionMessage.IMPOSSIBLE_TO_DESERIALIZE);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql",
            "/data/sql/insertBoards.sql", "/data/sql/insertTasks.sql"})
    void updateTask_ValidTaskData_ReturnsStatusCodeOkAndUpdatedTask() throws Exception {
        Task preInsertedTask = TestApproacherDataUtils.createPreInsertedTask(EntityNumber.FIRST);
        UUID taskID = preInsertedTask.getID();

        Task requestData = preInsertedTask;
        requestData.setTitle("Updated task");
        requestData.setDescription("Updated description");
        requestData.setPriority(Priority.valueOf("CRITICAL"));
        requestData.setDeadline(LocalDate.now().plusDays(10));
        requestData.setFinished(true);

        String path = PATH_TO_API + taskID;

        String requestJson = objectMapper.writeValueAsString(requestData);

        performSuccessfulRequest(HttpMethod.PATCH, HttpStatus.OK, token,
                                 path, requestJson, requestData);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql",
            "/data/sql/insertBoards.sql", "/data/sql/insertTasks.sql"})
    void updateTask_NonExistentTaskID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID taskID = UUID.randomUUID();

        Task requestData = TestApproacherDataUtils.createPreInsertedTask(EntityNumber.FIRST);

        String path = PATH_TO_API + taskID;

        String requestJson = objectMapper.writeValueAsString(requestData);

        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, token,
                             path, requestJson, ExceptionMessage.NOT_FOUND);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql",
            "/data/sql/insertBoards.sql", "/data/sql/insertTasks.sql"})
    void updateTask_InvalidTaskID_ReturnsStatusCodeForbiddenAndErrorResponse() throws Exception {
        Task preInsertedTask = TestApproacherDataUtils.createPreInsertedTask(EntityNumber.THIRD);
        UUID taskID = preInsertedTask.getID();

        Task requestData = TestApproacherDataUtils.createPreInsertedTask(EntityNumber.FIRST);

        String path = PATH_TO_API + taskID;

        String requestJson = objectMapper.writeValueAsString(requestData);

        performFailedRequest(HttpMethod.PATCH, HttpStatus.FORBIDDEN, token,
                             path, requestJson, ExceptionMessage.ACCESS_DENIED);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql",
            "/data/sql/insertBoards.sql", "/data/sql/insertTasks.sql"})
    void updateTask_IllegalTaskData_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        Task preInsertedTask = TestApproacherDataUtils.createPreInsertedTask(EntityNumber.FIRST);
        UUID taskID = preInsertedTask.getID();


        Task requestData = preInsertedTask;
        requestData.setDeadline(LocalDate.now().minusDays(2));

        String path = PATH_TO_API + taskID;

        String requestJson = objectMapper.writeValueAsString(requestData);

        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, token,
                             path, requestJson, ExceptionMessage.BEFORE_CURRENT_DATE);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql",
            "/data/sql/insertBoards.sql", "/data/sql/insertTasks.sql"})
    void deleteTask_ValidTaskID_ReturnsStatusCodeNoContent() throws Exception {
        Task preInsertedTask = TestApproacherDataUtils.createPreInsertedTask(EntityNumber.FIRST);
        UUID taskID = preInsertedTask.getID();

        String path = PATH_TO_API + taskID;

        performSuccessfulRequest(HttpMethod.DELETE, HttpStatus.NO_CONTENT, token,
                                 path, null, null);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql",
            "/data/sql/insertBoards.sql", "/data/sql/insertTasks.sql"})
    void deleteTask_NonExistentTaskID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID taskID = UUID.randomUUID();

        String path = PATH_TO_API + taskID;

        performFailedRequest(HttpMethod.DELETE, HttpStatus.BAD_REQUEST, token,
                             path, null, ExceptionMessage.NOT_FOUND);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql",
            "/data/sql/insertBoards.sql", "/data/sql/insertTasks.sql"})
    void deleteTask_InvalidTaskID_ReturnsStatusCodeForbiddenAndErrorResponse() throws Exception {
        Task preInsertedTask = TestApproacherDataUtils.createPreInsertedTask(EntityNumber.THIRD);
        UUID taskID = preInsertedTask.getID();

        String path = PATH_TO_API + taskID;

        performFailedRequest(HttpMethod.DELETE, HttpStatus.FORBIDDEN, token,
                             path, null, ExceptionMessage.ACCESS_DENIED);
    }
}