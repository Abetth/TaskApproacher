package com.taskapproacher.entity.task;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.taskapproacher.entity.task.response.TaskBoardResponse;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.interfaces.TaskBoardMatcher;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Table(name = "task_boards")
public class TaskBoard implements TaskBoardMatcher {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID ID;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "sorted", nullable = false)
    private boolean sorted;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "taskBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Task> tasks;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    public TaskBoard(TaskBoardResponse response) {
        this.ID = response.getID();
        this.title = response.getTitle();
        this.sorted = response.isSorted();
        this.tasks = response.getTasks();
        this.user = response.getUser();
    }

    public TaskBoard(String title, boolean sorted, List<Task> tasks, User user) {
        this.ID = new UUID(0L, 0L);
        this.title = title;
        this.sorted = sorted;
        this.tasks = tasks;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskBoard comparableTable = (TaskBoard) o;
        return ID.equals(comparableTable.ID) && title.equals(comparableTable.title)
                && tasks.equals(comparableTable.tasks) && user.equals(comparableTable.user);
    }

    @Override
    public int hashCode() {
        return 11 + ID.hashCode() + title.hashCode() + tasks.hashCode() + user.hashCode();
    }

    @Override
    public String toString() {
        return  "[   Table: " + ID
                + ", Title: " + title
                + ", Sorted: " + sorted
                + "User: " + user.getUsername() + "    ]";
    }
}
