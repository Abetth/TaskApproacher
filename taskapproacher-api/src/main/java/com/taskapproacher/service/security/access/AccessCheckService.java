package com.taskapproacher.service.security.access;

import com.taskapproacher.dao.task.TaskBoardDAO;
import com.taskapproacher.dao.task.TaskDAO;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.enums.ErrorMessage;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.stream.Stream;

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

    public boolean hasAccessToBoard(UUID boardId, UUID principalId) throws IllegalArgumentException {
        if (boardId == null || principalId == null) {
            String wrongValue = (boardId == null) ? "Task board id " : "Principal id ";

            throw new IllegalArgumentException(wrongValue + ErrorMessage.NULL);
        }

        TaskBoard foundBoard = taskBoardDAO.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Task board " + ErrorMessage.NOT_FOUND));

        return foundBoard.getUser().getId().equals(principalId);

    }

    public boolean hasAccessToTask(UUID taskId, UUID principalId) {
        if (taskId == null || principalId == null) {
            String wrongValue = (taskId == null) ? "Task id " : "Principal id ";

            throw new IllegalArgumentException(wrongValue + ErrorMessage.NULL);
        }

        Task task = taskDAO.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task " + ErrorMessage.NOT_FOUND));

        return task.getTaskBoard().getUser().getId().equals(principalId);
    }
}
