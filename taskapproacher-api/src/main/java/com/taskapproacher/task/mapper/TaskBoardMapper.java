package com.taskapproacher.task.mapper;

import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.model.TaskBoardDTO;

public class TaskBoardMapper {
    public TaskBoardDTO mapToTaskBoardDTO(TaskBoard taskBoard) {
        TaskBoardDTO response = new TaskBoardDTO();
        response.setID(taskBoard.getID());
        response.setTitle(taskBoard.getTitle());
        response.setSorted(taskBoard.isSorted());
        response.setTasks(taskBoard.getTasks());
        response.setUserID(taskBoard.getUserID());

        return response;
    }

    public TaskBoard mapToTaskBoardEntity(TaskBoardDTO dto) {
        TaskBoard taskBoard = new TaskBoard();
        taskBoard.setID(dto.getID());
        taskBoard.setTitle(dto.getTitle());
        taskBoard.setSorted(dto.isSorted());
        taskBoard.setTasks(dto.getTasks());

        return taskBoard;
    }
}
