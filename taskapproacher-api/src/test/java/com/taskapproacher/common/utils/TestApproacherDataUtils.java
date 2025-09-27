package com.taskapproacher.common.utils;

import com.taskapproacher.common.constant.EntityNumber;
import com.taskapproacher.task.constant.Priority;
import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.user.constant.Role;
import com.taskapproacher.user.model.User;

import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

public class TestApproacherDataUtils {
    public static User createPreInsertedUser(EntityNumber entityNumber) {
        User user = new User();

        switch (entityNumber) {
            case FIRST:
                user.setID(UUID.fromString("ca3912d5-b430-4018-b859-d4e804c4075a"));
                user.setUsername("TestUser1");
                user.setPassword("userpass");
                user.setEmail("mail@mail.mail");
                user.setRole(Role.USER);
                break;

            case SECOND:
                user.setID(UUID.fromString("db4123e6-b430-4018-b859-e5f915d5186b"));
                user.setUsername("TestUser2");
                user.setPassword("userpass");
                user.setEmail("m@mail.mail");
                user.setRole(Role.USER);
                break;

            default:
                throw new IllegalArgumentException("Unknown entity number: " + entityNumber);
        }

        User result = new User();
        BeanUtils.copyProperties(user, result);

        return result;
    }

    public static TaskBoard createPreInsertedTaskBoard(EntityNumber entityNumber) {
        TaskBoard taskBoard = new TaskBoard();

        final User DEFINED_FIRST_USER = createPreInsertedUser(EntityNumber.FIRST);
        final User DEFINED_SECOND_USER = createPreInsertedUser(EntityNumber.SECOND);

        switch (entityNumber) {
            case FIRST:
                taskBoard.setID(UUID.fromString("777886a8-ce33-474c-8e31-9bfefd4b8fd0"));
                taskBoard.setTitle("Task Board 1");
                taskBoard.setSorted(true);
                taskBoard.setTasks(new ArrayList<>());
                taskBoard.setUser(DEFINED_FIRST_USER);
                break;

            case SECOND:
                taskBoard.setID(UUID.fromString("537812a6-ce21-474c-8e31-9bfefd4b8fd1"));
                taskBoard.setTitle("Task Board 2");
                taskBoard.setSorted(true);
                taskBoard.setTasks(new ArrayList<>());
                taskBoard.setUser(DEFINED_FIRST_USER);
                break;

            case THIRD:
                taskBoard.setID(UUID.fromString("27e22265-e42e-4cb1-b5f1-ac7a0c5d81aa"));
                taskBoard.setTitle("Task Board 3");
                taskBoard.setSorted(false);
                taskBoard.setTasks(new ArrayList<>());
                taskBoard.setUser(DEFINED_SECOND_USER);
                break;

            default:
                throw new IllegalArgumentException("Unknown entity number: " + entityNumber);
        }

        TaskBoard result = new TaskBoard();
        BeanUtils.copyProperties(taskBoard, result);

        return result;
    }

    public static Task createPreInsertedTask(EntityNumber entityNumber) {
        Task task = new Task();

        final TaskBoard DEFINED_FIRST_TASK_BOARD = createPreInsertedTaskBoard(EntityNumber.FIRST);
        final TaskBoard DEFINED_THIRD_TASK_BOARD = createPreInsertedTaskBoard(EntityNumber.THIRD);

        switch (entityNumber) {
            case FIRST:
                task.setID(UUID.fromString("cffbbd8f-3631-42a8-b63c-7ce3173b1b19"));
                task.setTitle("Task 1");
                task.setDescription("Task 1 description");
                task.setFinished(true);
                task.setDeadline(LocalDate.of(2050, 9, 17));
                task.setPriority(Priority.fromInt(4));
                task.setTaskBoard(DEFINED_FIRST_TASK_BOARD);
                break;

            case SECOND:
                task.setID(UUID.fromString("130cd8a4-57c2-475c-9ff4-c0953eb90d54"));
                task.setTitle("Task 2");
                task.setDescription("Task 2 description");
                task.setFinished(false);
                task.setDeadline(LocalDate.of(2080, 10, 17));
                task.setPriority(Priority.fromInt(2));
                task.setTaskBoard(DEFINED_FIRST_TASK_BOARD);
                break;

            case THIRD:
                task.setID(UUID.fromString("52c2635e-080b-4d39-b4dc-9c14b2095f58"));
                task.setTitle("Task 3");
                task.setDescription("Task 3 description");
                task.setFinished(true);
                task.setDeadline(LocalDate.of(2060, 6, 17));
                task.setPriority(Priority.fromInt(2));
                task.setTaskBoard(DEFINED_THIRD_TASK_BOARD);
                break;

            default:
                throw new IllegalArgumentException("Unknown entity number: " + entityNumber);
        }

        Task result = new Task();
        BeanUtils.copyProperties(task, result);

        return result;
    }
}
