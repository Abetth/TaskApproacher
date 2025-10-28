package com.taskapproacher.task.mapper;

import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskDTO;

public class TaskMapper {
    public TaskDTO mapToTaskDTO(Task task) {
        return new TaskDTO(task.getID(), task.getTitle(), task.getDescription(), task.getPriority(),
                           task.getDeadline(), task.isFinished(), task.getTaskBoardID());
    }

    public Task mapToTaskEntity(TaskDTO dto) {
        return new Task(dto.getID(), dto.getTitle(), dto.getDescription(),
                        dto.getPriority(), dto.getDeadline(), dto.isFinished());
    }
}
