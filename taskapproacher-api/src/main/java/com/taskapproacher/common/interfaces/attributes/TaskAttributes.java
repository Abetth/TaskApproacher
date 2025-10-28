package com.taskapproacher.common.interfaces.attributes;

import com.taskapproacher.task.constant.Priority;

import java.time.LocalDate;
import java.util.UUID;

public interface TaskAttributes {
    UUID getID();
    String getTitle();
    String getDescription();
    Priority getPriority();
    LocalDate getDeadline();
    boolean isFinished();
    UUID getTaskBoardID();
}
