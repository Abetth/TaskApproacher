package com.taskapproacher.interfaces.matcher;

import com.taskapproacher.entity.task.TaskBoard;

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
