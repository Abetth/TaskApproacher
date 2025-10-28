package com.taskapproacher.auth.service;

import com.taskapproacher.auth.model.AuthRequest;
import com.taskapproacher.auth.model.RegisterRequest;
import com.taskapproacher.auth.model.AuthResponse;
import com.taskapproacher.user.model.User;
import com.taskapproacher.user.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        User user = new User(request.getUsername(), request.getPassword(), request.getEmail());

        User createdUser = userService.createUser(user);

        String jwtToken = jwtService.generateToken(createdUser);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userService.findByUsername(request.getUsername());

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}
