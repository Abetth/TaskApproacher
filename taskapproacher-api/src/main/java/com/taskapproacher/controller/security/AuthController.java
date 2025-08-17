package com.taskapproacher.controller.security;

import com.taskapproacher.entity.security.AuthRequest;
import com.taskapproacher.entity.security.AuthResponse;
import com.taskapproacher.entity.security.RegisterRequest;
import com.taskapproacher.service.security.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.status(201).body(authService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.authenticate(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null));
        }
    }
}
