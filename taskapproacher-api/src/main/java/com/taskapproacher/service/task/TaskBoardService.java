package com.taskapproacher.service.task;

import com.taskapproacher.dao.task.TaskBoardDAO;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.TaskBoardResponse;
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

    public TaskBoard findByID(UUID boardID) {
        TaskBoard taskBoard = taskBoardDAO.findByID(boardID);
        if (taskBoard == null) {
            throw new RuntimeException("Board is not found");
        }
        return taskBoard;
    }

    public List<Task> findByTaskBoard(UUID boardID) {
        if (taskBoardDAO.findByID(boardID) == null) {
            throw new EntityNotFoundException("Task board not found");
        }
        return taskBoardDAO.findRelatedEntitiesByUUID(boardID);
    }

    public TaskBoardResponse create(TaskBoard taskBoard) {
        if (taskBoard.getTitle() == null || taskBoard.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        taskBoard.setUser(userService.findByID(taskBoard.getUser().getID()));

        taskBoardDAO.save(taskBoard);
        return new TaskBoardResponse(taskBoard);
    }

    public TaskBoardResponse update(UUID boardID, TaskBoard taskBoard) {
        if (Objects.isNull(taskBoardDAO.findByID(boardID))) {
            throw new EntityNotFoundException("Database entry is missing");
        }
        TaskBoard updatedBoard = taskBoardDAO.findByID(boardID);
        updatedBoard.setTitle(taskBoard.getTitle());
        updatedBoard.setSorted(taskBoard.isSorted());

        taskBoardDAO.update(updatedBoard);
        return new TaskBoardResponse(updatedBoard);
    }

    public void delete(UUID boardID) {
        taskBoardDAO.delete(boardID);
    }
}
