package com.taskapproacher.service;

import com.taskapproacher.dao.TaskDAO;
import com.taskapproacher.entity.Task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskDAO taskDAO;
    private final TaskBoardService taskBoardService;

    @Autowired
    public TaskService(TaskDAO taskDAO, TaskBoardService taskBoardService) {
        this.taskDAO = taskDAO;
        this.taskBoardService = taskBoardService;
    }

    public Task findById(UUID taskId) {
        Task task = taskDAO.findById(taskId);
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
        if (Objects.isNull(taskDAO.findById(task.getId()))) {
            throw new RuntimeException("Entry is missing");
        }
        taskDAO.update(task);
        return task;
    }

    public void delete(UUID taskId) {
        taskDAO.delete(taskId);
    }
}
