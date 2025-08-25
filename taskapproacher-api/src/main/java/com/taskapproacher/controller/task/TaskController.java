package com.taskapproacher.controller.task;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.service.task.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("@taskBoardService.findById(#task.taskBoard.id).user.id == authentication.principal.id")
    public ResponseEntity<Task> create(@RequestBody Task task) {
        try {
            Task createTask = taskService.create(task);
            return ResponseEntity.status(201).body(createTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{taskId}")
    @PreAuthorize("@accessCheckService.hasAccessToTask(#taskId, authentication.principal.id)")
    public ResponseEntity<Task> update(@PathVariable UUID taskId, @RequestBody Task task) {
        try {
            return ResponseEntity.ok(taskService.update(taskId, task));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("@accessCheckService.hasAccessToTask(#taskId, authentication.principal.id)")
    public ResponseEntity<Task> delete(@PathVariable UUID taskId) {
        try {
            taskService.delete(taskId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
