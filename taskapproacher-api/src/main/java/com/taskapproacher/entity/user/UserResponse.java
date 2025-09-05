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
}
