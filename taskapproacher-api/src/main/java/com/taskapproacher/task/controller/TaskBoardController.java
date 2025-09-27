package com.taskapproacher.task.controller;

import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.model.TaskBoardResponse;
import com.taskapproacher.task.model.TaskResponse;
import com.taskapproacher.task.service.TaskBoardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/board")
public class TaskBoardController {
    private final TaskBoardService taskBoardService;

    @Autowired
    public TaskBoardController(TaskBoardService taskBoardService) {
        this.taskBoardService = taskBoardService;
    }

    @GetMapping("/{boardID}/tasks")
    @PreAuthorize("@accessCheckService.hasAccessToBoard(#boardID, authentication.principal.ID)")
    public ResponseEntity<List<TaskResponse>> getTasksByBoard(@PathVariable UUID boardID) {
        return ResponseEntity.ok(taskBoardService.findByTaskBoard(boardID));
    }

    @PostMapping("/{userID}")
    @PreAuthorize("#userID == authentication.principal.ID")
    public ResponseEntity<TaskBoardResponse> create(@PathVariable UUID userID, @RequestBody TaskBoard board) {
        TaskBoardResponse createBoard = taskBoardService.create(userID, board);
        return ResponseEntity.status(201).body(createBoard);
    }

    @PatchMapping("/{boardID}")
    @PreAuthorize("@accessCheckService.hasAccessToBoard(#boardID, authentication.principal.ID)")
    public ResponseEntity<TaskBoardResponse> update(@PathVariable UUID boardID, @RequestBody TaskBoard board) {
        return ResponseEntity.ok(taskBoardService.update(boardID, board));
    }

    @DeleteMapping("/{boardID}")
    @PreAuthorize("@accessCheckService.hasAccessToBoard(#boardID, authentication.principal.ID)")
    public ResponseEntity<TaskBoardResponse> delete(@PathVariable UUID boardID) {
        taskBoardService.delete(boardID);
        return ResponseEntity.noContent().build();
    }
}
