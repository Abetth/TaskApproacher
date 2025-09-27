package com.taskapproacher.service.security.access;

import com.taskapproacher.constant.ExceptionMessage;
import com.taskapproacher.repository.task.TaskBoardRepository;
import com.taskapproacher.repository.task.TaskRepository;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccessCheckService {
    private final TaskBoardRepository taskBoardRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public AccessCheckService(TaskBoardRepository taskBoardRepository, TaskRepository taskRepository) {
        this.taskBoardRepository = taskBoardRepository;
        this.taskRepository = taskRepository;
    }

    public boolean hasAccessToBoard(UUID boardID, UUID principalID) throws IllegalArgumentException {
        if (boardID == null || principalID == null) {
            String wrongValue = (boardID == null) ? "Task board id " : "Principal id ";

            throw new IllegalArgumentException(wrongValue + ExceptionMessage.NULL);
        }

        TaskBoard foundBoard = taskBoardRepository.findByID(boardID)
                                                  .orElseThrow(() -> new EntityNotFoundException("Task board " + ExceptionMessage.NOT_FOUND));

        return foundBoard.getUser().getID().equals(principalID);

    }

    public boolean hasAccessToTask(UUID taskID, UUID principalID) {
        if (taskID == null || principalID == null) {
            String wrongValue = (taskID == null) ? "Task id " : "Principal id ";

            throw new IllegalArgumentException(wrongValue + ExceptionMessage.NULL);
        }

        Task task = taskRepository.findByID(taskID)
                                  .orElseThrow(() -> new EntityNotFoundException("Task " + ExceptionMessage.NOT_FOUND));

        return task.getTaskBoard().getUser().getID().equals(principalID);
    }
}
