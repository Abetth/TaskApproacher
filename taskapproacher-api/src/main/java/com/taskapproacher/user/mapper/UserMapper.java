package com.taskapproacher.user.mapper;

import com.taskapproacher.user.model.User;
import com.taskapproacher.user.model.UserDTO;

public class UserMapper {
    public UserDTO mapToUserResponse(User user) {
        return new UserDTO(user.getID(), user.getUsername(), user.getEmail(), user.getRole());
    }

    public User mapToUserEntity(UserDTO dto) {
        return new User(dto.getID(), dto.getUsername(), dto.getEmail(), dto.getRole());
    }
}
