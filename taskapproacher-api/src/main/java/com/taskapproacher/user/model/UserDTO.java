package com.taskapproacher.user.model;

import com.taskapproacher.user.constant.Role;

import lombok.Value;

import java.util.UUID;

@Value
public class UserDTO {
    UUID ID;
    String username;
    String email;
    Role role;
}
