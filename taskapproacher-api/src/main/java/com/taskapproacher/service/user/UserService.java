package com.taskapproacher.service.user;

import com.taskapproacher.dao.user.UserDAO;
import com.taskapproacher.entity.task.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.user.UserResponse;
import com.taskapproacher.enums.Role;
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

    public User findByID(UUID userID) {
        User user = userDAO.findByID(userID);
        if (user == null) {
            throw new RuntimeException("User is not found");
        }
        return user;
    }

    public Optional<User> findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public List<TaskBoardResponse> findBoardsByUser(UUID userID) {
        if (userDAO.findByID(userID) == null) {
            throw new EntityNotFoundException("User not found");
        }
        return userDAO.findRelatedEntitiesByUUID(userID);
    }

    public UserResponse create(User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be empty");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("User password cannot be empty");
        }

        User createdUser = new User();
        createdUser.setUsername(user.getUsername());
        createdUser.setEmail(user.getEmail());
        createdUser.setPassword(passwordEncoder.encode(user.getPassword()));
        createdUser.setRole(Role.USER);

        if (userDAO.isUserExists(createdUser)) {
            throw new RuntimeException("User already exists");
        } else {
            userDAO.save(createdUser);
        }

        return new UserResponse(createdUser);
    }

    public UserResponse update(UUID userID, User user) {
        if (Objects.isNull(userDAO.findByID(userID))) {
            throw new RuntimeException("Database entry is missing");
        }

        if(!Objects.isNull(user.getPassword()) && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        User updatedUser = userDAO.findByID(userID);
        updatedUser.setUsername(user.getUsername());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setPassword(updatedUser.getPassword());

        userDAO.update(updatedUser);

        return new UserResponse(updatedUser);
    }

    public void delete(UUID userID) {
        if (Objects.isNull(userDAO.findByID(userID))) {
            throw new RuntimeException("User not found");
        }

        userDAO.delete(userID);
    }
}
