package com.taskapproacher.entity.task;

import com.taskapproacher.entity.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskBoardResponse {
    private UUID ID;
    private String title;
    private boolean isSorted;
    private List<Task> tasks;
    private UserResponse user;


    public TaskBoardResponse(TaskBoard taskBoard) {
        this.ID = taskBoard.getID();
        this.title = taskBoard.getTitle();
        this.isSorted = taskBoard.isSorted();
        this.tasks = taskBoard.getTasks();
        this.user = new UserResponse(taskBoard.getUser());
    }
}
