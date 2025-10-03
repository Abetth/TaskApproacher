package com.taskapproacher.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.taskapproacher.auth.model.AuthRequest;
import com.taskapproacher.auth.model.AuthResponse;
import com.taskapproacher.common.constant.EntityNumber;
import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.interfaces.matcher.TaskBoardMatcher;
import com.taskapproacher.common.utils.TestApproacherDataUtils;
import com.taskapproacher.task.mapper.TaskBoardMapper;
import com.taskapproacher.task.mapper.TaskMapper;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.model.TaskBoardDTO;
import com.taskapproacher.task.model.TaskDTO;
import com.taskapproacher.user.model.User;

import org.hamcrest.core.StringContains;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//Tests naming convention: method_scenario_result
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskBoardControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private final String PATH_TO_API = "/api/board/";
    private final TaskBoardMapper taskBoardMapper = new TaskBoardMapper();
    private final TaskMapper taskMapper = new TaskMapper();
    private String token;

    private TaskBoardDTO createDefaultTaskBoardDTO(UUID userID) {
        TaskBoardDTO taskBoard = new TaskBoardDTO();
        taskBoard.setTitle("Test Board Creation");
        taskBoard.setSorted(false);
        taskBoard.setUserID(userID);

        return taskBoard;
    }

    private List<TaskDTO> createDTOListOfPreInsertedTasks() {
        TaskDTO firstTask = taskMapper.mapToTaskDTO(TestApproacherDataUtils.createPreInsertedTask(EntityNumber.FIRST));
        TaskDTO secondTask = taskMapper.mapToTaskDTO(TestApproacherDataUtils.createPreInsertedTask(EntityNumber.SECOND));

        return List.of(firstTask, secondTask);
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

    private ResultMatcher[] buildSuccessfulMatchers(HttpMethod method, HttpStatus status,
                                                    TaskBoardMatcher taskBoardMatcher) {
        List<ResultMatcher> matchers = new ArrayList<>();
        int statusCode = status.value();

        matchers.add(status().is(statusCode));
        if (method == HttpMethod.GET || method == HttpMethod.DELETE) {
            return matchers.toArray(new ResultMatcher[0]);
        }

        if (method != HttpMethod.POST) {
            matchers.add(jsonPath("$.id").value(taskBoardMatcher.getID().toString()));
            matchers.add(jsonPath("$.userID").value(taskBoardMatcher.getUserID().toString()));
        } else {
            matchers.add(jsonPath("$.id").isNotEmpty());
            matchers.add(jsonPath("$.userID").isNotEmpty());
        }

        matchers.add(content().contentType(MediaType.APPLICATION_JSON));
        matchers.add(jsonPath("$.title").value(taskBoardMatcher.getTitle()));
        matchers.add(jsonPath("$.sorted").value(taskBoardMatcher.isSorted()));
        matchers.add(jsonPath("$.tasks").value(taskBoardMatcher.getTasks()));

        return matchers.toArray(new ResultMatcher[0]);
    }

    private MockHttpServletRequestBuilder buildRequest(HttpMethod method, String token,
                                                       String path, String objectJson) {
        MockHttpServletRequestBuilder builder = request(method, path)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token);

        if (method != HttpMethod.GET & method != HttpMethod.DELETE) {
            builder.content(objectJson);
        }

        return builder;
    }

    private void performFailedRequest(HttpMethod method, HttpStatus status, String token, String path,
                                      String objectJson, ExceptionMessage exceptionMessage) throws Exception {
        ResultMatcher[] matchers = buildFailedMatchers(status, path, exceptionMessage);
        MockHttpServletRequestBuilder builder = buildRequest(method, token, path, objectJson);

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    private void performSuccessfulRequest(HttpMethod method, HttpStatus status, String token, String path,
                                          String objectJson, TaskBoardMatcher taskBoardMatcher) throws Exception {
        ResultMatcher[] matchers = buildSuccessfulMatchers(method, status, taskBoardMatcher);
        MockHttpServletRequestBuilder builder = buildRequest(method, token, path, objectJson);

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    private String getAccessToken(String username, String password) throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername(username);
        request.setPassword(password);

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
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void anyRequest_NullInPath_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        TaskBoardDTO request = createDefaultTaskBoardDTO(UUID.randomUUID());

        String path = PATH_TO_API + null + "/tasks";

        String taskBoardJson = objectMapper.writeValueAsString(request);

        performFailedRequest(HttpMethod.GET, HttpStatus.BAD_REQUEST, token,
                             path, taskBoardJson, ExceptionMessage.INVALID_DATA_ID);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void anyRequest_NullToken_ReturnsStatusCodeForbiddenAndErrorResponse() throws Exception {
        User preInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        UUID userID = preInsertedUser.getID();

        TaskBoardDTO request = createDefaultTaskBoardDTO(userID);

        String path = PATH_TO_API + userID;

        String requestJson = objectMapper.writeValueAsString(request);

        String token = null;

        performFailedRequest(HttpMethod.POST, HttpStatus.FORBIDDEN, token,
                             path, requestJson, ExceptionMessage.INVALID_AUTH_TOKEN);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql",
            "/data/sql/insertBoards.sql", "/data/sql/insertTasks.sql"})
    void getTasksByBoard_ValidTaskBoardID_ReturnsStatusCodeOkAndListOfTaskResponse() throws Exception {
        TaskBoard preInsertedTaskBoard = TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.FIRST);
        UUID taskBoardID = preInsertedTaskBoard.getID();

        List<TaskDTO> preInsertedTasks = createDTOListOfPreInsertedTasks();

        String path = PATH_TO_API + taskBoardID + "/tasks";

        mockMvc.perform(request(HttpMethod.GET, path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id").value(preInsertedTasks.get(0).getID().toString()))
                .andExpect(jsonPath("$.[0].title").value(preInsertedTasks.get(0).getTitle()))
                .andExpect(jsonPath("$.[1].id").value(preInsertedTasks.get(1).getID().toString()))
                .andExpect(jsonPath("$.[1].title").value(preInsertedTasks.get(1).getTitle()));
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql",
            "/data/sql/insertBoards.sql", "/data/sql/insertTasks.sql"})
    void getTasksByBoard_NonExistentTaskBoardID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID taskBoardID = UUID.randomUUID();

        String path = PATH_TO_API + taskBoardID + "/tasks";

        performFailedRequest(HttpMethod.GET, HttpStatus.BAD_REQUEST, token,
                             path, null, ExceptionMessage.NOT_FOUND);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql",
            "/data/sql/insertBoards.sql", "/data/sql/insertTasks.sql"})
    void getTasksByBoard_InvalidTaskBoardID_ReturnsStatusCodeForbiddenAndErrorResponse() throws Exception {
        TaskBoard preInsertedTaskBoard = TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.THIRD);
        UUID taskBoardID = preInsertedTaskBoard.getID();

        String path = PATH_TO_API + taskBoardID + "/tasks";

        performFailedRequest(HttpMethod.GET, HttpStatus.FORBIDDEN, token,
                             path, null, ExceptionMessage.ACCESS_DENIED);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void createTaskBoard_ValidTaskBoardData_ReturnsStatusCodeCreatedAndTaskBoardResponse() throws Exception {
        User preInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        UUID userID = preInsertedUser.getID();

        TaskBoardDTO request = createDefaultTaskBoardDTO(userID);

        String path = PATH_TO_API + userID;

        String requestJson = objectMapper.writeValueAsString(request);

        performSuccessfulRequest(HttpMethod.POST, HttpStatus.CREATED, token,
                                 path, requestJson, request);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void createTaskBoard_InvalidUserID_ReturnsStatusCodeForbiddenAndErrorResponse() throws Exception {
        UUID userID = UUID.randomUUID();

        TaskBoardDTO requestBoard = createDefaultTaskBoardDTO(userID);

        String path = PATH_TO_API + userID;

        String requestJson = objectMapper.writeValueAsString(requestBoard);

        performFailedRequest(HttpMethod.POST, HttpStatus.FORBIDDEN, token,
                             path, requestJson, ExceptionMessage.ACCESS_DENIED);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void createTaskBoard_IllegalTaskBoardData_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        User preInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        UUID userID = preInsertedUser.getID();

        TaskBoardDTO request = createDefaultTaskBoardDTO(userID);
        request.setTitle("");

        String path = PATH_TO_API + userID;

        String requestJson = objectMapper.writeValueAsString(request);

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, token,
                             path, requestJson, ExceptionMessage.EMPTY);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void updateTaskBoard_ValidTaskBoardData_ReturnsStatusCodeOkAndTaskBoardResponse() throws Exception {
        TaskBoard preInsertedTaskBoard = TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.FIRST);
        UUID taskBoardID = preInsertedTaskBoard.getID();

        TaskBoardDTO updatedTaskBoardData = taskBoardMapper.mapToTaskBoardDTO(preInsertedTaskBoard);
        updatedTaskBoardData.setTitle("Updated title");
        updatedTaskBoardData.setSorted(true);

        String path = PATH_TO_API + taskBoardID;

        String updateDataJson = objectMapper.writeValueAsString(updatedTaskBoardData);

        performSuccessfulRequest(HttpMethod.PATCH, HttpStatus.OK, token,
                                 path, updateDataJson, updatedTaskBoardData);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void updateTaskBoard_NonExistentTaskBoardID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID taskBoardID = UUID.randomUUID();

        TaskBoardDTO updatedTaskBoardData = new TaskBoardDTO();

        String path = PATH_TO_API + taskBoardID;

        String updateDataJson = objectMapper.writeValueAsString(updatedTaskBoardData);

        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, token,
                             path, updateDataJson, ExceptionMessage.NOT_FOUND);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void updateTaskBoard_InvalidTaskBoardID_ReturnsStatusCodeForbiddenAndErrorResponse() throws Exception {
        TaskBoard preInsertedTaskBoard = TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.THIRD);
        UUID taskBoardID = preInsertedTaskBoard.getID();

        TaskBoardDTO updatedTaskBoardData = taskBoardMapper.mapToTaskBoardDTO(
                TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.FIRST)
        );

        String path = PATH_TO_API + taskBoardID;

        String updateDataJson = objectMapper.writeValueAsString(updatedTaskBoardData);

        performFailedRequest(HttpMethod.PATCH, HttpStatus.FORBIDDEN, token,
                             path, updateDataJson, ExceptionMessage.ACCESS_DENIED);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void deleteTaskBoard_ValidTaskBoardID_ReturnsStatusCodeNoContent() throws Exception {
        TaskBoard preInsertedTaskBoard = TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.FIRST);
        UUID taskBoardID = preInsertedTaskBoard.getID();

        String path = PATH_TO_API + taskBoardID;

        performSuccessfulRequest(HttpMethod.DELETE, HttpStatus.NO_CONTENT, token,
                                 path, null, null);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void deleteTaskBoard_NonExistentTaskBoardID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID taskBoardID = UUID.randomUUID();

        String path = PATH_TO_API + taskBoardID;

        performFailedRequest(HttpMethod.DELETE, HttpStatus.BAD_REQUEST, token,
                             path, null, ExceptionMessage.NOT_FOUND);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void deleteTaskBoard_InvalidTaskBoardID_ReturnsStatusCodeForbiddenAndErrorResponse() throws Exception {
        TaskBoard preInsertedTaskBoard = TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.THIRD);
        UUID taskBoardID = preInsertedTaskBoard.getID();

        String path = PATH_TO_API + taskBoardID;

        performFailedRequest(HttpMethod.DELETE, HttpStatus.FORBIDDEN, token,
                             path, null, ExceptionMessage.ACCESS_DENIED);
    }
}
