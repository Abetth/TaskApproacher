package com.taskapproacher.controller.user;

import com.taskapproacher.constant.ExceptionMessage;
import com.taskapproacher.constant.Role;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.user.UserResponse;
import com.taskapproacher.exception.GlobalExceptionHandler;
import com.taskapproacher.interfaces.TaskMatcher;
import com.taskapproacher.service.user.UserService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import org.hamcrest.core.StringContains;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

import org.mockito.ArgumentMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// Illegal***Data = Empty || Null or any other unsuitable data
//Tests naming convention: method_scenario_result
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final String PATH_TO_API = "/api/users/";

    private User createDefaultUser(UUID userID) {
        User user = new User();
        user.setID(userID);
        user.setUsername("Created User 1");
        user.setPassword("userpass");
        user.setEmail("usermail@mail.mail");
        user.setRole(Role.USER);
        user.setTaskBoards(null);

        return user;
    }

    private List<TaskBoard> createDefaultListOfTaskBoards(User user) {
        TaskBoard firstTaskBoard = new TaskBoard(
                UUID.randomUUID(),
                "First task board",
                false,
                null,
                user
        );

        TaskBoard secondTaskBoard = new TaskBoard(
                UUID.randomUUID(),
                "First task board",
                true,
                null,
                user
        );

        return List.of(firstTaskBoard, secondTaskBoard);
    }

    private Map<String, Object> mapUser(User user) {
        Map<String, Object> userMap = Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "password", user.getPassword()
        );

        return userMap;
    }

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
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

    private ResultMatcher[] buildSuccessfulMatchers(HttpMethod method, HttpStatus status, UserResponse userResponse) {
        List<ResultMatcher> matchers = new ArrayList<>();
        int statusCode = status.value();

        matchers.add(status().is(statusCode));

        if (method == HttpMethod.DELETE) {
            return matchers.toArray(new ResultMatcher[0]);
        }

        matchers.add(jsonPath("$.id").value(userResponse.getID().toString()));
        matchers.add(jsonPath("$.username").value(userResponse.getUsername()));
        matchers.add(jsonPath("$.email").value(userResponse.getEmail()));
        matchers.add(jsonPath("$.role").value(userResponse.getRole().toString()));
        matchers.add(jsonPath("$.password").doesNotExist());

        return matchers.toArray(new ResultMatcher[0]);
    }

    private MockHttpServletRequestBuilder buildRequest(HttpMethod method, String path, String objectJson) {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(method, path)
                .contentType(MediaType.APPLICATION_JSON);

        if (method == HttpMethod.PATCH) {
            builder.content(objectJson);
        }

        return builder;
    }

    private void performFailedRequest(HttpMethod method, HttpStatus status, String path, String objectJson, ExceptionMessage exceptionMessage) throws Exception{
        MockHttpServletRequestBuilder builder = buildRequest(method, path, objectJson);
        ResultMatcher[] matchers = buildFailedMatchers(status, path, exceptionMessage);

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    private void performSuccessfulRequest(HttpMethod method, HttpStatus status, String path, String objectJson, Object obj) throws Exception {
        UserResponse response;
        if (obj instanceof User) {
            response = new UserResponse((User) obj);
        } else {
            response = (UserResponse) obj;
        }

        MockHttpServletRequestBuilder builder = buildRequest(method, path, objectJson);
        ResultMatcher[] matchers = buildSuccessfulMatchers(method, status, response);

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    @Test
    void anyRequest_NullPath_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        String path = PATH_TO_API + null + "/boards";

        performFailedRequest(HttpMethod.GET, HttpStatus.BAD_REQUEST, path, null, ExceptionMessage.INVALID_DATA);

        verify(userService, times(0)).findBoardsByUser(ArgumentMatchers.any(UUID.class));
    }

    @Test
    void getUserProfile_ValidUserAuthentication_ReturnsStatusCodeOkAndResponseUser() throws Exception {
        UUID userID = UUID.randomUUID();
        User user = createDefaultUser(userID);

        String path = PATH_TO_API + "/profile";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        performSuccessfulRequest(HttpMethod.GET, HttpStatus.OK, path, null, user);

        SecurityContextHolder.clearContext();

        verify(authentication, times(1)).getPrincipal();
    }

    @Test
    void getBoardsByUser_ValidUserID_ReturnsStatusCodeOkAndTaskBoardResponseList() throws Exception {
        UUID userID = UUID.randomUUID();
        User user = createDefaultUser(userID);
        List<TaskBoardResponse> response = createDefaultListOfTaskBoards(user)
                .stream()
                .map(TaskBoardResponse::new)
                .toList();

        String path = PATH_TO_API + userID + "/boards";

        when(userService.findBoardsByUser(ArgumentMatchers.any(UUID.class)))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.GET, path)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.[0].id").value(response.get(0).getID().toString()))
                .andExpect(jsonPath("$.[1].id").value(response.get(1).getID().toString()));

        verify(userService, times(1)).findBoardsByUser(ArgumentMatchers.any(UUID.class));
    }

    @Test
    void getBoardsByUser_InvalidUserID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID userID = UUID.randomUUID();

        String path = PATH_TO_API + userID + "/boards";

        when(userService.findBoardsByUser(ArgumentMatchers.any(UUID.class)))
                .thenThrow(new EntityNotFoundException(ExceptionMessage.NOT_FOUND.toString()));

        performFailedRequest(HttpMethod.GET, HttpStatus.BAD_REQUEST, path, null, ExceptionMessage.NOT_FOUND);

        verify(userService, times(1)).findBoardsByUser(ArgumentMatchers.any(UUID.class));
    }

    @Test
    void getBoardsByUser_IllegalUserID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID userID = UUID.randomUUID();

        String path = PATH_TO_API + userID + "/boards";

        when(userService.findBoardsByUser(ArgumentMatchers.any(UUID.class)))
                .thenThrow(new IllegalArgumentException(ExceptionMessage.NULL.toString()));

        performFailedRequest(HttpMethod.GET, HttpStatus.BAD_REQUEST, path, null, ExceptionMessage.NULL);

        verify(userService, times(1)).findBoardsByUser(ArgumentMatchers.any(UUID.class));
    }

    @Test
    void update_ValidUserData_ReturnsStatusCodeOkAndUserResponse() throws Exception {
        UUID userID = UUID.randomUUID();

        User updateData = createDefaultUser(null);
        updateData.setUsername("Updated user");
        updateData.setEmail("mail@mail.mail");
        updateData.setPassword("newuserpass");
        Map<String, Object> updateMap = mapUser(updateData);

        UserResponse response = new UserResponse(updateData);
        response.setID(userID);

        String updateDataJson = objectMapper.writeValueAsString(updateMap);
        String path = PATH_TO_API + userID;

        when(userService.update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(User.class)))
                .thenReturn(response);

        performSuccessfulRequest(HttpMethod.PATCH, HttpStatus.OK, path, updateDataJson, response);

        verify(userService, times(1))
                .update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(User.class));
    }

    @Test
    void update_InvalidUserID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID userID = UUID.randomUUID();
        User updateData = createDefaultUser(userID);
        Map<String, Object> updateMap = mapUser(updateData);

        String updateDataJson = objectMapper.writeValueAsString(updateMap);
        String path = PATH_TO_API + userID;

        when(userService.update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(User.class)))
                .thenThrow(new EntityNotFoundException(ExceptionMessage.NOT_FOUND.toString()));

        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, path, updateDataJson, ExceptionMessage.NOT_FOUND);

        verify(userService, times(1)).update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(User.class));
    }

    @Test
    void update_IllegalUserData_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID userID = UUID.randomUUID();
        User updateData = createDefaultUser(userID);
        Map<String, Object> updateMap = mapUser(updateData);

        String updateDataJson = objectMapper.writeValueAsString(updateMap);
        String path = PATH_TO_API + userID;

        when(userService.update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(User.class)))
                .thenThrow(new IllegalArgumentException(ExceptionMessage.NULL.toString()));

        performFailedRequest(HttpMethod.PATCH, HttpStatus.BAD_REQUEST, path, updateDataJson, ExceptionMessage.NULL);

        verify(userService, times(1)).update(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(User.class));
    }

    @Test
    void delete_ValidUserID_ReturnsStatusCodeNoContent() throws Exception {
        UUID userID = UUID.randomUUID();

        String path = PATH_TO_API + userID;

        doNothing().when(userService).delete(ArgumentMatchers.any(UUID.class));

        performSuccessfulRequest(HttpMethod.DELETE, HttpStatus.NO_CONTENT, path, null, null);

        verify(userService, times(1)).delete(ArgumentMatchers.any(UUID.class));
    }

    @Test
    void delete_IllegalUserID_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        UUID userID = UUID.randomUUID();

        String path = PATH_TO_API + userID;

        doThrow(new IllegalArgumentException(ExceptionMessage.NULL.toString()))
                .when(userService).delete(ArgumentMatchers.any(UUID.class));

        performFailedRequest(HttpMethod.DELETE, HttpStatus.BAD_REQUEST, path, null, ExceptionMessage.NULL);

        verify(userService, times(1)).delete(ArgumentMatchers.any(UUID.class));
    }
}
