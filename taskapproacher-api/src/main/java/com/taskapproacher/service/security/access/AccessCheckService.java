package com.taskapproacher.service.security.access;

import com.taskapproacher.dao.task.TaskBoardDAO;
import com.taskapproacher.dao.task.TaskDAO;
import com.taskapproacher.entity.task.TaskBoard;
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

    public boolean hasAccessToBoard(UUID boardId, UUID principalId) {
        return taskBoardDAO.findById(boardId).get().getUser().getId().equals(principalId);

    }

    public boolean hasAccessToTask(UUID taskId, UUID principalId) {
        return taskDAO.findById(taskId).get().getTaskBoard().getUser().getId().equals(principalId);
    }
}
