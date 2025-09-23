package com.taskapproacher.interfaces.matcher;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.user.User;

import java.util.List;
import java.util.UUID;

public interface TaskBoardMatcher {
    UUID getID();
    String getTitle();
    boolean isSorted();
    List<Task> getTasks();
    User getUser();
}
