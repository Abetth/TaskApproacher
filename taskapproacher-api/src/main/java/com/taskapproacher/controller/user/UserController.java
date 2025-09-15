package com.taskapproacher.controller.user;

import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.user.UserResponse;
import com.taskapproacher.service.user.UserService;
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
    public ResponseEntity<UserResponse> update(@PathVariable UUID userID, @RequestBody User user) {
        return ResponseEntity.ok(userService.update(userID, user));
    }

    @DeleteMapping("/{userID}")
    @PreAuthorize("#userID == authentication.principal.ID")
    public ResponseEntity<UserResponse> delete(@PathVariable UUID userID) {
        userService.delete(userID);
        return ResponseEntity.noContent().build();
    }
}
