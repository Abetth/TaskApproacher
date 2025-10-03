package com.taskapproacher.task.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import com.taskapproacher.common.interfaces.matcher.TaskBoardMatcher;
import com.taskapproacher.task.constant.TaskConstants;
import com.taskapproacher.user.model.User;

import jakarta.persistence.*;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.DynamicUpdate;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicUpdate
@Table(name = "task_boards")
public class TaskBoard implements TaskBoardMatcher {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID ID;

    @Column(name = "title", nullable = false)
    @Size(max = TaskConstants.MAX_TASK_BOARD_TITLE_LENGTH)
    private String title;

    @Column(name = "sorted", nullable = false)
    private boolean sorted;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "taskBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Task> tasks;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    public UUID getUserID() {
        return this.user.getID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskBoard comparableTable = (TaskBoard) o;

        return title.equals(comparableTable.title)
               && sorted == comparableTable.sorted
               && tasks.equals(comparableTable.tasks)
               && user.equals(comparableTable.user);
    }

    @Override
    public int hashCode() {
        return 11 + title.hashCode() + Boolean.hashCode(sorted) + tasks.hashCode() + user.hashCode();
    }

    @Override
    public String toString() {
        return "[   Table: " + ID
               + ", Title: " + title
               + ", Sorted: " + sorted
               + "User: " + user.getUsername() + "    ]";
    }
}
