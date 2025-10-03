package com.taskapproacher.task.constant;

import java.util.Arrays;

public enum Priority {
    CRITICAL(1),
    HIGHEST(2),
    HIGH(3),
    STANDARD(4),
    MEDIUM_HIGH(5),
    MEDIUM(6),
    MEDIUM_LOW(7),
    LOW(8),
    LOWEST(9),
    NONE(10);

    private final Integer priority;

    Priority(Integer priority) {
        this.priority = priority;
    }

    public Integer getPriority() {
        return priority;
    }


}
