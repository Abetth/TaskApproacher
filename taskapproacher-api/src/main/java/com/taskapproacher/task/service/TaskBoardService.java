package com.taskapproacher.task.service;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.model.TaskBoardResponse;
import com.taskapproacher.task.model.TaskResponse;
import com.taskapproacher.task.repository.TaskBoardRepository;
import com.taskapproacher.user.service.UserService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskBoardService {
    private final TaskBoardRepository taskBoardRepository;
    private final UserService userService;

    @Autowired
    public TaskBoardService(TaskBoardRepository taskBoardRepository, UserService userService) {
        this.taskBoardRepository = taskBoardRepository;
        this.userService = userService;
    }

    public TaskBoard findByID(UUID taskBoardID) throws IllegalArgumentException, EntityNotFoundException {
        if (taskBoardID == null) {
            throw new IllegalArgumentException("Task board id " + ExceptionMessage.NULL);
        }

        return taskBoardRepository.findByID(taskBoardID).orElseThrow(
                () -> new EntityNotFoundException("Task board " + ExceptionMessage.NOT_FOUND)
        );
    }

    public List<TaskResponse> findByTaskBoard(UUID taskBoardID)
            throws IllegalArgumentException, EntityNotFoundException {
        findByID(taskBoardID);

        List<Task> tasks = taskBoardRepository.findRelatedEntitiesByID(taskBoardID);

        return tasks.stream().map(TaskResponse::new).collect(Collectors.toList());
    }

    public TaskBoardResponse createTaskBoard(UUID userID, TaskBoard taskBoard)
            throws IllegalArgumentException, EntityNotFoundException {
        if (taskBoard.getTitle() == null || taskBoard.getTitle().isEmpty()) {
            ExceptionMessage error = (taskBoard.getTitle() == null) ? ExceptionMessage.NULL : ExceptionMessage.EMPTY;

            throw new IllegalArgumentException("Title " + error);
        }

        taskBoard.setUser(userService.findByID(userID));

        return new TaskBoardResponse(taskBoardRepository.save(taskBoard));
    }

    public TaskBoardResponse updateTaskBoard(UUID taskBoardID, TaskBoard taskBoard)
            throws IllegalArgumentException, EntityNotFoundException {
        TaskBoard updatedBoard = findByID(taskBoardID);

        if (taskBoard.getTitle() != null && !taskBoard.getTitle().isEmpty()) {
            updatedBoard.setTitle(taskBoard.getTitle());
        }
        updatedBoard.setSorted(taskBoard.isSorted());

        return new TaskBoardResponse(taskBoardRepository.update(updatedBoard));
    }

    public void deleteTaskBoard(UUID taskBoardID) throws IllegalArgumentException {
        if (taskBoardID == null) {
            throw new IllegalArgumentException("Board id " + ExceptionMessage.NULL);
        }

        TaskBoard taskBoard = findByID(taskBoardID);

        taskBoardRepository.delete(taskBoard);
    }
}
