package com.taskapproacher.entity.user;

import com.taskapproacher.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private Role role;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserResponse comparable = (UserResponse) o;

        return username.equals(comparable.username) && email.equals(comparable.email) && role.equals(comparable.role);
    }

    @Override
    public String toString() {
        return "[   User: " + id + "\n"
                + "Username: " + username + "\n"
                + "E-mail: " + email + "    ]";
    }
}
