package com.taskapproacher.common.exception.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ErrorResponseUtil {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendErrorResponse(HttpServletRequest request, HttpServletResponse response,
                                  int status, String message) throws IOException {
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
