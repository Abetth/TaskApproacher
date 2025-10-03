package com.taskapproacher.auth.service;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskBoard;

import com.taskapproacher.task.service.TaskBoardService;
import com.taskapproacher.task.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccessCheckService {
    private final TaskBoardService taskBoardService;
    private final TaskService taskService;

    @Autowired
    public AccessCheckService(TaskBoardService taskBoardService, TaskService taskService) {
        this.taskBoardService = taskBoardService;
        this.taskService = taskService;
    }

    public boolean hasAccessToBoard(UUID boardID, UUID principalID) throws IllegalArgumentException {
        if (boardID == null || principalID == null) {
            String wrongValue = (boardID == null) ? "Task board id " : "Principal id ";

            throw new IllegalArgumentException(wrongValue + ExceptionMessage.NULL);
        }

        TaskBoard foundBoard = taskBoardService.findByID(boardID);

        return foundBoard.getUser().getID().equals(principalID);
    }

    public boolean hasAccessToTask(UUID taskID, UUID principalID) {
        if (taskID == null || principalID == null) {
            String wrongValue = (taskID == null) ? "Task id " : "Principal id ";

            throw new IllegalArgumentException(wrongValue + ExceptionMessage.NULL);
        }

        Task task = taskService.findByID(taskID);

        return task.getTaskBoard().getUser().getID().equals(principalID);
    }
}
