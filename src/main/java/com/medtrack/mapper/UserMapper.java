package com.medtrack.mapper;

import org.springframework.stereotype.Component;

import com.medtrack.dto.UserDto;
import com.medtrack.model.User;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        // Works with your compact constructor
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail());
    }

    // Mapping from DTO to entity
    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .id(dto.id())
                .name(dto.name())
                .email(dto.email())
                .build();
    }
}