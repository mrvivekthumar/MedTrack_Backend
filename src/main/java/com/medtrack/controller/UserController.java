package com.medtrack.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medtrack.dto.UserDto;
import com.medtrack.mapper.UserMapper;
import com.medtrack.model.User;
import com.medtrack.service.UserService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@RequestBody User user) {
        User savedUser = userService.signUp(user);
        return ResponseEntity.ok(userMapper.toDto(savedUser));
    }

    @PostMapping("/signin")
    public ResponseEntity<UserDto> signIn(@RequestBody User user) {
        User authenticatedUser = userService.signIn(user);
        return ResponseEntity.ok(userMapper.toDto(authenticatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.ok("User with ID %d is deleted".formatted(id));
    }
}
