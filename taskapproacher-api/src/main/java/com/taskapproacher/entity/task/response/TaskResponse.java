package com.taskapproacher.entity.task.response;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.interfaces.TaskMatcher;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TaskResponse implements TaskMatcher {
    private UUID ID;

    private String title;

    private String description;

    private String priority;

    private LocalDate deadline;

    private boolean finished;

    private TaskBoard taskBoard;

    public TaskResponse(Task task) {
        this.ID = task.getID();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.priority = task.getPriority();
        this.deadline = task.getDeadline();
        this.finished = task.isFinished();
        this.taskBoard = task.getTaskBoard();
    }

}
