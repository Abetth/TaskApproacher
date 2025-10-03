package com.taskapproacher.task.service;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.task.mapper.TaskBoardMapper;
import com.taskapproacher.task.mapper.TaskMapper;
import com.taskapproacher.task.model.Task;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.model.TaskBoardDTO;
import com.taskapproacher.task.model.TaskDTO;
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
    private final TaskMapper taskMapper;
    private final TaskBoardMapper taskBoardMapper;
    private final TaskBoardRepository taskBoardRepository;
    private final UserService userService;

    @Autowired
    public TaskBoardService(TaskBoardRepository taskBoardRepository, UserService userService) {
        this.taskMapper = new TaskMapper();
        this.taskBoardMapper = new TaskBoardMapper();
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

    public List<TaskDTO> findByTaskBoard(UUID taskBoardID)
            throws IllegalArgumentException, EntityNotFoundException {
        findByID(taskBoardID);

        List<Task> tasks = taskBoardRepository.findRelatedEntitiesByID(taskBoardID);

        return tasks.stream().map(taskMapper::mapToTaskDTO).collect(Collectors.toList());
    }

    public TaskBoardDTO createTaskBoard(UUID userID, TaskBoardDTO request)
            throws IllegalArgumentException, EntityNotFoundException {
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            ExceptionMessage error = (request.getTitle() == null) ? ExceptionMessage.NULL : ExceptionMessage.EMPTY;

            throw new IllegalArgumentException("Title " + error);
        }

        TaskBoard taskBoardFromRequest = taskBoardMapper.mapToTaskBoardEntity(request);
        taskBoardFromRequest.setUser(userService.findByID(userID));

        return taskBoardMapper.mapToTaskBoardDTO(taskBoardRepository.save(taskBoardFromRequest));
    }

    public TaskBoardDTO updateTaskBoard(UUID taskBoardID, TaskBoardDTO request)
            throws IllegalArgumentException, EntityNotFoundException {
        TaskBoard updatedBoard = findByID(taskBoardID);

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            updatedBoard.setTitle(request.getTitle());
        }
        updatedBoard.setSorted(request.isSorted());

        return taskBoardMapper.mapToTaskBoardDTO(taskBoardRepository.update(updatedBoard));
    }

    public void deleteTaskBoard(UUID taskBoardID) throws IllegalArgumentException {
        if (taskBoardID == null) {
            throw new IllegalArgumentException("Board id " + ExceptionMessage.NULL);
        }

        TaskBoard taskBoard = findByID(taskBoardID);

        taskBoardRepository.delete(taskBoard);
    }
}
