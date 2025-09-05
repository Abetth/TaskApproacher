package com.taskapproacher.service.task;

import com.taskapproacher.dao.task.TaskDAO;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.enums.Priority;
import com.taskapproacher.entity.task.response.TaskResponse;
import com.taskapproacher.entity.task.request.TaskRequest;
import com.taskapproacher.enums.ErrorMessage;

import com.taskapproacher.entity.task.TaskBoard;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskDAO taskDAO;
    private final TaskBoardService taskBoardService;

    @Autowired
    public TaskService(TaskDAO taskDAO, TaskBoardService taskBoardService) {
        this.taskDAO = taskDAO;
        this.taskBoardService = taskBoardService;
    }

    public Task findById(UUID taskId) throws IllegalArgumentException, EntityNotFoundException {
        if (taskId == null) {
            throw new IllegalArgumentException("Task id " + ErrorMessage.NULL);
        }

        return taskDAO.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Task " + ErrorMessage.NOT_FOUND));
    }


    public TaskResponse create(UUID boardId, TaskRequest request, String timeZone) throws IllegalArgumentException {
        TaskBoard boardForTask = taskBoardService.findById(boardId);

        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            ErrorMessage error = (request.getTitle() == null) ? ErrorMessage.NULL : ErrorMessage.EMPTY;
            throw new IllegalArgumentException("Title " + error);
        }

        if (request.getPriority() == null || request.getPriority().isEmpty()) {
            request.setPriority(Priority.STANDARD.toString());
        }

        if (request.getDeadline() == null || request.getDeadline().isBefore(ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDate())) {
            ErrorMessage error = (request.getDeadline() == null) ? ErrorMessage.NULL : ErrorMessage.BEFORE_CURRENT_DATE;
            throw new IllegalArgumentException("Task deadline " + error);
        }

        Task taskFromRequest = new Task(request);
        taskFromRequest.setTaskBoard(boardForTask);

        return new TaskResponse(taskDAO.save(taskFromRequest));
    }

    public TaskResponse update(UUID taskId, TaskRequest request, String timeZone) throws IllegalArgumentException {
        Task updatedTask = findById(taskId);

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
                throw new IllegalArgumentException("Task deadline " + ErrorMessage.BEFORE_CURRENT_DATE);
            } else {
                updatedTask.setDeadline(request.getDeadline());
            }
        }

        updatedTask.setFinished(request.isFinished());

        if (request.getTaskBoard() != null) {
            updatedTask.setTaskBoard(request.getTaskBoard());
        }

        return new TaskResponse(taskDAO.update(updatedTask));
    }

    public void delete(UUID taskId) throws IllegalArgumentException {
        if (taskId == null) {
            throw new IllegalArgumentException("Task id " + ErrorMessage.NULL);
        }

        taskDAO.delete(taskId);
    }
}
