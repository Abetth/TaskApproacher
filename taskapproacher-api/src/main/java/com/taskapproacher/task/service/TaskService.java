package com.taskapproacher.task.service;

import com.taskapproacher.common.constant.ExceptionMessage;
import com.taskapproacher.task.constant.Priority;
import com.taskapproacher.task.mapper.TaskMapper;
import com.taskapproacher.task.model.*;

import com.taskapproacher.task.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;
    private final TaskBoardService taskBoardService;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskBoardService taskBoardService) {
        this.taskMapper = new TaskMapper();
        this.taskRepository = taskRepository;
        this.taskBoardService = taskBoardService;
    }

    public Task findByID(UUID taskID) throws IllegalArgumentException, EntityNotFoundException {
        if (taskID == null) {
            throw new IllegalArgumentException("Task id " + ExceptionMessage.NULL);
        }

        return taskRepository.findByID(taskID).orElseThrow(
                () -> new EntityNotFoundException("Task " + ExceptionMessage.NOT_FOUND)
        );
    }


    public TaskDTO createTask(UUID boardId, TaskDTO request, String timeZone)
            throws IllegalArgumentException, EntityNotFoundException {
        TaskBoard taskBoard = taskBoardService.findByID(boardId);

        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            ExceptionMessage error = (request.getTitle() == null)
                    ? ExceptionMessage.NULL
                    : ExceptionMessage.EMPTY;

            throw new IllegalArgumentException("Title " + error);
        }

        if (request.getPriority() == null) {
            throw new IllegalArgumentException("Priority " + ExceptionMessage.NULL);
        }

        if (request.getDeadline() == null) {
            throw new IllegalArgumentException("Task deadline " + ExceptionMessage.NULL);
        }

        if (request.getDeadline().isBefore(ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate())) {
            throw new IllegalArgumentException("Task deadline " + ExceptionMessage.BEFORE_CURRENT_DATE);
        }

        Task task = taskMapper.mapToTaskEntity(request);
        task.setTaskBoard(taskBoard);

        return taskMapper.mapToTaskDTO(taskRepository.save(task));
    }

    public TaskDTO updateTask(UUID taskID, TaskDTO request, String timeZone)
            throws IllegalArgumentException, EntityNotFoundException {
        Task task = findByID(taskID);

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            task.setDescription(request.getDescription());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getDeadline() != null) {
            if (request.getDeadline().isBefore(ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate())) {
                throw new IllegalArgumentException("Task deadline " + ExceptionMessage.BEFORE_CURRENT_DATE);
            } else {
                task.setDeadline(request.getDeadline());
            }
        }

        task.setFinished(request.isFinished());

        if (request.getTaskBoardID() != null) {
            task.setTaskBoard(taskBoardService.findByID(request.getTaskBoardID()));
        }

        return taskMapper.mapToTaskDTO(taskRepository.update(task));
    }

    public void deleteTask(UUID taskID) throws IllegalArgumentException {
        if (taskID == null) {
            throw new IllegalArgumentException("Task id " + ExceptionMessage.NULL);
        }

        Task task = findByID(taskID);

        taskRepository.delete(task);
    }
}
