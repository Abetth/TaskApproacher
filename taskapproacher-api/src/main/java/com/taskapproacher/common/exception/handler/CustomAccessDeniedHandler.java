package com.taskapproacher.common.exception.handler;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.exception.util.ErrorResponseUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ErrorResponseUtil errorResponseUtil;

    @Autowired
    public CustomAccessDeniedHandler(ErrorResponseUtil errorResponseUtil) {
        this.errorResponseUtil = errorResponseUtil;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
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

        errorResponseUtil.sendErrorResponse(request, response, status, message);
    }
}
