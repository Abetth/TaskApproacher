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
import java.util.Optional;
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

    public TaskBoard findById(UUID boardId) {
        return taskBoardDAO.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board is not found"));
    }

    public List<Task> findByTaskBoard(UUID boardId) {
        if (taskBoardDAO.findById(boardId).isEmpty()) {
            throw new EntityNotFoundException("Task board not found");
        }
        return taskBoardDAO.findRelatedEntitiesByUUID(boardId);
    }

    public TaskBoardResponse create(TaskBoard taskBoard) {
        if (taskBoard.getTitle() == null || taskBoard.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        taskBoard.setUser(userService.findById(taskBoard.getUser().getId()));

        return new TaskBoardResponse(taskBoardDAO.save(taskBoard));
    }

    public TaskBoardResponse update(UUID boardId, TaskBoard taskBoard) {
        if (taskBoardDAO.findById(boardId).isEmpty()) {
            throw new EntityNotFoundException("Database entry is missing");
        }
        TaskBoard updatedBoard = taskBoardDAO.findById(boardId).get();
        updatedBoard.setTitle(taskBoard.getTitle());
        updatedBoard.setSorted(taskBoard.isSorted());

        return new TaskBoardResponse(taskBoardDAO.update(updatedBoard));
    }

    public void delete(UUID boardId) {
        taskBoardDAO.delete(boardId);
    }
}
