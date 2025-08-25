package com.taskapproacher.service.task;

import com.taskapproacher.dao.task.TaskDAO;
import com.taskapproacher.entity.task.Task;

import com.taskapproacher.entity.task.TaskBoard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        TaskBoard boardForTask = taskBoardService.findById(task.getTaskBoard().getId());
        task.setTaskBoard(boardForTask);

        taskDAO.save(task);
        return task;
    }

    public Task update(UUID taskId, Task task) {
        if (Objects.isNull(taskDAO.findById(taskId))) {
            throw new RuntimeException("Entry is missing");
        }
        Task updatedTask = taskDAO.findById(taskId);
        updatedTask.setTitle(task.getTitle());
        updatedTask.setPriority(task.getPriority());
        updatedTask.setDeadline(task.getDeadline());
        updatedTask.setDescription(task.getDescription());
        updatedTask.setStatus(task.isStatus());
        updatedTask.setTaskBoard(task.getTaskBoard());

        taskDAO.update(updatedTask);
        return updatedTask;
    }

    public void delete(UUID taskId) {
        taskDAO.delete(taskId);
    }
}
