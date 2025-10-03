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
public class UserDTO {
    private UUID ID;
    private String username;
    private String email;
    private Role role;
}
