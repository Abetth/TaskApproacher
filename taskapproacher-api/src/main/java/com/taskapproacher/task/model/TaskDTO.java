package com.taskapproacher.task.model;

import com.taskapproacher.common.interfaces.attributes.TaskAttributes;
import com.taskapproacher.task.constant.Priority;

import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
public class TaskDTO implements TaskAttributes {
    UUID ID;
    String title;
    String description;
    Priority priority;
    LocalDate deadline;
    boolean finished;
    UUID taskBoardID;
}
