package com.medtrack.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medtrack.dto.AuthResponse;
import com.medtrack.dto.ChangePasswordDto;
import com.medtrack.dto.ForgotPasswordDto;
import com.medtrack.dto.RefreshTokenDto;
import com.medtrack.dto.TokenResponse;
import com.medtrack.dto.UserRequestDto;
import com.medtrack.dto.UserResponseDto;
import com.medtrack.dto.UserStatsDto;
import com.medtrack.dto.VerificationDto;
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
    public ResponseEntity<UserResponseDto> signUp(@RequestBody UserRequestDto userDto) {
        System.out.println("Received user sign-up request: " + userDto);
        User savedUser = userService.signUp(userDto);
        return ResponseEntity.ok(userMapper.toDto(savedUser));
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@RequestBody UserRequestDto userDto) {
        AuthResponse authenticatedUser = userService.signIn(userDto);
        return ResponseEntity.ok(authenticatedUser);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable("userId") Long userId) {
        User user = userService.getUser(userId);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable("userId") Long userId) {
        userService.delete(userId);
        return ResponseEntity.ok("User with ID %d is deleted".formatted(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable("userId") Long userId,
            @RequestBody UserRequestDto userDto) {
        User updatedUser = userService.updateUser(userId, userDto);
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDto passwordDto) {
        userService.changePassword(passwordDto);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        userService.requestPasswordReset(forgotPasswordDto.email());
        return ResponseEntity.ok("Password reset email sent");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        String newToken = userService.refreshToken(refreshTokenDto.refreshToken());
        return ResponseEntity.ok(new TokenResponse(newToken));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody VerificationDto verificationDto) {
        userService.verifyEmail(verificationDto.code());
        return ResponseEntity.ok("Email verified successfully");
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStatsDto> getUserStats(@PathVariable("userId") Long userId) {
        UserStatsDto stats = userService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }
}
