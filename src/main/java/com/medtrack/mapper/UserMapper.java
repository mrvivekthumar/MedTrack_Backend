package com.medtrack.mapper;

import org.springframework.stereotype.Component;

import com.medtrack.dto.UserRequestDto;
import com.medtrack.dto.UserResponseDto;
import com.medtrack.model.User;

@Component
public class UserMapper {

    // Map from Entity to Response DTO
    public UserResponseDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDto(
                user.getId(),
                user.getFullname(),
                user.getEmail());
    }

    // Mapping from DTO to entity
    public User toEntity(UserRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .fullname(dto.fullName())
                .email(dto.email())
                .password(dto.password())
                .build();
    }
}