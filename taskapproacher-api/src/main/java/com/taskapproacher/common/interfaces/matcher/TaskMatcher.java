package com.taskapproacher.common.interfaces.matcher;

import com.taskapproacher.task.model.TaskBoard;

import java.time.LocalDate;
import java.util.UUID;

public interface TaskMatcher {
    UUID getID();
    String getTitle();
    String getDescription();
    String getPriority();
    LocalDate getDeadline();
    boolean isFinished();
    TaskBoard getTaskBoard();
}
