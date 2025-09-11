package com.taskapproacher.entity.task;

import com.fasterxml.jackson.annotation.JsonBackReference;

import com.taskapproacher.constant.Priority;
import com.taskapproacher.entity.task.request.TaskRequest;
import com.taskapproacher.interfaces.TaskMatcher;

import jakarta.persistence.*;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicUpdate
@Table(name = "tasks")
public class Task implements TaskMatcher {
    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID ID;

    @Setter
    @Column(name = "title", nullable = false)
    private String title;

    @Setter
    @Column(name = "description")
    private String description;

    @Column(name = "priority", nullable = false)
    private Priority priority;

    @Setter
    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @Setter
    @Column(name = "finished", nullable = false)
    private boolean finished;

    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_board_id")
    @JsonBackReference
    private TaskBoard taskBoard;

    public Task(TaskRequest request) {
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.priority = Priority.valueOf(request.getPriority());
        this.deadline = request.getDeadline();
        this.finished = request.isFinished();
        this.taskBoard = request.getTaskBoard();
    }

    public Task(String title, String description, int priority, LocalDate deadline, boolean finished, TaskBoard list) {
        this.ID = new UUID(0L, 0L);
        this.title = title;
        this.description = description;
        this.priority = Priority.fromInt(priority);
        this.deadline = deadline;
        this.finished = finished;
        this.taskBoard = list;
    }

    public Task(String title, String description, Priority priority, LocalDate deadline, boolean finished, TaskBoard list) {
        this.ID = new UUID(0L, 0L);
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.deadline = deadline;
        this.finished = finished;
        this.taskBoard = list;
    }

    public void setPriority(int priority) {
        if (priority <= 0 || priority >= 11) {
            throw new IllegalArgumentException("Priority out of bounds. Only values from 1 to 10 acceptable");
        }
        this.priority = Priority.fromInt(priority);
    }

    public void setPriority(String priority) {
        this.priority = Priority.valueOf(priority);
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getPriority() {
        return this.priority.toString();
    }

    public Priority getEnumPriority() {
        return this.priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task comparableTask = (Task) o;

        return title.equals(comparableTask.title)
                && description.equals(comparableTask.description) && priority.equals(comparableTask.priority)
                && deadline.equals(comparableTask.deadline) && finished == comparableTask.finished
                && taskBoard.equals(comparableTask.taskBoard);
    }

    @Override
    public int hashCode() {
        return 11 + title.hashCode() + description.hashCode() + priority.getPriority()
                + deadline.hashCode() + taskBoard.hashCode();
    }
}
