package com.taskapproacher.service.user;

import com.taskapproacher.constant.ExceptionMessage;
import com.taskapproacher.constant.Role;
import com.taskapproacher.repository.user.UserRepository;
import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.user.response.UserResponse;
import com.taskapproacher.exception.custom.EntityAlreadyExistsException;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findByID(UUID userID) throws IllegalArgumentException, EntityNotFoundException {
        if (userID == null) {
            throw new IllegalArgumentException("User id " + ExceptionMessage.NULL);
        }

        return userRepository.findByID(userID).orElseThrow(
                () -> new EntityNotFoundException("User " + ExceptionMessage.NOT_FOUND)
        );
    }

    public User findByUsername(String username) throws IllegalArgumentException, UsernameNotFoundException {
        if (username == null || username.isEmpty()) {
            ExceptionMessage error = (username == null) ? ExceptionMessage.NULL
                    : ExceptionMessage.EMPTY;

            throw new IllegalArgumentException("Username " + error);
        }

        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User " + ExceptionMessage.NOT_FOUND + ": " + username)
        );
    }

    public List<TaskBoardResponse> findBoardsByUser(UUID userID)
            throws IllegalArgumentException, EntityNotFoundException {
        findByID(userID);

        return userRepository.findRelatedEntitiesByID(userID);
    }

    public UserResponse create(User user) throws IllegalArgumentException, EntityAlreadyExistsException {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            ExceptionMessage error = (user.getUsername() == null) ? ExceptionMessage.NULL : ExceptionMessage.EMPTY;
            throw new IllegalArgumentException("Username " + error);
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            ExceptionMessage error = (user.getEmail() == null) ? ExceptionMessage.NULL : ExceptionMessage.EMPTY;
            throw new IllegalArgumentException("User email " + error);
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            ExceptionMessage error = (user.getPassword() == null) ? ExceptionMessage.NULL : ExceptionMessage.EMPTY;
            throw new IllegalArgumentException("User password " + error);
        }

        User createdUser = new User();
        createdUser.setUsername(user.getUsername());
        createdUser.setEmail(user.getEmail());
        createdUser.setPassword(passwordEncoder.encode(user.getPassword()));
        createdUser.setRole(Role.USER);

        if (userRepository.isUserExists(createdUser)) {
            throw new EntityAlreadyExistsException("User " + ExceptionMessage.ALREADY_EXISTS);
        } else {
            return new UserResponse(userRepository.save(createdUser));
        }
    }

    public UserResponse update(UUID userID, User user) throws IllegalArgumentException, EntityNotFoundException {
        User updatedUser = findByID(userID);

        String newUsername = user.getUsername();
        if (newUsername != null && !newUsername.isEmpty() && !newUsername.equals(updatedUser.getUsername())) {

            if (userRepository.isUsernameAlreadyTaken(newUsername)) {
                throw new EntityAlreadyExistsException("User with this username " + ExceptionMessage.ALREADY_EXISTS);
            }
            updatedUser.setUsername(newUsername);
        }

        String newEmail = user.getEmail();
        if (newEmail != null && !newEmail.isEmpty() && !newEmail.equals(updatedUser.getEmail())) {

            if (userRepository.isEmailAlreadyTaken(newEmail)) {
                throw new EntityAlreadyExistsException("User with this email " + ExceptionMessage.ALREADY_EXISTS);
            }
            updatedUser.setEmail(newEmail);
        }

        String newPassword = user.getPassword();
        if (newPassword != null && !newPassword.isEmpty()) {
            updatedUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return new UserResponse(userRepository.update(updatedUser));
    }

    public void delete(UUID userID) throws IllegalArgumentException, EntityNotFoundException {
        if (userID == null) {
            throw new IllegalArgumentException("User id " + ExceptionMessage.NULL);
        }

        User user = findByID(userID);

        userRepository.delete(user);
    }
}
