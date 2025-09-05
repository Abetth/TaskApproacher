package com.taskapproacher.service.user;

import com.taskapproacher.dao.user.UserDAO;
import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.user.UserResponse;
import com.taskapproacher.enums.Role;
import com.taskapproacher.customexceptions.EntityAlreadyExistsException;
import com.taskapproacher.enums.ErrorMessage;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public User findById(UUID userId) throws IllegalArgumentException, EntityNotFoundException {
        if (userId == null) {
            throw new IllegalArgumentException("User id " + ErrorMessage.NULL);
        }

        return userDAO.findById(userId).orElseThrow(() -> new EntityNotFoundException("User " + ErrorMessage.NOT_FOUND));
    }

    public User findByUsername(String username) throws IllegalArgumentException, UsernameNotFoundException {
        if (username == null || username.isEmpty()) {
            ErrorMessage error = (username == null) ? ErrorMessage.NULL : ErrorMessage.EMPTY;
            throw new IllegalArgumentException("Username " + error);
        }

        return userDAO.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + ErrorMessage.NOT_FOUND + ": " + username));
    }

    public List<TaskBoardResponse> findBoardsByUser(UUID userId) {
        findById(userId);

        return userDAO.findRelatedEntitiesByID(userId);
    }

    public UserResponse create(User user) throws IllegalArgumentException, EntityAlreadyExistsException {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            ErrorMessage error = (user.getUsername() == null) ? ErrorMessage.NULL : ErrorMessage.EMPTY;
            throw new IllegalArgumentException("Username " + error);
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            ErrorMessage error = (user.getEmail() == null) ? ErrorMessage.NULL : ErrorMessage.EMPTY;
            throw new IllegalArgumentException("User email " + error);
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            ErrorMessage error = (user.getPassword() == null) ? ErrorMessage.NULL : ErrorMessage.EMPTY;
            throw new IllegalArgumentException("User password " + error);
        }

        User createdUser = new User();
        createdUser.setUsername(user.getUsername());
        createdUser.setEmail(user.getEmail());
        createdUser.setPassword(passwordEncoder.encode(user.getPassword()));
        createdUser.setRole(Role.USER);

        if (userDAO.isUserExists(createdUser)) {
            throw new EntityAlreadyExistsException("User " + ErrorMessage.ALREADY_EXISTS);
        } else {
            return new UserResponse(userDAO.save(createdUser));
        }
    }

    public UserResponse update(UUID userId, User user) {
        User updatedUser = findById(userId);

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

    public void delete(UUID userId) throws IllegalArgumentException {
        if (userId == null) {
            throw new IllegalArgumentException("User id " + ErrorMessage.NULL);
        }

        userDAO.delete(userId);
    }
}
