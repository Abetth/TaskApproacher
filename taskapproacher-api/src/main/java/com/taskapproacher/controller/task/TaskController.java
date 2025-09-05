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
    @PreAuthorize("@taskBoardService.findByID(#task.taskBoard.ID).user.ID == authentication.principal.id")
    public ResponseEntity<Task> create(@RequestBody Task task) {
        try {
            Task createTask = taskService.create(task);
            return ResponseEntity.status(201).body(createTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{taskID}")
    @PreAuthorize("@accessCheckService.hasAccessToTask(#taskID, authentication.principal.id)")
    public ResponseEntity<Task> update(@PathVariable UUID taskID, @RequestBody Task task) {
        try {
            return ResponseEntity.ok(taskService.update(taskID, task));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{taskID}")
    @PreAuthorize("@accessCheckService.hasAccessToTask(#taskID, authentication.principal.id)")
    public ResponseEntity<Task> delete(@PathVariable UUID taskID) {
        try {
            taskService.delete(taskID);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
