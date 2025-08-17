package com.taskapproacher.entity.user;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.taskapproacher.entity.task.TaskBoard;
import com.taskapproacher.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Setter
    @Column(nullable = false, name = "username")
    private String username;

    @Setter
    @Column(nullable = false, name = "user_password")
    private String password;

    @Setter
    @Column(nullable = false, name = "email_address")
    private String email;

    @Setter
    private Role role;

    @Setter
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TaskBoard> taskBoards;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User comparable = (User) o;

        return id.equals(comparable.id) && username.equals(comparable.username) && email.equals(comparable.username);
    }

    @Override
    public int hashCode() {
        return 11 + id.hashCode() + username.hashCode() + email.hashCode();
    }

    @Override
    public String toString() {
        return "[   User: " + id + "\n"
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
