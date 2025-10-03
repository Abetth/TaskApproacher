package com.taskapproacher.task.model;

import com.taskapproacher.common.interfaces.matcher.TaskMatcher;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
public class TaskDTO implements TaskMatcher {
        private UUID ID;
        private String title;
        private String description;
        private String priority;
        private LocalDate deadline;
        private boolean finished;
        private UUID taskBoardID;

        public String getPriorityAsString() {
            return this.priority;
        }
}
