package com.taskapproacher.service.user;

import com.taskapproacher.dao.user.UserDAO;
import com.taskapproacher.entity.task.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.user.UserResponse;
import com.taskapproacher.enums.Role;
import com.taskapproacher.customexceptions.EntityAlreadyExistsException;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserDAO userDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
    }

    public User findById(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id cannot be null");
        }

        return userDAO.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }

    public Optional<User> findByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        Optional<User> user = userDAO.findByUsername(username);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User is not found");
        }

        return user;
    }

    public List<TaskBoardResponse> findBoardsByUser(UUID userId) {
        if (userDAO.findById(userId).isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        return userDAO.findRelatedEntitiesByUUID(userId);
    }

    public UserResponse create(User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("User password cannot be null or empty");
        }

        User createdUser = new User();
        createdUser.setUsername(user.getUsername());
        createdUser.setEmail(user.getEmail());
        createdUser.setPassword(passwordEncoder.encode(user.getPassword()));
        createdUser.setRole(Role.USER);

        if (userDAO.isUserExists(createdUser)) {
            throw new EntityAlreadyExistsException("User already exists");
        } else {
            return new UserResponse(userDAO.save(createdUser));
        }
    }

    public UserResponse update(UUID userId, User user) {
        if (userDAO.findById(userId).isEmpty()) {
            throw new EntityNotFoundException("Database entry is missing");
        }

        User updatedUser = userDAO.findById(userId).get();

        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            updatedUser.setUsername(user.getUsername());
        }

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            updatedUser.setEmail(user.getEmail());
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            updatedUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return new UserResponse(userDAO.update(updatedUser));
    }

    public void delete(UUID userId) {
        if (userDAO.findById(userId).isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        userDAO.delete(userId);
    }
}
