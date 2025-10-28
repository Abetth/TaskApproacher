package com.taskapproacher.task.model;

import com.taskapproacher.common.interfaces.attributes.TaskBoardAttributes;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Value
public class TaskBoardDTO implements TaskBoardAttributes {
    UUID ID;
    String title;
    boolean sorted;
    List<Task> tasks;
    UUID userID;
}
