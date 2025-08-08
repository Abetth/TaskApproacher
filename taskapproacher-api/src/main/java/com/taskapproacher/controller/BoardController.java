package com.taskapproacher.controller;

import com.taskapproacher.entity.Task;
import com.taskapproacher.entity.TaskBoard;
import com.taskapproacher.service.TaskBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<TaskBoard>> getAllBoards() {
        try {
            return ResponseEntity.ok(taskBoardService.findAll());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<TaskBoard> getById(@PathVariable UUID boardId) {
        try {
            return ResponseEntity.ok(taskBoardService.findById(boardId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{boardId}/tasks")
    public ResponseEntity<List<Task>> getByBoard(@PathVariable UUID boardId) {
        try {
            return ResponseEntity.ok(taskBoardService.findByTaskBoard(boardId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<TaskBoard> create(@RequestBody TaskBoard board) {
        try {
            TaskBoard createBoard = taskBoardService.create(board);
            return ResponseEntity.status(201).body(createBoard);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping
    public ResponseEntity<TaskBoard> update(@RequestBody TaskBoard board) {
        try {
            return ResponseEntity.ok(taskBoardService.update(board));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<TaskBoard> delete(@PathVariable UUID boardId) {
        try {
            taskBoardService.delete(boardId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
