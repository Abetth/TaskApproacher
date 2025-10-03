package com.taskapproacher.user.controller;

import com.taskapproacher.task.model.TaskBoardDTO;
import com.taskapproacher.user.service.UserService;
import com.taskapproacher.user.model.User;
import com.taskapproacher.user.model.UserDTO;

import com.taskapproacher.user.mapper.UserMapper;
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
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService) {
        this.userMapper = new UserMapper();
        this.userService = userService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDTO> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userMapper.mapToUserResponse(user));
    }

    @GetMapping("/{userID}/boards")
    @PreAuthorize("#userID == authentication.principal.ID")
    public ResponseEntity<List<TaskBoardDTO>> getBoardsByUser(@PathVariable UUID userID) {
        return ResponseEntity.ok(userService.findBoardsByUser(userID));
    }

    @PatchMapping("/{userID}")
    @PreAuthorize("#userID == authentication.principal.ID")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID userID, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(userID, user));
    }

    @DeleteMapping("/{userID}")
    @PreAuthorize("#userID == authentication.principal.ID")
    public ResponseEntity<UserDTO> deleteUser(@PathVariable UUID userID) {
        userService.deleteUser(userID);
        return ResponseEntity.noContent().build();
    }
}
