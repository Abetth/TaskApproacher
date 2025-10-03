package com.taskapproacher.auth.service;

import com.taskapproacher.auth.model.AuthRequest;
import com.taskapproacher.auth.model.RegisterRequest;
import com.taskapproacher.auth.model.AuthResponse;
import com.taskapproacher.user.model.User;
import com.taskapproacher.user.model.UserDTO;
import com.taskapproacher.user.service.UserService;
import com.taskapproacher.user.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userMapper = new UserMapper();
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        User user = new User();

        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());

        UserDTO userDTO = userService.createUser(user);

        User createdUser = userMapper.mapToUserEntity(userDTO);

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
