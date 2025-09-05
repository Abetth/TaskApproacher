package com.taskapproacher.service.task;

import com.taskapproacher.dao.task.TaskBoardDAO;
import com.taskapproacher.entity.task.Task;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.enums.ErrorMessage;
import com.taskapproacher.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskBoardService {
    private final TaskBoardDAO taskBoardDAO;
    private final UserService userService;

    @Autowired
    public TaskBoardService(TaskBoardDAO taskBoardDAO, UserService userService) {
        this.taskBoardDAO = taskBoardDAO;
        this.userService = userService;
    }

    public TaskBoard findByID(UUID boardID) throws IllegalArgumentException, EntityNotFoundException {
        if (boardID == null) {
            throw new IllegalArgumentException("Task board id " + ErrorMessage.NULL);
        }
        return taskBoardDAO.findByID(boardID).orElseThrow(() -> new EntityNotFoundException("Task board " + ErrorMessage.NOT_FOUND));
    }

    public List<Task> findByTaskBoard(UUID boardID) {
        findByID(boardID);

        return taskBoardDAO.findRelatedEntitiesByID(boardID);
    }

    public TaskBoardResponse create(UUID userID, TaskBoard taskBoard) throws IllegalArgumentException {
        if (taskBoard.getTitle() == null || taskBoard.getTitle().isEmpty()) {
            ErrorMessage error = (taskBoard.getTitle() == null) ? ErrorMessage.NULL : ErrorMessage.EMPTY;
            throw new IllegalArgumentException("Title " + error);
        }

        taskBoard.setUser(userService.findByID(userID));

        return new TaskBoardResponse(taskBoardDAO.save(taskBoard));
    }

    public TaskBoardResponse update(UUID boardID, TaskBoard taskBoard) {
        TaskBoard updatedBoard = findByID(boardID);

        if (taskBoard.getTitle() != null && !taskBoard.getTitle().isEmpty()) {
            updatedBoard.setTitle(taskBoard.getTitle());
        }
        updatedBoard.setSorted(taskBoard.isSorted());

        return new TaskBoardResponse(taskBoardDAO.update(updatedBoard));
    }

    public void delete(UUID boardID) throws IllegalArgumentException {
        if (boardID == null) {
            throw new IllegalArgumentException("Board id " + ErrorMessage.NULL);
        }

        taskBoardDAO.delete(boardID);
    }
}
