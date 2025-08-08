package com.taskapproacher.service;

import com.taskapproacher.dao.TaskBoardDAO;
import com.taskapproacher.entity.Task;
import com.taskapproacher.entity.TaskBoard;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TaskBoardService {
    private final TaskBoardDAO taskBoardDAO;

    @Autowired
    public TaskBoardService(TaskBoardDAO taskBoardDAO) {
        this.taskBoardDAO = taskBoardDAO;
    }

    public TaskBoard findById(UUID boardId) {
            TaskBoard taskBoard = taskBoardDAO.findById(boardId);
        if (taskBoard == null) {
            throw new RuntimeException("Board is not found");
        }
        return taskBoard;
    }

    public List<Task> findByTaskBoard(UUID boardId) {
        if (taskBoardDAO.findById(boardId) == null) {
            throw new RuntimeException("Task board not found");
        }
        return taskBoardDAO.findRelatedEntitiesByUUID(boardId);
    }

    public TaskBoard create(TaskBoard taskBoard) {
        if (taskBoard.getTitle() == null || taskBoard.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        taskBoardDAO.save(taskBoard);
        return taskBoard;
    }

    public List<TaskBoard> findAll() {
        return taskBoardDAO.findAll();
    }

    public TaskBoard update(TaskBoard taskBoard) {
        if (Objects.isNull(taskBoardDAO.findById(taskBoard.getId()))) {
            throw new EntityNotFoundException("Database entry is missing");
        }
        taskBoard.setTasks(taskBoardDAO.findById(taskBoard.getId()).getTasks());

        taskBoardDAO.update(taskBoard);
        return taskBoard;
    }

    public void delete(UUID boardId) {
        taskBoardDAO.delete(boardId);
    }
}
