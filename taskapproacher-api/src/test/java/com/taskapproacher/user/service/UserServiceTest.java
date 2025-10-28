package com.taskapproacher.user.service;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.common.exception.custom.EntityAlreadyExistsException;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.model.TaskBoardDTO;
import com.taskapproacher.user.constant.Role;
import com.taskapproacher.user.model.User;
import com.taskapproacher.user.model.UserDTO;
import com.taskapproacher.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//Tests naming convention: method_scenario_result
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;

    private User createDefaultUser(UUID userID) {
        User user = new User();
        user.setID(userID);
        user.setUsername("Created User 1");
        user.setPassword("userpass");
        user.setEmail("usermail@mail.mail");
        user.setRole(Role.USER);
        user.setTaskBoards(createDefaultListOfTaskBoards(user));

        return user;
    }

    private List<TaskBoard> createDefaultListOfTaskBoards(User user) {
        TaskBoard firstTaskBoard = new TaskBoard(
                UUID.randomUUID(),
                "First task board",
                false,
                null,
                user
        );

        TaskBoard secondTaskBoard = new TaskBoard(
                UUID.randomUUID(),
                "First task board",
                true,
                null,
                user
        );

        return List.of(firstTaskBoard, secondTaskBoard);
    }

    @Test
    void findByID_ValidID_ReturnsUser() {
        UUID userID = UUID.randomUUID();
        User mockUser = createDefaultUser(userID);

        when(userRepository.findByID(userID)).thenReturn(Optional.of(mockUser));

        User user = userService.findByID(userID);

        assertEquals(userID, user.getID());
        assertEquals(mockUser.getUsername(), user.getUsername());
        assertEquals(mockUser.getEmail(), user.getEmail());
        assertEquals(mockUser.getRole(), user.getRole());

        verify(userRepository, times(1)).findByID(userID);
    }

    @Test
    void findByID_InvalidID_ThrowsEntityNotFoundException() {
        UUID userID = UUID.randomUUID();

        when(userRepository.findByID(userID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.findByID(userID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findByID(userID);
    }

    @Test
    void findByID_NullID_ThrowsIllegalArgumentException() {
        UUID userID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.findByID(userID);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(0)).findByID(userID);
    }

    @Test
    void findByUsername_ValidUsername_ReturnsUser() {
        UUID userID = UUID.randomUUID();
        User mockUser = createDefaultUser(userID);

        when(userRepository.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));

        User user = userService.findByUsername(mockUser.getUsername());

        assertEquals(userID, user.getID());
        assertEquals(mockUser.getUsername(), user.getUsername());

        verify(userRepository, times(1)).findByUsername(mockUser.getUsername());
    }

    @Test
    void findByUsername_InvalidUsername_ThrowsUsernameNotFoundException() {
        String username = "Wrong username";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.findByUsername(username);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void findByUsername_EmptyUsername_ThrowsIllegalArgumentException() {
        String username = "";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
           userService.findByUsername(username);
        });

        String expectedMessage = ExceptionMessage.EMPTY.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(0)).findByUsername(username);
    }

    @Test
    void findByUsername_NullUsername_ThrowsIllegalArgumentException() {
        String username = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.findByUsername(username);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(0)).findByUsername(username);
    }

    @Test
    void findBoardsByUser_ValidUserID_ReturnsTaskBoardDTOList() {
        UUID userID = UUID.randomUUID();
        User user = createDefaultUser(userID);

        List<TaskBoard> mockBoards = createDefaultListOfTaskBoards(user);

        when(userRepository.findByID(userID)).thenReturn(Optional.of(user));
        when(userRepository.findRelatedEntitiesByID(userID)).thenReturn(mockBoards);

        List<TaskBoardDTO> responseBoards = userService.findBoardsByUser(userID);

        assertEquals(responseBoards.size(), 2);
        assertEquals(responseBoards.get(0).getID(), mockBoards.get(0).getID());
        assertEquals(responseBoards.get(1).getID(), mockBoards.get(1).getID());

        verify(userRepository, times(1)).findByID(userID);
        verify(userRepository, times(1)).findRelatedEntitiesByID(userID);
    }

    @Test
    void findBoardsByUser_ValidUserIDZeroTaskBoards_ReturnsEmptyTaskBoardDTOList() {
        UUID userID = UUID.randomUUID();
        User user = createDefaultUser(userID);

        when(userRepository.findByID(userID)).thenReturn(Optional.of(user));
        when(userRepository.findRelatedEntitiesByID(userID)).thenReturn(List.of());

        List<TaskBoardDTO> responseBoards = userService.findBoardsByUser(userID);

        assertEquals(responseBoards.size(), 0);

        verify(userRepository, times(1)).findByID(userID);
        verify(userRepository, times(1)).findRelatedEntitiesByID(userID);
    }

    @Test
    void findBoardsByUser_NullUserID_ThrowsIllegalArgumentException() {
        UUID userID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.findBoardsByUser(userID);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(0)).findByID(userID);
        verify(userRepository, times(0)).findRelatedEntitiesByID(userID);
    }

    @Test
    void findBoardsByUser_InvalidUserID_ThrowsEntityNotFoundException() {
        UUID userID = UUID.randomUUID();

        when(userRepository.findByID(userID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
           userService.findBoardsByUser(userID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findByID(userID);
        verify(userRepository, times(0)).findRelatedEntitiesByID(userID);
    }

    @Test
    void createUser_ValidUser_ReturnsUser() {
        User user = createDefaultUser(UUID.randomUUID());

        String encodedPassword = "encodedUserPass";

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        when(userRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.isUserExists(ArgumentMatchers.any(User.class))).thenReturn(false);
        when(passwordEncoder.encode(user.getPassword())).thenReturn(encodedPassword);

        User response = userService.createUser(user);

        User capturedUser = captor.getValue();

        assertEquals(user.getUsername(), capturedUser.getUsername());
        assertEquals(user.getEmail(), capturedUser.getEmail());
        assertEquals(Role.USER, capturedUser.getRole());
        assertEquals(encodedPassword, capturedUser.getPassword());
        assertNotEquals(user.getPassword(), capturedUser.getPassword());

        assertNotNull(response);
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());

        verify(userRepository, times(1)).save(captor.capture());
        verify(userRepository, times(1)).isUserExists(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_EmptyUsername_ThrowsIllegalArgumentException() {
        User user = createDefaultUser(UUID.randomUUID());
        user.setUsername("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user);
        });

        String expectedMessage = ExceptionMessage.EMPTY.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(0)).isUserExists(ArgumentMatchers.any(User.class));
        verify(userRepository, times(0)).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_NullUsername_ThrowsIllegalArgumentException() {
        User user = createDefaultUser(UUID.randomUUID());
        user.setUsername(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
           userService.createUser(user);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(0)).isUserExists(ArgumentMatchers.any(User.class));
        verify(userRepository, times(0)).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_EmptyEmail_ThrowsIllegalArgumentException() {
        User user = createDefaultUser(UUID.randomUUID());
        user.setEmail("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user);
        });

        String expectedMessage = ExceptionMessage.EMPTY.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(0)).isUserExists(ArgumentMatchers.any(User.class));
        verify(userRepository, times(0)).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_NullEmail_ThrowsIllegalArgumentException() {
        User user = createDefaultUser(UUID.randomUUID());
        user.setEmail(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(0)).isUserExists(ArgumentMatchers.any(User.class));
        verify(userRepository, times(0)).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_EmptyPassword_ThrowsIllegalArgumentException() {
        User user = createDefaultUser(UUID.randomUUID());
        user.setPassword("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user);
        });

        String expectedMessage = ExceptionMessage.EMPTY.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(0)).isUserExists(ArgumentMatchers.any(User.class));
        verify(userRepository, times(0)).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_NullPassword_ThrowsIllegalArgumentException() {
        User user = createDefaultUser(UUID.randomUUID());
        user.setPassword(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(0)).isUserExists(ArgumentMatchers.any(User.class));
        verify(userRepository, times(0)).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void createUser_UserAlreadyExists_ThrowsEntityAlreadyExistsException() {
        User user = createDefaultUser(UUID.randomUUID());

        when(userRepository.isUserExists(ArgumentMatchers.any(User.class))).thenReturn(true);

        EntityAlreadyExistsException exception = assertThrows(EntityAlreadyExistsException.class, () -> {
            userService.createUser(user);
        });

        String expectedMessage = ExceptionMessage.ALREADY_EXISTS.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).isUserExists(ArgumentMatchers.any(User.class));
        verify(userRepository, times(0)).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void updateUser_ValidUser_ReturnsUserDTODataChanged() {
        UUID userID = UUID.randomUUID();
        String newEncodedPassword = "newEncodedPass";

        User existingUser = createDefaultUser(userID);

        User copyOfExistingUser = new User();
        BeanUtils.copyProperties(existingUser, copyOfExistingUser);

        User updateData = new User();
        updateData.setUsername("Updated User 1");
        updateData.setPassword("changedpass");
        updateData.setEmail("ml@mail.mail");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByID(userID)).thenReturn(Optional.of(copyOfExistingUser));
        when(userRepository.isUsernameAlreadyTaken(ArgumentMatchers.any(String.class))).thenReturn(false);
        when(userRepository.isEmailAlreadyTaken(ArgumentMatchers.any(String.class))).thenReturn(false);
        when(passwordEncoder.encode(updateData.getPassword())).thenReturn(newEncodedPassword);
        when(userRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO response  = userService.updateUser(userID, updateData);
        User capturedUser = captor.getValue();

        assertNotEquals(existingUser.getUsername(), capturedUser.getUsername());
        assertNotEquals(existingUser.getEmail(), capturedUser.getEmail());
        assertNotEquals(existingUser.getPassword(), capturedUser.getPassword());
        assertEquals(newEncodedPassword, capturedUser.getPassword());
        assertEquals(existingUser.getRole(), capturedUser.getRole());
        assertEquals(existingUser.getTaskBoards(), capturedUser.getTaskBoards());

        assertNotNull(response);
        assertEquals(updateData.getUsername(), response.getUsername());
        assertEquals(updateData.getEmail(), response.getEmail());

        verify(userRepository, times(1)).findByID(userID);
        verify(userRepository, times(1)).isUsernameAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(userRepository, times(1)).isEmailAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(passwordEncoder, times(1)).encode(ArgumentMatchers.any(String.class));
        verify(userRepository, times(1)).update(captor.capture());
    }

    @Test
    void updateUser_UserFieldsAreNull_ReturnsUserDTODataDidNotChanged() {
        UUID userID = UUID.randomUUID();

        User existingUser = createDefaultUser(userID);

        User copyOfExistingUser = new User();
        BeanUtils.copyProperties(existingUser, copyOfExistingUser);

        User updateData = new User();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByID(userID)).thenReturn(Optional.of(copyOfExistingUser));
        when(userRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO response = userService.updateUser(userID, updateData);
        User capturedUser = captor.getValue();

        assertEquals(existingUser.getID(), capturedUser.getID());
        assertEquals(existingUser.getUsername(), capturedUser.getUsername());
        assertEquals(existingUser.getPassword(), capturedUser.getPassword());
        assertEquals(existingUser.getEmail(), capturedUser.getEmail());
        assertEquals(existingUser.getRole(), capturedUser.getRole());
        assertEquals(existingUser.getTaskBoards(), capturedUser.getTaskBoards());

        assertNotNull(response);
        assertNotEquals(updateData.getUsername(), response.getUsername());
        assertNotEquals(updateData.getEmail(), response.getEmail());

        verify(userRepository, times(1)).findByID(userID);
        verify(passwordEncoder, times(0)).encode(ArgumentMatchers.any(String.class));
        verify(userRepository, times(0)).isUsernameAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(userRepository, times(0)).isEmailAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(userRepository, times(1)).update(captor.capture());
    }

    @Test
    void updateUser_UserFieldsAreEmpty_ReturnsUserDTODataDidNotChanged() {
        UUID userID = UUID.randomUUID();

        User existingUser = createDefaultUser(userID);

        User copyOfExistingUser = new User();
        BeanUtils.copyProperties(existingUser, copyOfExistingUser);

        User updateData = new User();
        updateData.setUsername("");
        updateData.setPassword("");
        updateData.setEmail("");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByID(userID)).thenReturn(Optional.of(copyOfExistingUser));
        when(userRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO response = userService.updateUser(userID, updateData);
        User capturedUser = captor.getValue();

        assertEquals(existingUser.getID(), capturedUser.getID());
        assertEquals(existingUser.getUsername(), capturedUser.getUsername());
        assertEquals(existingUser.getPassword(), capturedUser.getPassword());
        assertEquals(existingUser.getEmail(), capturedUser.getEmail());
        assertEquals(existingUser.getRole(), capturedUser.getRole());
        assertEquals(existingUser.getTaskBoards(), capturedUser.getTaskBoards());

        assertNotNull(response);
        assertNotEquals(updateData.getUsername(), response.getUsername());
        assertNotEquals(updateData.getEmail(), response.getEmail());

        verify(userRepository, times(1)).findByID(userID);
        verify(passwordEncoder, times(0)).encode(ArgumentMatchers.any(String.class));
        verify(userRepository, times(0)).isUsernameAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(userRepository, times(0)).isEmailAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(userRepository, times(1)).update(captor.capture());
    }

    @Test
    void updateUser_UserFieldsAreTheSame_ReturnsUserDTODataDidNotChanged() {
        UUID userID = UUID.randomUUID();

        User existingUser = createDefaultUser(userID);

        User copyOfExistingUser = new User();
        BeanUtils.copyProperties(existingUser, copyOfExistingUser);

        User updateData = new User();
        updateData.setUsername(existingUser.getUsername());
        updateData.setEmail(existingUser.getEmail());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByID(userID)).thenReturn(Optional.of(copyOfExistingUser));
        when(userRepository.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO response = userService.updateUser(userID, updateData);
        User capturedUser = captor.getValue();

        assertEquals(existingUser.getID(), capturedUser.getID());
        assertEquals(existingUser.getUsername(), capturedUser.getUsername());
        assertEquals(existingUser.getPassword(), capturedUser.getPassword());
        assertEquals(existingUser.getEmail(), capturedUser.getEmail());
        assertEquals(existingUser.getRole(), capturedUser.getRole());
        assertEquals(existingUser.getTaskBoards(), capturedUser.getTaskBoards());

        assertNotNull(response);
        assertEquals(updateData.getUsername(), response.getUsername());
        assertEquals(updateData.getEmail(), response.getEmail());

        verify(userRepository, times(1)).findByID(userID);
        verify(passwordEncoder, times(0)).encode(ArgumentMatchers.any(String.class));
        verify(userRepository, times(0)).isUsernameAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(userRepository, times(0)).isEmailAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(userRepository, times(1)).update(captor.capture());
    }

    @Test
    void updateUser_InvalidUserID_ThrowsEntityNotFoundException() {
        UUID userID = UUID.randomUUID();

        when(userRepository.findByID(userID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.updateUser(userID, new User());
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findByID(userID);
        verify(passwordEncoder, times(0)).encode(ArgumentMatchers.any(String.class));
        verify(userRepository, times(0)).isUsernameAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(userRepository, times(0)).isEmailAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(userRepository, times(0)).update(ArgumentMatchers.any(User.class));
    }

    @Test
    void updateUser_AlreadyTakenUsername_ThrowsEntityAlreadyExistsException() {
        UUID userID = UUID.randomUUID();
        User user = createDefaultUser(userID);

        User updateData = new User();
        updateData.setUsername("ABUsernameBA");

        when(userRepository.findByID(userID)).thenReturn(Optional.of(user));
        when(userRepository.isUsernameAlreadyTaken(ArgumentMatchers.any(String.class))).thenReturn(true);

        EntityAlreadyExistsException exception = assertThrows(EntityAlreadyExistsException.class, () -> {
            userService.updateUser(userID, updateData);
        });

        String expectedMessage = "username " + ExceptionMessage.ALREADY_EXISTS;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findByID(userID);
        verify(userRepository, times(1)).isUsernameAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(passwordEncoder, times(0)).encode(ArgumentMatchers.any(String.class));
        verify(userRepository, times(0)).isEmailAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(userRepository, times(0)).update(ArgumentMatchers.any(User.class));
    }

    @Test
    void updateUser_AlreadyTakenEmail_ThrowsEntityAlreadyExistsException() {
        UUID userID = UUID.randomUUID();
        User user = createDefaultUser(userID);

        User updateData = new User();
        updateData.setEmail("AB@mail.mail");

        when(userRepository.findByID(userID)).thenReturn(Optional.of(user));
        when(userRepository.isEmailAlreadyTaken(ArgumentMatchers.any(String.class))).thenReturn(true);

        EntityAlreadyExistsException exception = assertThrows(EntityAlreadyExistsException.class, () -> {
            userService.updateUser(userID, updateData);
        });

        String expectedMessage = "email " + ExceptionMessage.ALREADY_EXISTS;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findByID(userID);
        verify(userRepository, times(0)).isUsernameAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(passwordEncoder, times(0)).encode(ArgumentMatchers.any(String.class));
        verify(userRepository, times(1)).isEmailAlreadyTaken(ArgumentMatchers.any(String.class));
        verify(userRepository, times(0)).update(ArgumentMatchers.any(User.class));
    }

    @Test
    void deleteUser_ValidUserID_UserDeletedSuccessfully() {
        UUID userID = UUID.randomUUID();

        User user = createDefaultUser(userID);

        when(userRepository.findByID(userID)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(ArgumentMatchers.any(User.class));

        userService.deleteUser(userID);

        verify(userRepository, times(1)).findByID(userID);
        verify(userRepository, times(1)).delete(ArgumentMatchers.any(User.class));
    }

    @Test
    void deleteUser_NulldUserID_ThrowsIllegalArgumentException() {
        UUID userID = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(userID);
        });

        String expectedMessage = ExceptionMessage.NULL.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(0)).findByID(ArgumentMatchers.any(UUID.class));
        verify(userRepository, times(0)).delete(ArgumentMatchers.any(User.class));
    }

    @Test
    void deleteUser_InvalidUserID_ThrowsEntityNotFoundException() {
        UUID userID = UUID.randomUUID();

        when(userRepository.findByID(userID))
                .thenThrow(new EntityNotFoundException(ExceptionMessage.NOT_FOUND.toString()));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.deleteUser(userID);
        });

        String expectedMessage = ExceptionMessage.NOT_FOUND.toString();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findByID(userID);
        verify(userRepository, times(0)).delete(ArgumentMatchers.any(User.class));
    }
}
