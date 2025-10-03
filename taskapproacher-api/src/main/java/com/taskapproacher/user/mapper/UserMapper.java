package com.taskapproacher.user.mapper;

import com.taskapproacher.user.model.User;
import com.taskapproacher.user.model.UserDTO;

public class UserMapper {
    public UserDTO mapToUserResponse(User user) {
        UserDTO response = new UserDTO();
        response.setID(user.getID());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return response;
    }

    public User mapToUserEntity(UserDTO dto) {
        User user = new User();
        user.setID(dto.getID());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());

        return user;
    }
}
