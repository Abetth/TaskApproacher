package com.taskapproacher.user.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import com.taskapproacher.config.password.PasswordConstants;
import com.taskapproacher.task.model.TaskBoard;
import com.taskapproacher.user.constant.Role;

import com.taskapproacher.user.constant.UserConstants;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.DynamicUpdate;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicUpdate
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID ID;

    @Column(nullable = false, name = "username")
    @Size(min = UserConstants.MIN_USERNAME_LENGTH, max = UserConstants.MAX_USERNAME_LENGTH)
    private String username;

    // PasswordConstants is used to provide single constant per app. Can be duplicated in UserConstants if needed.
    @Column(nullable = false, name = "user_password")
    @Size(min = PasswordConstants.MIN_LENGTH)
    private String password;

    @Column(nullable = false, name = "email_address")
    private String email;

    @Column(nullable = false, name = "role")
    private Role role;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TaskBoard> taskBoards;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User comparable = (User) o;

        return username.equals(comparable.username) && email.equals(comparable.email) && role.equals(comparable.role);
    }

    @Override
    public int hashCode() {
        return 11 + username.hashCode() + email.hashCode() + role.hashCode();
    }

    @Override
    public String toString() {
        return "[   User: " + ID + "\n"
                + "Username: " + username + "\n"
                + "E-mail: " + email + "    ]";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
