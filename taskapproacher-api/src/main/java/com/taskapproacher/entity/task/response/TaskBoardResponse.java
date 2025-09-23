package com.taskapproacher.entity.task.response;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.user.response.UserResponse;
import com.taskapproacher.interfaces.matcher.TaskBoardMatcher;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskBoardResponse implements TaskBoardMatcher {
    private UUID ID;
    private String title;
    private boolean sorted;
    private List<Task> tasks;
    private UserResponse user;


    public TaskBoardResponse(TaskBoard taskBoard) {
        this.ID = taskBoard.getID();
        this.title = taskBoard.getTitle();
        this.sorted = taskBoard.isSorted();
        this.tasks = taskBoard.getTasks();
        this.user = new UserResponse(taskBoard.getUser());
    }

    @Override
    public User getUser() {
        return new User(this.user);
    }
}
