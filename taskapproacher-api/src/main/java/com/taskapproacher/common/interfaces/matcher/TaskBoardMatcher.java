package com.taskapproacher.common.interfaces.matcher;

import com.taskapproacher.task.model.Task;
import com.taskapproacher.user.model.User;

import java.util.List;
import java.util.UUID;

public interface TaskBoardMatcher {
    UUID getID();
    String getTitle();
    boolean isSorted();
    List<Task> getTasks();
    UUID getUserID();
}
