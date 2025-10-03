package com.taskapproacher.task.mapper;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskDTO;

public class TaskMapper {
    public TaskDTO mapToTaskDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setID(task.getID());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriorityAsString());
        dto.setDeadline(task.getDeadline());
        dto.setFinished(task.isFinished());
        dto.setTaskBoardID(task.getTaskBoardID());

        return dto;
    }

    public Task mapToTaskEntity(TaskDTO dto) {
        Task task = new Task();
        task.setID(dto.getID());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDeadline(dto.getDeadline());
        task.setFinished(dto.isFinished());
        task.setPriority(dto.getPriorityAsString());

        return task;
    }
}
