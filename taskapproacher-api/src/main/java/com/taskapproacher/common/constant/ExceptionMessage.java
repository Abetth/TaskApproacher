package com.taskapproacher.common.constant;

import com.taskapproacher.config.password.PasswordConstants;

public enum ExceptionMessage {
    NOT_FOUND("not found"),
    NULL("can't be null"),
    EMPTY("can't be empty"),
    WRONG_FORMAT("wrong format"),
    ALREADY_EXISTS("already exists"),
    BEFORE_CURRENT_DATE("can't be less than the current date"),
    CREATION_FAILURE("creation failure"),
    INVALID_DATA_ID("Entity data or ID is invalid"),
    EXPIRED_AUTH("Authentication is expired"),
    INVALID_AUTH_TOKEN("Authentication token is invalid"),
    INVALID_USER_DATA("Invalid user data"),
    ACCESS_DENIED("Access denied"),
    INVALID_PASSWORD_LENGTH("Password is too short, minimum length is " + PasswordConstants.MIN_LENGTH),
    INVALID_USERNAME_LENGTH("Username should be from 3 to 32 characters long");

    private final String message;

    ExceptionMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
