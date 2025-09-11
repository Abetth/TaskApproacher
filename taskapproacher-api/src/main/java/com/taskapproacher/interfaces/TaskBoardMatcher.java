package com.taskapproacher.interfaces;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.user.User;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

public interface TaskBoardMatcher {
    UUID getID();
    String getTitle();
    boolean isSorted();
    List<Task> getTasks();
    User getUser();
}
