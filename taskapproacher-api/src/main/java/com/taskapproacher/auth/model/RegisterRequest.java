package com.taskapproacher.auth.model;

import lombok.Value;

@Value
public class RegisterRequest {
    String username;
    String password;
    String email;
}
