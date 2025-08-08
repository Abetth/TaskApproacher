package com.taskapproacher.service;

import com.taskapproacher.dao.TaskDAO;
import com.taskapproacher.entity.Task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final TaskDAO taskDAO;
    private final TaskBoardService taskBoardService;

    @Autowired
    public TaskService(TaskDAO taskDAO, TaskBoardService taskBoardService) {
        this.taskDAO = taskDAO;
        this.taskBoardService = taskBoardService;
    }

    public Task findById(Long id) {
        Task task = taskDAO.findById(id);
        if (task == null) {
            throw new RuntimeException("Task not found");
        }
        return task;
    }


    public Task create(Task task) {
        if (task.getTaskBoard() == null || taskBoardService.findById(task.getTaskBoard().getId()) == null) {
            throw new IllegalArgumentException("Task board not found");
        }

        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        taskDAO.save(task);
        return task;
    }

    public Task update(Task task) {
        taskDAO.update(task);
        return task;
    }

    public void delete(Long id) {
        taskDAO.delete(id);
    }
}
