package com.taskapproacher.entity.task.request;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;

import java.time.LocalDate;
import java.util.UUID;

import com.taskapproacher.interfaces.matcher.TaskMatcher;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TaskRequest implements TaskMatcher {
    private UUID ID;
    private String title;
    private String description;
    private String priority;
    private LocalDate deadline;
    private boolean finished;
    private TaskBoard taskBoard;

    public TaskRequest(Task task) {
        this.ID = task.getID();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.priority = task.getPriority();
        this.deadline = task.getDeadline();
        this.finished = task.isFinished();
        this.taskBoard = task.getTaskBoard();
    }
}
