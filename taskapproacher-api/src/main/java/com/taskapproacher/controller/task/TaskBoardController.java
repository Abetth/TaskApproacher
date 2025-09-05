package com.taskapproacher.controller.task;

import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.service.task.TaskBoardService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
public class TaskBoardController {
    private final TaskBoardService taskBoardService;

    @Autowired
    public TaskBoardController(TaskBoardService taskBoardService) {
        this.taskBoardService = taskBoardService;
    }

    @GetMapping("/{boardID}/tasks")
    @PreAuthorize("@accessCheckService.hasAccessToBoard(#boardID, authentication.principal.ID)")
    public ResponseEntity<List<Task>> getTasksByBoard(@PathVariable UUID boardID) {
        try {
            return ResponseEntity.ok(taskBoardService.findByTaskBoard(boardID));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{userID}")
    @PreAuthorize("#board.user.ID == authentication.principal.ID")
    public ResponseEntity<TaskBoardResponse> create(@PathVariable UUID userID,
                                                    @RequestBody TaskBoard board) {
        try {
            TaskBoardResponse createBoard = taskBoardService.create(userID, board);
            return ResponseEntity.status(201).body(createBoard);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping("/{boardID}")
    @PreAuthorize("@accessCheckService.hasAccessToBoard(#boardID, authentication.principal.ID)")
    public ResponseEntity<TaskBoardResponse> update(@PathVariable UUID boardID, @RequestBody TaskBoard board) {
        try {
            return ResponseEntity.ok(taskBoardService.update(boardID, board));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{boardID}")
    @PreAuthorize("@accessCheckService.hasAccessToBoard(#boardID, authentication.principal.ID)")
    public ResponseEntity<TaskBoardResponse> delete(@PathVariable UUID boardID) {
        try {
            taskBoardService.delete(boardID);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
