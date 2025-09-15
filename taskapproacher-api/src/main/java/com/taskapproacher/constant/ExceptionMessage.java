package com.taskapproacher.constant;

public enum ExceptionMessage {
    NOT_FOUND("not found"),
    NULL("can't be null"),
    EMPTY("can't be empty"),
    WRONG_FORMAT("wrong format"),
    ALREADY_EXISTS("already exists"),
    BEFORE_CURRENT_DATE("can't be less than the current date"),
    CREATION_FAILURE("creation failure"),
    INVALID_DATA("Data is invalid"),
    EXPIRED_AUTH("Authentication is expired"),
    INVALID_AUTH_TOKEN("Authentication token is invalid"),
    ACCESS_DENIED("Access denied");

    private final String message;

    ExceptionMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
