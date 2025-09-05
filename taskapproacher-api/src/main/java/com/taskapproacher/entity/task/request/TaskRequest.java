package com.taskapproacher.entity.task.request;

import com.taskapproacher.entity.task.TaskBoard;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TaskRequest {
    private UUID ID;

    private String title;

    private String description;

    private String priority;

    private LocalDate deadline;

    private boolean finished;

    private TaskBoard taskBoard;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskRequest comparable = (TaskRequest) o;

        return title.equals(comparable.title)
                && description.equals(comparable.description) && priority.equals(comparable.priority)
                && deadline.equals(comparable.deadline) && finished == comparable.finished
                && taskBoard.equals(comparable.taskBoard);

    }
}
