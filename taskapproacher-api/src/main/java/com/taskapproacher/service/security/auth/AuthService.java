package com.taskapproacher.service.security.auth;

import com.taskapproacher.entity.security.AuthRequest;
import com.taskapproacher.entity.security.AuthResponse;
import com.taskapproacher.entity.security.RegisterRequest;
import com.taskapproacher.entity.user.User;
import com.taskapproacher.entity.user.UserResponse;
import com.taskapproacher.service.user.UserService;
import com.taskapproacher.constant.ExceptionMessage;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        User user = new User();

        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());

        UserResponse userResponse = userService.create(user);

        try {
            User createdUser = userService.findByUsername(userResponse.getUsername());
            String jwtToken = jwtService.generateToken(createdUser);
            return new AuthResponse(jwtToken);
        } catch (EntityNotFoundException e) {
            throw new UsernameNotFoundException("User " + ExceptionMessage.CREATION_FAILURE);
        }
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
