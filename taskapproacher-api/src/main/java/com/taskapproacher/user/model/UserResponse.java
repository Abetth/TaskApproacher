package com.taskapproacher.user.model;

import com.taskapproacher.user.constant.Role;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID ID;
    private String username;
    private String email;
    private Role role;

    public UserResponse(User user) {
        this.ID = user.getID();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
    }

    @Override
    public String toString() {
        return "[   User: " + ID + "\n"
                + "Username: " + username + "\n"
                + "E-mail: " + email + "    ]";
    }
}
