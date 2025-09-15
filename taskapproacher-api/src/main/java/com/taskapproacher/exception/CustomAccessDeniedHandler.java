package com.taskapproacher.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskapproacher.constant.ExceptionMessage;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        int status = HttpStatus.FORBIDDEN.value();
        String message = ExceptionMessage.ACCESS_DENIED.toString();

        Throwable cause = accessDeniedException.getCause();
        if (cause instanceof ExpiredJwtException) {
            status = HttpStatus.FORBIDDEN.value();
            message = ExceptionMessage.EXPIRED_AUTH.toString();
        } else if (cause instanceof MalformedJwtException) {
            status = HttpStatus.FORBIDDEN.value();
            message = ExceptionMessage.INVALID_AUTH_TOKEN.toString();
        }

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
