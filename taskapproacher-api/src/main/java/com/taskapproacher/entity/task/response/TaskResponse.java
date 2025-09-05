package com.taskapproacher.entity.task.response;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import lombok.Setter;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class TaskResponse {
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
        this.priority = task.getPriority().toString();
        this.deadline = task.getDeadline();
        this.finished = task.isFinished();
        this.taskBoard = task.getTaskBoard();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskResponse comparable = (TaskResponse) o;

        return title.equals(comparable.title)
                && description.equals(comparable.description) && priority.equals(comparable.priority)
                && deadline.equals(comparable.deadline) && finished == comparable.finished
                && taskBoard.equals(comparable.taskBoard);

    }
}
