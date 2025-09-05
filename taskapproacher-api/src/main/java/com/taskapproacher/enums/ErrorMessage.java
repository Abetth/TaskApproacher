package com.taskapproacher.enums;

public enum ErrorMessage {
    NOT_FOUND("not found"),
    NULL("can't be null"),
    EMPTY("can't be empty"),
    NULL_OR_EMPTY("can't be null or empty"),
    WRONG_FORMAT("wrong format"),
    ALREADY_EXISTS("already exists"),
    BEFORE_CURRENT_DATE("can't be less than the current date"),
    CREATION_FAILURE("creation failure");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
