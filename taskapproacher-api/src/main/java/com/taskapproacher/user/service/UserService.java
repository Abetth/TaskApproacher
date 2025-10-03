package com.taskapproacher.user.service;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.exception.custom.EntityAlreadyExistsException;
import com.taskapproacher.task.mapper.TaskBoardMapper;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.model.TaskBoardDTO;
import com.taskapproacher.user.constant.Role;
import com.taskapproacher.user.model.User;
import com.taskapproacher.user.model.UserDTO;
import com.taskapproacher.user.repository.UserRepository;
import com.taskapproacher.user.mapper.UserMapper;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final TaskBoardMapper taskBoardMapper;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.taskBoardMapper  = new TaskBoardMapper();
        this.userMapper = new UserMapper();
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

    public List<TaskBoardDTO> findBoardsByUser(UUID userID)
            throws IllegalArgumentException, EntityNotFoundException {
        findByID(userID);

        List<TaskBoard> taskBoards = userRepository.findRelatedEntitiesByID(userID);

        return taskBoards.stream().map(taskBoardMapper::mapToTaskBoardDTO).collect(Collectors.toList());
    }

    public UserDTO createUser(User user) throws IllegalArgumentException, EntityAlreadyExistsException {
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
            return userMapper.mapToUserResponse(userRepository.save(createdUser));
        }
    }

    public UserDTO updateUser(UUID userID, User user) throws IllegalArgumentException, EntityNotFoundException {
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

        return userMapper.mapToUserResponse(userRepository.update(updatedUser));
    }

    public void deleteUser(UUID userID) throws IllegalArgumentException, EntityNotFoundException {
        if (userID == null) {
            throw new IllegalArgumentException("User id " + ExceptionMessage.NULL);
        }

        User user = findByID(userID);

        userRepository.delete(user);
    }
}
