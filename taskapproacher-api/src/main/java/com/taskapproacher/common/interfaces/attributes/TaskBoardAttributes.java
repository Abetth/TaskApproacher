package com.taskapproacher.common.interfaces.attributes;

import com.taskapproacher.task.model.Task;

import java.util.List;
import java.util.UUID;

public interface TaskBoardAttributes {
    UUID getID();
    String getTitle();
    boolean isSorted();
    List<Task> getTasks();
    UUID getUserID();
}
