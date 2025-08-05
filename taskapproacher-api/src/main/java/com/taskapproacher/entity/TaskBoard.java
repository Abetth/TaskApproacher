package com.taskapproacher.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "task_boards")
public class TaskBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public TaskBoard(String title, boolean isSorted, List<Task> tasks) {
        this.id = 0L;
        this.title = title;
        this.isSorted = isSorted;
        this.tasks = tasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskBoard comparableTable = (TaskBoard) o;
        return id.equals(comparableTable.id) && title.equals(comparableTable.title)
                && tasks.equals(comparableTable.tasks);
    }

    @Override
    public int hashCode() {
        return 11 + id.intValue() + title.hashCode() + tasks.hashCode();
    }

    @Override
    public String toString() {
        return  "[   Table: " + id + ", Title: " + title + ", Sorted: " + isSorted + "    ]";
    }
}
