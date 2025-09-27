package com.taskapproacher.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.taskapproacher.auth.model.AuthRequest;
import com.taskapproacher.auth.model.AuthResponse;
import com.taskapproacher.auth.model.RegisterRequest;
import com.taskapproacher.auth.service.JwtService;
import com.taskapproacher.auth.service.UserDetailsServiceImpl;
import com.taskapproacher.common.constant.EntityNumber;
import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.utils.TestApproacherDataUtils;
import com.taskapproacher.user.model.User;
import com.taskapproacher.user.service.UserService;

import org.hamcrest.core.StringContains;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//Tests naming convention: method_scenario_result
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;
    private final String PATH_TO_API = "/api/auth/";

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

    private MockHttpServletRequestBuilder buildRequest(HttpMethod method, String path, String objectJson) {
        MockHttpServletRequestBuilder builder = request(method, path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectJson);

        return builder;
    }

    private void performFailedRequest(HttpMethod method, HttpStatus status, String path,
                                      String objectJson, ExceptionMessage exceptionMessage) throws Exception {
        MockHttpServletRequestBuilder builder = buildRequest(method, path, objectJson);
        ResultMatcher[] matchers = buildFailedMatchers(status, path, exceptionMessage);

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void login_ValidUserData_ReturnsStatusCodeOkAndToken() throws Exception {
        User preInsertedUser = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST);
        String username = preInsertedUser.getUsername();
        String password = preInsertedUser.getPassword();

        AuthRequest request = new AuthRequest();
        request.setUsername(username);
        request.setPassword(password);

        String path = PATH_TO_API + "login";

        String requestJson = objectMapper.writeValueAsString(request);

        String tokenJson = mockMvc.perform(request(HttpMethod.POST, path)
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content(requestJson))
                                  .andExpect(status().is(HttpStatus.OK.value()))
                                  .andReturn()
                                  .getResponse()
                                  .getContentAsString();

        AuthResponse response = objectMapper.readValue(tokenJson, AuthResponse.class);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        assertEquals(request.getUsername(), jwtService.extractUsername(response.getToken()));
        assertTrue(jwtService.isTokenValid(response.getToken(), userDetails));
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void login_InvalidUserData_ReturnsStatusCodeUnauthorizedAndErrorResponse() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("TestUser");
        request.setPassword("1211");

        String path = PATH_TO_API + "login";

        String requestJson = objectMapper.writeValueAsString(request);

        performFailedRequest(HttpMethod.POST, HttpStatus.UNAUTHORIZED, path,
                             requestJson, ExceptionMessage.INVALID_USER_DATA);
    }

    @Test
    @Sql(scripts = "/data/sql/clearData.sql")
    void register_ValidUserData_ReturnsStatusCodeCreatedAndToken() throws Exception {
        String username = "TestUser";

        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail("mail@mail.mail");
        request.setPassword("123pass123");

        String path = PATH_TO_API + "register";

        String requestJson = objectMapper.writeValueAsString(request);

        String tokenJson = mockMvc.perform(request(HttpMethod.POST, path)
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content(requestJson))
                                  .andExpect(status().is(HttpStatus.CREATED.value()))
                                  .andReturn()
                                  .getResponse()
                                  .getContentAsString();

        AuthResponse response = objectMapper.readValue(tokenJson, AuthResponse.class);
        User createdUser = userService.findByUsername(username);

        assertEquals(request.getUsername(), jwtService.extractUsername(response.getToken()));
        assertEquals(request.getUsername(), createdUser.getUsername());
        assertEquals(request.getEmail(), createdUser.getEmail());
        assertTrue(createdUser.getPassword().startsWith("{bcrypt}"));
    }

    @Test
    @Sql(scripts = "/data/sql/clearData.sql")
    void register_IllegalUserData_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        String username = "";

        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail("mail@mail.mail");
        request.setPassword("123pass123");

        String path = PATH_TO_API + "register";

        String requestJson = objectMapper.writeValueAsString(request);

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, path,
                             requestJson, ExceptionMessage.EMPTY);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void register_DuplicateUsername_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        String username = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST).getUsername();

        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail("mail@mail.mail");
        request.setPassword("123pass123");

        String path = PATH_TO_API + "register";

        String requestJson = objectMapper.writeValueAsString(request);

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, path,
                             requestJson, ExceptionMessage.ALREADY_EXISTS);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void register_DuplicateEmail_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        String username = "New user";
        String email = TestApproacherDataUtils.createPreInsertedUser(EntityNumber.FIRST).getEmail();

        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword("123pass123");

        String path = PATH_TO_API + "register";

        String requestJson = objectMapper.writeValueAsString(request);

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, path,
                             requestJson, ExceptionMessage.ALREADY_EXISTS);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void register_InvalidPassword_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        String username = "Test User";

        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail("mail@mail.mail");
        request.setPassword("111");

        String path = PATH_TO_API + "register";

        String requestJson = objectMapper.writeValueAsString(request);

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, path,
                             requestJson, ExceptionMessage.INVALID_PASSWORD_LENGTH);
    }

    @Test
    @Sql(scripts = {"/data/sql/clearData.sql", "/data/sql/insertUsers.sql"})
    void register_InvalidUsername_ReturnsStatusCodeBadRequestAndErrorResponse() throws Exception {
        String username = "T";

        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail("ma@mail.mail");
        request.setPassword("111222333");

        String path = PATH_TO_API + "register";

        String requestJson = objectMapper.writeValueAsString(request);

        performFailedRequest(HttpMethod.POST, HttpStatus.BAD_REQUEST, path,
                             requestJson, ExceptionMessage.INVALID_USERNAME_LENGTH);
    }
}
