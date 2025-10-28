package com.taskapproacher.common.constant;

import com.taskapproacher.config.password.PasswordConstants;
import com.taskapproacher.task.constant.TaskConstants;
import com.taskapproacher.user.constant.UserConstants;

public enum ExceptionMessage {
    NOT_FOUND("not found"),
    NULL("can't be null"),
    EMPTY("can't be empty"),
    WRONG_FORMAT("wrong format"),
    ALREADY_EXISTS("already exists"),
    BEFORE_CURRENT_DATE("can't be less than the current date"),
    INVALID_DATA_ID("Entity data or ID is invalid"),
    EXPIRED_AUTH("Authentication is expired"),
    INVALID_AUTH_TOKEN("Authentication token is invalid"),
    INVALID_USER_DATA("Invalid user data"),
    ACCESS_DENIED("Access denied"),
    INVALID_PASSWORD_LENGTH("Password is too short, minimum length is " + PasswordConstants.MIN_LENGTH),
    INVALID_USERNAME_LENGTH("Username should be from " + UserConstants.MIN_USERNAME_LENGTH
                            + " to " + UserConstants.MAX_USERNAME_LENGTH + " characters long"),
    INVALID_TASK_FIELDS_LENGTH("Please check your input values, the title and description of the task should not"
                               + " exceed " + TaskConstants.MAX_TASK_TITLE_LENGTH
                               + " and " + TaskConstants.MAX_TASK_DESCRIPTION_LENGTH
                               + " characters, respectively"),
    INVALID_TASK_BOARD_TITLE_LENGTH("Task board title is too long, maximum length is "
                                    + TaskConstants.MAX_TASK_BOARD_TITLE_LENGTH),
    IMPOSSIBLE_TO_DESERIALIZE("Impossible to deserialize data");

    private final String message;

    ExceptionMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
