package com.taskapproacher.entity.task.request;

import com.taskapproacher.entity.task.TaskBoard;

import java.time.LocalDate;
import java.util.UUID;

import com.taskapproacher.interfaces.TaskMatcher;
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
}
