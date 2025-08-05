package com.taskapproacher.service;

import com.taskapproacher.dao.TaskBoardDAO;
import com.taskapproacher.entity.TaskBoard;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskBoardService {
    private final TaskBoardDAO taskBoardDAO;

    @Autowired
    public TaskBoardService(TaskBoardDAO taskBoardDAO) {
        this.taskBoardDAO = taskBoardDAO;
    }

    public TaskBoard findById(Long id) {
            TaskBoard taskBoard = taskBoardDAO.findById(id);
        if (taskBoard == null) {
            throw new RuntimeException("Board is not found");
        }
        return taskBoard;
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
        taskBoardDAO.update(taskBoard);
        return taskBoard;
    }

    public void delete(Long id) {
        taskBoardDAO.delete(id);
    }
}
