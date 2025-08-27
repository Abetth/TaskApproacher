package com.taskapproacher.service.user;

import com.taskapproacher.dao.user.UserDAO;
import com.taskapproacher.entity.task.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.user.UserResponse;
import com.taskapproacher.enums.Role;
import com.taskapproacher.customexceptions.EntityAlreadyExistsException;

import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserDAO userDAO;

    @Test
    void findById_ValidId_ReturnsUser() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);

        Mockito.when(userDAO.findById(userId)).thenReturn(Optional.of(mockUser));

        User user = userService.findById(userId);

        assertEquals(userId, user.getId());

        Mockito.verify(userDAO, Mockito.times(1)).findById(userId);
    }

    @Test
    void findById_InValidId_ThrowsEntityNotFoundException() {
        UUID id = UUID.randomUUID();

        Mockito.when(userDAO.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.findById(id);
        });

        Mockito.verify(userDAO, Mockito.times(1)).findById(id);

        String expectedMessage = "User is not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void findById_NullId_ThrowsIllegalArgumentException() {
        UUID id = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.findById(id);
        });

        String expectedMessage = "User id cannot be null";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void findByUsername_ValidUsername_ReturnsOptionalUser() {
        UUID userId = UUID.randomUUID();
        String username = "User 1";
        User mockUser = new User(userId, username, "testpassword" , "usermail@mail.mail", Role.USER, null);
        Optional<User> optionalUser = Optional.of(mockUser);

        Mockito.when(userDAO.findByUsername(username)).thenReturn(optionalUser);

        User user = userService.findByUsername(username).get();

        assertEquals(userId, user.getId());
        assertEquals(username, user.getUsername());

        Mockito.verify(userDAO, Mockito.times(1)).findByUsername(username);
    }

    @Test
    void findByUsername_InValidUsername_ThrowsEntityNotFoundException() {
        String username = "Wrong username";

        Mockito.when(userDAO.findByUsername(username)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.findByUsername(username);
        });

        Mockito.verify(userDAO, Mockito.times(1)).findByUsername(username);

        String expectedMessage = "User is not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void findByUsername_EmptyUsername_ThrowsIllegalArgumentException() {
        String username = "";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
           userService.findByUsername(username);
        });

        String expectedMessage = "Username cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void findByUsername_NullUsername_ThrowsIllegalArgumentException() {
        String username = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.findByUsername(username);
        });

        String expectedMessage = "Username cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void findBoardsByUser_ValidUserId_ReturnsTaskBoardResponseList() {
        UUID userId = UUID.randomUUID();
        User owner = new User();
        TaskBoardResponse mockBoard1 = new TaskBoardResponse();
        TaskBoardResponse mockBoard2 = new TaskBoardResponse();
        mockBoard1.setId(UUID.randomUUID());
        mockBoard2.setId(UUID.randomUUID());

        List<TaskBoardResponse> mockBoards = List.of(mockBoard1, mockBoard2);

        Mockito.when(userDAO.findById(userId)).thenReturn(Optional.of(owner));
        Mockito.when(userDAO.findRelatedEntitiesByUUID(userId)).thenReturn(mockBoards);

        List<TaskBoardResponse> responseBoards = userService.findBoardsByUser(userId);

        assertEquals(responseBoards.size(), 2);
        assertEquals(responseBoards.get(0).getId(), mockBoard1.getId());
        assertEquals(responseBoards.get(1).getId(), mockBoard2.getId());

        Mockito.verify(userDAO, Mockito.times(1)).findById(userId);
        Mockito.verify(userDAO, Mockito.times(1)).findRelatedEntitiesByUUID(userId);
    }

    @Test
    void findBoardsByUser_ValidUserIdZeroTaskBoards_ReturnsEmptyTaskBoardList() {
        UUID userId = UUID.randomUUID();
        User owner = new User();

        Mockito.when(userDAO.findById(userId)).thenReturn(Optional.of(owner));
        Mockito.when(userDAO.findRelatedEntitiesByUUID(userId)).thenReturn(List.of());

        List<TaskBoardResponse> responseBoards = userService.findBoardsByUser(userId);

        assertEquals(responseBoards.size(), 0);

        Mockito.verify(userDAO, Mockito.times(1)).findById(userId);
        Mockito.verify(userDAO, Mockito.times(1)).findRelatedEntitiesByUUID(userId);
    }

    @Test
    void findBoardsByUser_InValidUserId_ThrowsEntityNotFoundException() {
        UUID userId = UUID.randomUUID();

        Mockito.when(userDAO.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
           userService.findBoardsByUser(userId);
        });

        String expectedMessage = "User not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void create_ValidUserData_NewUsersDataIsIdenticalAndReturnsUserResponse() {
        User user = new User();
        user.setUsername("Created User 1");
        user.setPassword("userpass");
        user.setEmail("usermail@mail.mail");
        user.setRole(Role.USER);
        user.setTaskBoards(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userDAO.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.when(userDAO.isUserExists(ArgumentMatchers.any(User.class))).thenReturn(false);
        Mockito.when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedUserPass");

        UserResponse response = userService.create(user);

        User createdUser = captor.getValue();

        assertEquals(user.getUsername(), createdUser.getUsername());
        assertEquals(user.getEmail(), createdUser.getEmail());
        assertEquals(user.getRole(), createdUser.getRole());
        assertNotEquals(user.getPassword(), createdUser.getPassword());

        assertNotNull(response);
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRole(), response.getRole());


        Mockito.verify(userDAO, Mockito.times(1)).save(captor.capture());
        Mockito.verify(userDAO, Mockito.times(1)).isUserExists(ArgumentMatchers.any(User.class));
    }

    @Test
    void create_EmptyUserName_ThrowsIllegalArgumentException() {
        String username = "";

        User user = new User();
        user.setUsername(username);
        user.setEmail("mail");
        user.setPassword("userpass");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.create(user);
        });

        String expectedMessage = "Username cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void create_NullUserName_ThrowsIllegalArgumentException() {
        String username = null;

        User user = new User();
        user.setUsername(username);
        user.setEmail("mail");
        user.setPassword("userpass");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
           userService.create(user);
        });

        String expectedMessage = "Username cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void create_EmptyEmail_ThrowsIllegalArgumentException() {
        String username = "Created User 1";
        String email = "";

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("userpass");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.create(user);
        });

        String expectedMessage = "User email cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void create_NullEmail_ThrowsIllegalArgumentException() {
        String username = "Created User 1";
        String email = null;

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("userpass");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.create(user);
        });

        String expectedMessage = "User email cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void create_EmptyPassword_ThrowsIllegalArgumentException() {
        String username = "Created User 1";
        String email = "mail";
        String password = "";

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.create(user);
        });

        String expectedMessage = "User password cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void create_NullPassword_ThrowsIllegalArgumentException() {
        String username = "Created User 1";
        String email = "mail";
        String password = null;

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.create(user);
        });

        String expectedMessage = "User password cannot be null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void create_UserAlreadyExists_ThrowsEntityAlreadyExistsException() {
        UUID userId = UUID.randomUUID();
        String username = "Created User 1";
        String email = "mail";
        String password = "userpass";

        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        Mockito.when(userDAO.isUserExists(ArgumentMatchers.any(User.class))).thenReturn(true);

        EntityAlreadyExistsException exception = assertThrows(EntityAlreadyExistsException.class, () -> {
            userService.create(user);
        });

        String expectedMessage = "User already exists";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void update_ValidUserData_UsersDataChangedAndReturnsUserResponse() {
        UUID userId = UUID.randomUUID();

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("Created User 1");
        existingUser.setPassword("userpass");
        existingUser.setEmail("mail");
        existingUser.setRole(Role.USER);
        existingUser.setTaskBoards(null);

        User updateData = new User();
        updateData.setId(userId);
        updateData.setUsername("Updated User 1");
        updateData.setPassword("changedpass");
        updateData.setEmail("umal@mail.mail");
        updateData.setRole(Role.USER);
        updateData.setTaskBoards(null);

        Mockito.when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        Mockito.when(passwordEncoder.encode(updateData.getPassword())).thenReturn("newEncodedPass");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response  = userService.update(userId, updateData);

        User updatedUser = captor.getValue();


        assertEquals("Updated User 1", updatedUser.getUsername());
        assertEquals("umal@mail.mail", updatedUser.getEmail());
        assertEquals(Role.USER, updatedUser.getRole());
        assertNotEquals("changedpass", updatedUser.getPassword());
        assertNotEquals("userpass", updatedUser.getPassword());

        assertNotNull(response);
        assertEquals(updateData.getUsername(), response.getUsername());
        assertEquals(updateData.getEmail(), response.getEmail());
        assertEquals(updateData.getRole(), response.getRole());

        Mockito.verify(userDAO, Mockito.times(2)).findById(userId);
        Mockito.verify(userDAO, Mockito.times(1)).update(captor.capture());
    }

    @Test
    void update_AllUserFieldsIsNull_UsersDataDidNotChangedAndReturnsUserResponse() {
        UUID userId = UUID.randomUUID();

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("Created User 1");
        existingUser.setPassword("userpass");
        existingUser.setEmail("mail");
        existingUser.setRole(Role.USER);
        existingUser.setTaskBoards(null);

        User updateData = new User();

        Mockito.when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        Mockito.when(userDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.update(userId, updateData);

        User updatedUser = captor.getValue();

        assertEquals("Created User 1", updatedUser.getUsername());
        assertEquals("userpass", updatedUser.getPassword());
        assertEquals("mail", updatedUser.getEmail());
        assertEquals(Role.USER, updatedUser.getRole());

        assertNotNull(response);
        assertEquals("Created User 1", response.getUsername());
        assertEquals("mail", response.getEmail());
        assertEquals(Role.USER, response.getRole());

        Mockito.verify(userDAO, Mockito.times(2)).findById(userId);
        Mockito.verify(userDAO, Mockito.times(1)).update(captor.capture());
    }

    @Test
    void update_AllUserFieldsAreEmpty_UsersDataDidNotChangedAndReturnsUserResponse() {
        UUID userId = UUID.randomUUID();

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("Created User 1");
        existingUser.setPassword("userpass");
        existingUser.setEmail("mail");
        existingUser.setRole(Role.USER);
        existingUser.setTaskBoards(null);

        User updateData = new User();
        updateData.setUsername("");
        updateData.setPassword("");
        updateData.setEmail("");

        Mockito.when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        Mockito.when(userDAO.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.update(userId, updateData);

        User updatedUser = captor.getValue();

        assertEquals("Created User 1", updatedUser.getUsername());
        assertEquals("userpass", updatedUser.getPassword());
        assertEquals("mail", updatedUser.getEmail());
        assertEquals(Role.USER, updatedUser.getRole());

        assertNotNull(response);
        assertEquals("Created User 1", response.getUsername());
        assertEquals("mail", response.getEmail());
        assertEquals(Role.USER, response.getRole());

        Mockito.verify(userDAO, Mockito.times(2)).findById(userId);
        Mockito.verify(userDAO, Mockito.times(1)).update(captor.capture());
    }

    @Test
    void update_InvalidUserId_ThrowsEntityNotFoundException() {
        UUID userId = UUID.randomUUID();
        User user = new User();

        Mockito.when(userDAO.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.update(userId, user);
        });

        String expectedMessage = "Database entry is missing";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void delete_ValidUserId_UserDeletedSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = new User();

        Mockito.doNothing().when(userDAO).delete(userId);
        Mockito.when(userDAO.findById(userId)).thenReturn(Optional.of(user));

        userService.delete(userId);

        Mockito.verify(userDAO, Mockito.times(1)).delete(userId);
    }

    @Test
    void delete_InValidUserId_ThrowsEntityNotFoundException() {
        UUID userId = UUID.randomUUID();

        Mockito.when(userDAO.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.delete(userId);
        });

        String expectedMessage = "User not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
