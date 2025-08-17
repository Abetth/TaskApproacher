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
        this.email = user.getUsername();
        this.role = user.getRole();
    }
}
