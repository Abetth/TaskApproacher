package com.taskapproacher.service.task;

import com.taskapproacher.constant.ExceptionMessage;
import com.taskapproacher.constant.Priority;
import com.taskapproacher.repository.task.TaskRepository;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.request.TaskRequest;
import com.taskapproacher.entity.task.response.TaskResponse;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskBoardService taskBoardService;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskBoardService taskBoardService) {
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


    public TaskResponse create(UUID boardId, TaskRequest request, String timeZone)
            throws IllegalArgumentException, EntityNotFoundException {
        TaskBoard boardForTask = taskBoardService.findByID(boardId);

        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            ExceptionMessage error = (request.getTitle() == null)
                    ? ExceptionMessage.NULL
                    : ExceptionMessage.EMPTY;

            throw new IllegalArgumentException("Title " + error);
        }

        if (request.getPriority() == null || request.getPriority().isEmpty()) {
            request.setPriority(Priority.STANDARD.toString());
        }

        if ((request.getDeadline() == null)
            || request.getDeadline().isBefore(ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate())) {

            ExceptionMessage error = (request.getDeadline() == null) ? ExceptionMessage.NULL
                    : ExceptionMessage.BEFORE_CURRENT_DATE;

            throw new IllegalArgumentException("Task deadline " + error);
        }

        Task taskFromRequest = new Task(request);
        taskFromRequest.setTaskBoard(boardForTask);

        return new TaskResponse(taskRepository.save(taskFromRequest));
    }

    public TaskResponse update(UUID taskID, TaskRequest request, String timeZone)
            throws IllegalArgumentException, EntityNotFoundException {
        Task updatedTask = findByID(taskID);

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            updatedTask.setTitle(request.getTitle());
        }

        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            updatedTask.setDescription(request.getDescription());
        }

        if (request.getPriority() != null && !request.getPriority().isEmpty()) {
            updatedTask.setPriority(request.getPriority());
        }

        if (request.getDeadline() != null) {
            if (request.getDeadline().isBefore(ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate())) {
                throw new IllegalArgumentException("Task deadline " + ExceptionMessage.BEFORE_CURRENT_DATE);
            } else {
                updatedTask.setDeadline(request.getDeadline());
            }
        }

        updatedTask.setFinished(request.isFinished());

        if (request.getTaskBoard() != null) {
            updatedTask.setTaskBoard(request.getTaskBoard());
        }

        return new TaskResponse(taskRepository.update(updatedTask));
    }

    public void delete(UUID taskID) throws IllegalArgumentException {
        if (taskID == null) {
            throw new IllegalArgumentException("Task id " + ExceptionMessage.NULL);
        }

        Task task = findByID(taskID);

        taskRepository.delete(task);
    }
}
