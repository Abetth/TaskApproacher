package com.taskapproacher.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.taskapproacher.auth.model.AuthRequest;
import com.taskapproacher.auth.model.AuthResponse;
import com.taskapproacher.common.constant.EntityNumber;
import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.utils.TestApproacherDataUtils;
import com.taskapproacher.task.mapper.TaskBoardMapper;
import com.taskapproacher.task.model.TaskBoardDTO;
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
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//Tests naming convention: method_scenario_result
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private final String PATH_TO_API = "/api/user/";
    private final TaskBoardMapper taskBoardMapper = new TaskBoardMapper();
    private String token;

    private Map<String, Object> buildUpdatePayload(User user) {
        return Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "password", user.getPassword()
        );
    }

    @BeforeEach
    public void setUp() throws Exception {
        User preInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        token = getAccessToken(preInsertedUser.getUsername(), preInsertedUser.getPassword());
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

    private ResultMatcher[] buildSuccessfulMatchers(HttpMethod method, HttpStatus status, User user) {
        List<ResultMatcher> matchers = new ArrayList<>();
        int statusCode = status.value();

        matchers.add(status().is(statusCode));

        if (method == HttpMethod.DELETE) {
            return matchers.toArray(new ResultMatcher[0]);
        }

        matchers.add(jsonPath("$.id").value(user.getID().toString()));
        matchers.add(jsonPath("$.username").value(user.getUsername()));
        matchers.add(jsonPath("$.email").value(user.getEmail()));
        matchers.add(jsonPath("$.role").value(user.getRole().toString()));
        matchers.add(jsonPath("$.password").doesNotExist());

        return matchers.toArray(new ResultMatcher[0]);
    }

    private MockHttpServletRequestBuilder buildRequest(HttpMethod method, String token,
                                                       String path, String objectJson) {
        MockHttpServletRequestBuilder builder = request(method, path)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token);

        if (method == HttpMethod.PATCH) {
            builder.content(objectJson);
        }

        return builder;
    }

    private void performFailedRequest(HttpMethod method, HttpStatus status, String token, String path,
                                      String objectJson, ExceptionMessage exceptionMessage) throws Exception {
        MockHttpServletRequestBuilder builder = buildRequest(method, token, path, objectJson);
        ResultMatcher[] matchers = buildFailedMatchers(status, path, exceptionMessage);

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    private void performSuccessfulRequest(HttpMethod method, HttpStatus status, String token, String path,
                                          String objectJson, User user) throws Exception {

        MockHttpServletRequestBuilder builder = buildRequest(method, token, path, objectJson);
        ResultMatcher[] matchers = buildSuccessfulMatchers(method, status, user);

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

    private List<TaskBoardDTO> createDTOListOfPreInsertedTaskBoards() {
        TaskBoardDTO firstBoard = taskBoardMapper.mapToTaskBoardDTO(
                TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.FIRST)
        );
        TaskBoardDTO secondBoard = taskBoardMapper.mapToTaskBoardDTO(
                TestApproacherDataUtils.createPreInsertedTaskBoard(EntityNumber.SECOND)
        );

        return List.of(firstBoard, secondBoard);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void anyRequest_NullInPath_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        String path = PATH_TO_API + null + "/boards";

        performFailedRequest(HttpMethod.GET, HttpStatus.BAD_REQUEST, token,
                             path, null, ExceptionMessage.INVALID_DATA_ID);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void anyRequest_NullToken_ReturnsStatusCodeForbiddenAndErrorResponse() throws Exception {
        String path = PATH_TO_API + null + "/boards";

        String token = null;

        performFailedRequest(HttpMethod.GET, HttpStatus.FORBIDDEN, token,
                             path, null, ExceptionMessage.INVALID_AUTH_TOKEN);
    }


    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void getUserProfile_ValidUserAuthentication_ReturnsStatusCodeOkAndUserData() throws Exception {
        String path = PATH_TO_API + "/profile";

        User preInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);

        performSuccessfulRequest(HttpMethod.GET, HttpStatus.OK, token,
                                 path, null, preInsertedUser);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql", "/data/sql/insertBoards.sql"})
    void getBoardsByUser_ValidUserID_ReturnsStatusCodeOkAndTaskBoardResponseList() throws Exception {
        User preInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        UUID userID = preInsertedUser.getID();

        List<TaskBoardDTO> preInsertedTaskBoards = createDTOListOfPreInsertedTaskBoards();

        String path = PATH_TO_API + userID + "/boards";

        mockMvc.perform(request(HttpMethod.GET, path)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token))
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().is(HttpStatus.OK.value()))
               .andExpect(jsonPath("$.[0].id").value(preInsertedTaskBoards.get(0).getID().toString()))
               .andExpect(jsonPath("$.[1].id").value(preInsertedTaskBoards.get(1).getID().toString()));
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void getBoardsByUser_InvalidUserID_ReturnsStatusCodeForbiddenRequestAndErrorResponse() throws Exception {
        UUID userID = UUID.randomUUID();

        String path = PATH_TO_API + userID + "/boards";

        performFailedRequest(HttpMethod.GET, HttpStatus.FORBIDDEN, token,
                             path, null, ExceptionMessage.ACCESS_DENIED);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void updateUser_ValidUserData_ReturnsStatusCodeOkAndUpdatedUser() throws Exception {
        User preInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        UUID userID = preInsertedUser.getID();

        User updatedUserData = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        updatedUserData.setUsername("Updated user");
        updatedUserData.setEmail("mail@mail.mail");
        updatedUserData.setPassword("newuserpass");

        Map<String, Object> userUpdatePayloadMap = buildUpdatePayload(updatedUserData);

        String path = PATH_TO_API + userID;

        String updateDataJson = objectMapper.writeValueAsString(userUpdatePayloadMap);

        performSuccessfulRequest(HttpMethod.PATCH, HttpStatus.OK, token,
                                 path, updateDataJson, updatedUserData);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void updateUser_EmptyUserData_ReturnsStatusCodeOkAndUpdatedUser() throws Exception {
        User preInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        UUID userID = preInsertedUser.getID();

        User updatedUserData = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        updatedUserData.setUsername("");
        updatedUserData.setEmail("");
        updatedUserData.setPassword("");

        Map<String, Object> userUpdatePayloadMap = buildUpdatePayload(updatedUserData);

        String path = PATH_TO_API + userID;

        String updateDataJson = objectMapper.writeValueAsString(userUpdatePayloadMap);

        performSuccessfulRequest(HttpMethod.PATCH, HttpStatus.OK, token,
                                 path, updateDataJson, preInsertedUser);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void updateUser_InvalidUserID_ReturnsStatusCodeForbiddenAndErrorResponse() throws Exception {
        UUID userID = UUID.randomUUID();

        String path = PATH_TO_API + userID;

        User updatedUserData = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);

        Map<String, Object> updateMap = buildUpdatePayload(updatedUserData);

        String updateDataJson = objectMapper.writeValueAsString(updateMap);

        performFailedRequest(HttpMethod.PATCH, HttpStatus.FORBIDDEN, token,
                             path, updateDataJson, ExceptionMessage.ACCESS_DENIED);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void updateUser_AlreadyTakenUsername_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        User firstPreInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        UUID userID = firstPreInsertedUser.getID();

        User secondPreInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.SECOND);
        String alreadyTakenUsername = secondPreInsertedUser.getUsername();

        String path = PATH_TO_API + userID;

        User updateUserData = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        updateUserData.setUsername(alreadyTakenUsername);

        Map<String, Object> updateMap = buildUpdatePayload(updateUserData);

        String updateDataJson = objectMapper.writeValueAsString(updateMap);

        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, token,
                             path, updateDataJson, ExceptionMessage.ALREADY_EXISTS);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void updateUser_AlreadyTakenEmail_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        User firstPreInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        UUID userID = firstPreInsertedUser.getID();

        User secondPreInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.SECOND);
        String alreadyTakenEmail = secondPreInsertedUser.getEmail();

        String path = PATH_TO_API + userID;

        User updateUserData = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        updateUserData.setEmail(alreadyTakenEmail);

        Map<String, Object> updateMap = buildUpdatePayload(updateUserData);

        String updateDataJson = objectMapper.writeValueAsString(updateMap);

        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, token,
                             path, updateDataJson, ExceptionMessage.ALREADY_EXISTS);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void updateUser_InvalidUsername_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        User firstPreInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        UUID userID = firstPreInsertedUser.getID();

        String path = PATH_TO_API + userID;

        User updateUserData = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        updateUserData.setUsername("U");

        Map<String, Object> updateMap = buildUpdatePayload(updateUserData);

        String updateDataJson = objectMapper.writeValueAsString(updateMap);

        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, token,
                             path, updateDataJson, ExceptionMessage.INVALID_USERNAME_LENGTH);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void updateUser_InvalidPassword_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        User firstPreInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        UUID userID = firstPreInsertedUser.getID();

        String path = PATH_TO_API + userID;

        User updateUserData = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        updateUserData.setUsername("Updated Data 1");
        updateUserData.setEmail("upml@m.m");
        updateUserData.setPassword("11");

        Map<String, Object> updateMap = buildUpdatePayload(updateUserData);

        String updateDataJson = objectMapper.writeValueAsString(updateMap);

        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, token,
                             path, updateDataJson, ExceptionMessage.INVALID_PASSWORD_LENGTH);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void deleteUser_ValidUserID_ReturnsStatusCodeNoContent() throws Exception {
        User preInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        UUID userID = preInsertedUser.getID();

        String path = PATH_TO_API + userID;

        performSuccessfulRequest(HttpMethod.DELETE, HttpStatus.NO_CONTENT, token,
                                 path, null, null);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void deleteUser_InvalidUserID_ReturnsStatusCodeForbiddenAndErrorResponse() throws Exception {
        UUID userID = UUID.randomUUID();

        String path = PATH_TO_API + userID;

        performFailedRequest(HttpMethod.DELETE, HttpStatus.FORBIDDEN, token,
                             path, null, ExceptionMessage.ACCESS_DENIED);
    }
}
