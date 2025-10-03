package com.taskapproacher.task.controller;

import com.taskapproacher.task.model.TaskBoardDTO;
import com.taskapproacher.task.model.TaskDTO;
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
    public ResponseEntity<List<TaskDTO>> getTasksByBoard(@PathVariable UUID boardID) {
        return ResponseEntity.ok(taskBoardService.findByTaskBoard(boardID));
    }

    @PostMapping("/{userID}")
    @PreAuthorize("#userID == authentication.principal.ID")
    public ResponseEntity<TaskBoardDTO> createTaskBoard(@PathVariable UUID userID, @RequestBody TaskBoardDTO board) {
        TaskBoardDTO createBoard = taskBoardService.createTaskBoard(userID, board);
        return ResponseEntity.status(201).body(createBoard);
    }

    @PatchMapping("/{boardID}")
    @PreAuthorize("@accessCheckService.hasAccessToBoard(#boardID, authentication.principal.ID)")
    public ResponseEntity<TaskBoardDTO> updateTaskBoard(@PathVariable UUID boardID, @RequestBody TaskBoardDTO board) {
        return ResponseEntity.ok(taskBoardService.updateTaskBoard(boardID, board));
    }

    @DeleteMapping("/{boardID}")
    @PreAuthorize("@accessCheckService.hasAccessToBoard(#boardID, authentication.principal.ID)")
    public ResponseEntity<TaskBoardDTO> deleteTaskBoard(@PathVariable UUID boardID) {
        taskBoardService.deleteTaskBoard(boardID);
        return ResponseEntity.noContent().build();
    }
}
