package com.taskapproacher.service.security.access;

import com.taskapproacher.dao.task.TaskBoardDAO;
import com.taskapproacher.dao.task.TaskDAO;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.constant.ExceptionMessage;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccessCheckService {
    private final TaskBoardDAO taskBoardDAO;
    private final TaskDAO taskDAO;

    @Autowired
    public AccessCheckService(TaskBoardDAO taskBoardDAO, TaskDAO taskDAO) {
        this.taskBoardDAO = taskBoardDAO;
        this.taskDAO = taskDAO;
    }

    public boolean hasAccessToBoard(UUID boardID, UUID principalID) throws IllegalArgumentException {
        if (boardID == null || principalID == null) {
            String wrongValue = (boardID == null) ? "Task board id " : "Principal id ";

            throw new IllegalArgumentException(wrongValue + ExceptionMessage.NULL);
        }

        TaskBoard foundBoard = taskBoardDAO.findByID(boardID)
                .orElseThrow(() -> new EntityNotFoundException("Task board " + ExceptionMessage.NOT_FOUND));

        return foundBoard.getUser().getID().equals(principalID);

    }

    public boolean hasAccessToTask(UUID taskID, UUID principalID) {
        if (taskID == null || principalID == null) {
            String wrongValue = (taskID == null) ? "Task id " : "Principal id ";

            throw new IllegalArgumentException(wrongValue + ExceptionMessage.NULL);
        }

        Task task = taskDAO.findByID(taskID)
                .orElseThrow(() -> new EntityNotFoundException("Task " + ExceptionMessage.NOT_FOUND));

        return task.getTaskBoard().getUser().getID().equals(principalID);
    }
}
