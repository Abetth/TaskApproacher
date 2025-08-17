package com.taskapproacher.entity.task;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.taskapproacher.entity.user.User;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "task_boards")
public class TaskBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Setter
    @Column(name = "title", nullable = false)
    private String title;

    @Setter
    @JsonProperty("isSorted")
    private boolean isSorted;

    @Setter
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "taskBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Task> tasks;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    public TaskBoard(String title, boolean isSorted, List<Task> tasks, User user) {
        this.id = new UUID(0L, 0L);
        this.title = title;
        this.isSorted = isSorted;
        this.tasks = tasks;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskBoard comparableTable = (TaskBoard) o;
        return id.equals(comparableTable.id) && title.equals(comparableTable.title)
                && tasks.equals(comparableTable.tasks) && user.equals(comparableTable.user);
    }

    @Override
    public int hashCode() {
        return 11 + id.hashCode() + title.hashCode() + tasks.hashCode() + user.hashCode();
    }

    @Override
    public String toString() {
        return  "[   Table: " + id
                + ", Title: " + title
                + ", Sorted: " + isSorted
                + "User: " + user.getUsername() + "    ]";
    }
}
