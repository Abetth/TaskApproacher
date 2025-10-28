package com.taskapproacher.task.controller;

import com.taskapproacher.task.model.TaskDTO;
import com.taskapproacher.task.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/task")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/board/{boardID}")
    @PreAuthorize("@taskBoardService.findByID(#boardID).user.ID == authentication.principal.ID")
    public ResponseEntity<TaskDTO> createTask(@PathVariable UUID boardID,
                                                   @RequestBody TaskDTO task,
                                                   @RequestHeader String timeZone) {
        return ResponseEntity.status(201).body(taskService.createTask(boardID, task, timeZone));
    }

    @PatchMapping("/{taskID}")
    @PreAuthorize("@accessCheckService.hasAccessToTask(#taskID, authentication.principal.ID)")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable UUID taskID,
                                                   @RequestBody TaskDTO task,
                                                   @RequestHeader String timeZone) {
        return ResponseEntity.ok(taskService.updateTask(taskID, task, timeZone));
    }

    @DeleteMapping("/{taskID}")
    @PreAuthorize("@accessCheckService.hasAccessToTask(#taskID, authentication.principal.ID)")
    public ResponseEntity<TaskDTO> deleteTask(@PathVariable UUID taskID) {
        taskService.deleteTask(taskID);
        return ResponseEntity.noContent().build();
    }

}
