package com.taskapproacher.controller.user;

import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.user.UserResponse;
import com.taskapproacher.service.user.UserService;
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
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<UserResponse> getUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication() ;
            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok(new UserResponse(user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}/boards")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<List<TaskBoardResponse>> getBoardsByUser(@PathVariable UUID userId) {
        try {
            return ResponseEntity.ok(userService.findBoardsByUser(userId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID userId, @RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.update(userId, user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new UserResponse(null));
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable UUID userId) {
        try {
            userService.delete(userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
