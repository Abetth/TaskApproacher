package com.taskapproacher.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.taskapproacher.constant.ExceptionMessage;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        int status = HttpStatus.UNAUTHORIZED.value();
        String message = ExceptionMessage.INVALID_USER_DATA.toString();

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("type", "about:blank");
        errorBody.put("title", HttpStatus.valueOf(status).getReasonPhrase());
        errorBody.put("status", status);
        errorBody.put("detail", message);
        errorBody.put("instance", request.getRequestURI());

        objectMapper.writeValue(response.getWriter(), errorBody);
    }
}
