package com.taskapproacher.user.controller;

import com.taskapproacher.task.model.TaskBoardResponse;
import com.taskapproacher.user.service.UserService;
import com.taskapproacher.user.model.User;
import com.taskapproacher.user.model.UserResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(new UserResponse(user));
    }

    @GetMapping("/{userID}/boards")
    @PreAuthorize("#userID == authentication.principal.ID")
    public ResponseEntity<List<TaskBoardResponse>> getBoardsByUser(@PathVariable UUID userID) {
        return ResponseEntity.ok(userService.findBoardsByUser(userID));
    }

    @PatchMapping("/{userID}")
    @PreAuthorize("#userID == authentication.principal.ID")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID userID, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(userID, user));
    }

    @DeleteMapping("/{userID}")
    @PreAuthorize("#userID == authentication.principal.ID")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable UUID userID) {
        userService.deleteUser(userID);
        return ResponseEntity.noContent().build();
    }
}
