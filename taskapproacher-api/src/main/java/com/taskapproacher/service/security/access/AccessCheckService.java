package com.taskapproacher.service.security.access;

import com.taskapproacher.dao.task.TaskBoardDAO;
import com.taskapproacher.dao.task.TaskDAO;
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

    public boolean hasAccessToBoard(UUID boardId, UUID principalId) {
        return taskBoardDAO.findByID(boardId).getUser().getID().equals(principalId);
    }

    public boolean hasAccessToTask(UUID taskId, UUID principalId) {
        return taskDAO.findByID(taskId).getTaskBoard().getUser().getID().equals(principalId);
    }
}
