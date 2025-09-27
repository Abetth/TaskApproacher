package com.taskapproacher.task.model;

import com.taskapproacher.common.interfaces.matcher.TaskMatcher;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

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
