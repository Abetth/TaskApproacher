package com.taskapproacher.service.task;

import com.taskapproacher.dao.task.TaskDAO;
import com.taskapproacher.entity.task.Task;

import com.taskapproacher.entity.task.TaskBoard;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
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
        return taskDAO.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
    }


    public Task create(Task task) {
        if (task.getTaskBoard() == null || taskBoardService.findById(task.getTaskBoard().getId()) == null) {
            throw new IllegalArgumentException("Task board not found");
        }

        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        TaskBoard boardForTask = taskBoardService.findById(task.getTaskBoard().getId());
        task.setTaskBoard(boardForTask);

        return taskDAO.save(task);
    }

    public Task update(UUID taskId, Task task) {
        if (taskDAO.findById(taskId).isEmpty()) {
            throw new RuntimeException("Entry is missing");
        }
        Task updatedTask = taskDAO.findById(taskId).get();
        updatedTask.setTitle(task.getTitle());
        updatedTask.setPriority(task.getPriority());
        updatedTask.setDeadline(task.getDeadline());
        updatedTask.setDescription(task.getDescription());
        updatedTask.setStatus(task.isStatus());
        updatedTask.setTaskBoard(task.getTaskBoard());

        return taskDAO.update(updatedTask);
    }

    public void delete(UUID taskId) {
        taskDAO.delete(taskId);
    }
}
