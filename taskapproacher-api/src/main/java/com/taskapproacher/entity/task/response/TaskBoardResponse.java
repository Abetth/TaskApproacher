package com.taskapproacher.entity.task.response;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
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
    private UUID id;
    private String title;
    private boolean sorted;
    private List<Task> tasks;
    private UserResponse user;


    public TaskBoardResponse(TaskBoard taskBoard) {
        this.id = taskBoard.getId();
        this.title = taskBoard.getTitle();
        this.sorted = taskBoard.isSorted();
        this.tasks = taskBoard.getTasks();
        this.user = new UserResponse(taskBoard.getUser());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskBoardResponse comparable = (TaskBoardResponse) o;

        return title.equals(comparable.title) && sorted == comparable.sorted && tasks.equals(comparable.tasks) && user.equals(comparable.user);
    }
}
