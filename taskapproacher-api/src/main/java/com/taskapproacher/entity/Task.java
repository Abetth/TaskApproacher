package com.taskapproacher.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "title", nullable = false)
    private String title;

    @Setter
    @Column(name = "description")
    private String description;

    @Setter
    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Setter
    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @Setter
    @Column(name = "status", nullable = false)
    private boolean status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_board_id")
    @JsonBackReference
    private TaskBoard taskBoard;

    public Task(String title, String description, int priority, LocalDate deadline, boolean status, TaskBoard list) {
        this.id = 0L;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.deadline = deadline;
        this.status = status;
        this.taskBoard = list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task comparableTask = (Task) o;

        return id.equals(comparableTask.id) && title.equals(comparableTask.title)
                && description.equals(comparableTask.description) && priority.equals(comparableTask.priority)
                && deadline.equals(comparableTask.deadline) && status == comparableTask.status
                && taskBoard.equals(comparableTask.taskBoard);
    }

    @Override
    public int hashCode() {
        return 11 + id.intValue() + title.hashCode() + description.hashCode() + priority + deadline.hashCode()
                + taskBoard.hashCode();
    }

    @Override
    public String toString() {
        return  "[   Task: " + id + "\n"
                + "Title: " + title + "\n"
                + "Description: " + description + "\n"
                + "Priority: " + priority + "\n"
                + "Deadline: " + deadline + "\n"
                + "Status: " + status + "\n"
                + "Task List: " + taskBoard + "    ]";
    }

}
