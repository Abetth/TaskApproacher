package com.taskapproacher.auth.model;

import lombok.Value;

@Value
public class AuthResponse {
    String token;

    public AuthResponse(String token) {
        this.token = token;
    }
}
