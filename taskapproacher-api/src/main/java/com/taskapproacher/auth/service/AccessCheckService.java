package com.taskapproacher.auth.service;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.task.repository.TaskBoardRepository;
import com.taskapproacher.task.repository.TaskRepository;
import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskBoard;

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
