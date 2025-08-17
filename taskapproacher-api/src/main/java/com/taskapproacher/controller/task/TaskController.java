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

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Task> getById(@PathVariable UUID taskId) {
        try {
            return ResponseEntity.ok(taskService.findById(taskId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Task> create(@RequestBody Task task) {
        try {
            Task createTask = taskService.create(task);
            return ResponseEntity.status(201).body(createTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Task> update(@RequestBody Task task) {
        try {
            return ResponseEntity.ok(taskService.update(task));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Task> delete(@PathVariable UUID taskId) {
        try {
            taskService.delete(taskId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
