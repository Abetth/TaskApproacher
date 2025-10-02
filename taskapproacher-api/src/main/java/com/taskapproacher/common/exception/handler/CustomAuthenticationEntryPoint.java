package com.taskapproacher.common.exception.handler;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.exception.util.ErrorResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseUtil errorResponseUtil;

    @Autowired
    public CustomAuthenticationEntryPoint(ErrorResponseUtil errorResponseUtil) {
        this.errorResponseUtil = errorResponseUtil;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        int status = HttpStatus.UNAUTHORIZED.value();
        String message = ExceptionMessage.INVALID_USER_DATA.toString();

        errorResponseUtil.sendErrorResponse(request, response, status, message);
    }
}
