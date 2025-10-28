package com.taskapproacher.common.exception.handler;

import com.fasterxml.jackson.core.JacksonException;
import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.exception.custom.EntityAlreadyExistsException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

import org.hibernate.HibernateException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public final class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException exception) {
        return ErrorResponse.create(exception, HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ErrorResponse handleEntityNotFoundException(EntityNotFoundException exception) {
        return ErrorResponse.create(exception, HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ErrorResponse handleUsernameNotFoundException(UsernameNotFoundException exception) {
        return ErrorResponse.create(exception, HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ErrorResponse handleEntityAlreadyExistsException(EntityAlreadyExistsException exception) {
        return ErrorResponse.create(exception, HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(HibernateException.class)
    public ErrorResponse handleHibernateException(HibernateException exception) {
        return ErrorResponse.create(exception, HttpStatus.BAD_REQUEST, "Database error. Please try later.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        return ErrorResponse.create(exception, HttpStatus.BAD_REQUEST, ExceptionMessage.INVALID_DATA_ID.toString());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ErrorResponse handleExpiredJwtException(ExpiredJwtException exception) {
        return ErrorResponse.create(exception, HttpStatus.FORBIDDEN, ExceptionMessage.EXPIRED_AUTH.toString());
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ErrorResponse handleMalformedJwtException(MalformedJwtException exception) {
        return ErrorResponse.create(exception, HttpStatus.FORBIDDEN, ExceptionMessage.INVALID_AUTH_TOKEN.toString());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException exception) {
        return ErrorResponse.create(exception, HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(JacksonException.class)
    public ErrorResponse handleJacksonException(JacksonException exception) {
        return ErrorResponse.create(exception, HttpStatus.INTERNAL_SERVER_ERROR,
                                    ExceptionMessage.IMPOSSIBLE_TO_DESERIALIZE.toString());
    }
}
