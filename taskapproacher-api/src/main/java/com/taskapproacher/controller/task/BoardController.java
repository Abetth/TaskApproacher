package com.taskapproacher.controller.task;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.TaskBoardResponse;
import com.taskapproacher.service.task.TaskBoardService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
    private final TaskBoardService taskBoardService;

    @Autowired
    public BoardController(TaskBoardService taskBoardService) {
        this.taskBoardService = taskBoardService;
    }

    @GetMapping("/{boardId}/tasks")
    @PreAuthorize("@accessCheckService.hasAccessToBoard(#boardId, authentication.principal.id)")
    public ResponseEntity<List<Task>> getTasksByBoard(@PathVariable UUID boardId) {
        try {
            return ResponseEntity.ok(taskBoardService.findByTaskBoard(boardId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("#board.user.id == authentication.principal.id")
    public ResponseEntity<TaskBoardResponse> create(@RequestBody TaskBoard board) {
        try {
            TaskBoardResponse createBoard = taskBoardService.create(board);
            return ResponseEntity.status(201).body(createBoard);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{boardId}")
    @PreAuthorize("@accessCheckService.hasAccessToBoard(#boardId, authentication.principal.id)")
    public ResponseEntity<TaskBoardResponse> update(@PathVariable UUID boardId, @RequestBody TaskBoard board) {
        try {
            return ResponseEntity.ok(taskBoardService.update(boardId, board));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{boardId}")
    @PreAuthorize("@accessCheckService.hasAccessToBoard(#boardId, authentication.principal.id)")
    public ResponseEntity<TaskBoardResponse> delete(@PathVariable UUID boardId) {
        try {
            taskBoardService.delete(boardId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
