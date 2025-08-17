package com.taskapproacher.service.task;

import com.taskapproacher.dao.task.TaskBoardDAO;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TaskBoardService {
    private final TaskBoardDAO taskBoardDAO;
    private final UserService userService;

    @Autowired
    public TaskBoardService(TaskBoardDAO taskBoardDAO, UserService userService) {
        this.taskBoardDAO = taskBoardDAO;
        this.userService = userService;
    }

    public TaskBoardResponse findById(UUID boardId) {
        TaskBoard taskBoard = taskBoardDAO.findById(boardId);
        if (taskBoard == null) {
            throw new RuntimeException("Board is not found");
        }
        return new TaskBoardResponse(taskBoard);
    }

    public List<Task> findByTaskBoard(UUID boardId) {
        if (taskBoardDAO.findById(boardId) == null) {
            throw new EntityNotFoundException("Task board not found");
        }
        return taskBoardDAO.findRelatedEntitiesByUUID(boardId);
    }

    public TaskBoardResponse create(TaskBoard taskBoard) {
        if (taskBoard.getTitle() == null || taskBoard.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        taskBoard.setUser(userService.findById(taskBoard.getUser().getId()));

        taskBoardDAO.save(taskBoard);
        return new TaskBoardResponse(taskBoard);
    }

    public TaskBoardResponse update(TaskBoard taskBoard) {
        if (Objects.isNull(taskBoardDAO.findById(taskBoard.getId()))) {
            throw new EntityNotFoundException("Database entry is missing");
        }
        taskBoard.setTasks(taskBoardDAO.findById(taskBoard.getId()).getTasks());
        taskBoard.setUser(userService.findById(taskBoard.getUser().getId()));

        taskBoardDAO.update(taskBoard);
        return new TaskBoardResponse(taskBoard);
    }

    public void delete(UUID boardId) {
        taskBoardDAO.delete(boardId);
    }
}
