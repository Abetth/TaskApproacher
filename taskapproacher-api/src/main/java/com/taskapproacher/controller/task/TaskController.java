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

    @PostMapping("/board/{boardID}")
    @PreAuthorize("@taskBoardService.findByID(#boardID).user.ID == authentication.principal.ID")
    public ResponseEntity<TaskResponse> create(@PathVariable UUID boardID,
                                               @RequestBody TaskRequest task,
                                               @RequestHeader String timeZone) {
        TaskResponse createTask = taskService.create(boardID, task, timeZone);
        return ResponseEntity.status(201).body(createTask);
    }

    @PatchMapping("/{taskID}")
    @PreAuthorize("@accessCheckService.hasAccessToTask(#taskID, authentication.principal.ID)")
    public ResponseEntity<TaskResponse> update(@PathVariable UUID taskID,
                                               @RequestBody TaskRequest task,
                                               @RequestHeader String timeZone) {
        return ResponseEntity.ok(taskService.update(taskID, task, timeZone));
    }

    @DeleteMapping("/{taskID}")
    @PreAuthorize("@accessCheckService.hasAccessToTask(#taskID, authentication.principal.ID)")
    public ResponseEntity<Task> delete(@PathVariable UUID taskID) {
        taskService.delete(taskID);
        return ResponseEntity.noContent().build();
    }

}
