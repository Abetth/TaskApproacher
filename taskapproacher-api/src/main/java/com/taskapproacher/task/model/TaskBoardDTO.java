package com.taskapproacher.task.model;

import com.taskapproacher.common.interfaces.matcher.TaskBoardMatcher;

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
public class TaskBoardDTO implements TaskBoardMatcher {
    private UUID ID;
    private String title;
    private boolean sorted;
    private List<Task> tasks;
    private UUID userID;
}
