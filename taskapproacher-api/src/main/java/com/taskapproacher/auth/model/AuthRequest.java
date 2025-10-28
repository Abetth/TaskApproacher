package com.taskapproacher.auth.model;

import lombok.Value;

@Value
public class AuthRequest {
    String username;
    String password;
}
