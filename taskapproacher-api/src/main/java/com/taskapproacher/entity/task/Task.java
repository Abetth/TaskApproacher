package com.taskapproacher.entity.task;

import com.fasterxml.jackson.annotation.JsonBackReference;

import com.taskapproacher.entity.user.User;
import jakarta.persistence.*;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DynamicUpdate
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @Column(name = "status", nullable = false)
    private boolean status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_board_id")
    @JsonBackReference
    private TaskBoard taskBoard;

    public Task(String title, String description, int priority, LocalDate deadline, boolean status, TaskBoard list) {
        this.id = new UUID(0L, 0L);
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
        return 11 + id.hashCode() + title.hashCode() + description.hashCode() + priority + deadline.hashCode()
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
