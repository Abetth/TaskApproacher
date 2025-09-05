package com.taskapproacher.controller.task;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.service.task.TaskService;
import com.taskapproacher.entity.task.response.TaskResponse;
import com.taskapproacher.entity.task.request.TaskRequest;

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

    @PostMapping("/board/{boardId}")
    @PreAuthorize("@taskBoardService.findById(#boardId).user.id == authentication.principal.id")
    public ResponseEntity<TaskResponse> create(@PathVariable UUID boardId,
                                               @RequestBody TaskRequest task,
                                               @RequestHeader String timeZone) {
        try {
            TaskResponse createTask = taskService.create(boardId, task, timeZone);
            return ResponseEntity.status(201).body(createTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{taskId}")
    @PreAuthorize("@accessCheckService.hasAccessToTask(#taskId, authentication.principal.id)")
    public ResponseEntity<TaskResponse> update(@PathVariable UUID taskId,
                                       @RequestBody TaskRequest task,
                                       @RequestHeader String timeZone) {
        try {
            return ResponseEntity.ok(taskService.update(taskId, task, timeZone));
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
