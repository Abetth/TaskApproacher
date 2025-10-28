package com.taskapproacher.task.mapper;

import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.task.model.TaskBoardDTO;

public class TaskBoardMapper {
    public TaskBoardDTO mapToTaskBoardDTO(TaskBoard taskBoard) {
        return new TaskBoardDTO(taskBoard.getID(), taskBoard.getTitle(), taskBoard.isSorted(),
                                taskBoard.getTasks(), taskBoard.getUserID());
    }

    public TaskBoard mapToTaskBoardEntity(TaskBoardDTO dto) {
        return new TaskBoard(dto.getID(), dto.getTitle(), dto.isSorted(), dto.getTasks());
    }
}
