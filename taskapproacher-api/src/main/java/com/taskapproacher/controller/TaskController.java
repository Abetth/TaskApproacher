package com.taskapproacher.controller;

import com.taskapproacher.entity.Task;
import com.taskapproacher.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getById(@PathVariable Long taskId) {
        try {
            return ResponseEntity.ok(taskService.findById(taskId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task) {
        try {
            Task createTask = taskService.create(task);
            return ResponseEntity.status(201).body(createTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping
    public ResponseEntity<Task> update(@RequestBody Task task) {
        try {
            return ResponseEntity.ok(taskService.update(task));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Task> delete(@PathVariable Long taskId) {
        try {
            taskService.delete(taskId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
