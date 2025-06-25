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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User Management", description = "Operations related to user registration, authentication, and profile management")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided credentials. Email must be unique.", tags = {
            "Authentication" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class), examples = @ExampleObject(value = """
                    {
                        "userId": 1,
                        "fullName": "John Doe",
                        "email": "john.doe@example.com"
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "errors": {
                            "error": "User with Email john.doe@example.com already exists."
                        }
                    }
                    """)))
    })
    public ResponseEntity<UserResponseDto> signUp(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User registration details", required = true, content = @Content(schema = @Schema(implementation = UserRequestDto.class), examples = @ExampleObject(value = """
                    {
                        "fullName": "John Doe",
                        "email": "john.doe@example.com",
                        "password": "securePassword123"
                    }
                    """))) @RequestBody UserRequestDto userDto) {
        System.out.println("Received user sign-up request: " + userDto);
        User savedUser = userService.signUp(userDto);
        return ResponseEntity.ok(userMapper.toDto(savedUser));
    }

    @PostMapping("/signin")
    @Operation(summary = "Sign in user", description = "Authenticates user credentials and returns JWT token for API access", tags = {
            "Authentication" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(value = """
                    {
                        "user": {
                            "userId": 1,
                            "fullName": "John Doe",
                            "email": "john.doe@example.com"
                        },
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "errors": {
                            "error": "Invalid Password"
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                        "errors": {
                            "error": "User with email john.doe@example.com not found"
                        }
                    }
                    """)))
    })
    public ResponseEntity<AuthResponse> signIn(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User login credentials", required = true, content = @Content(schema = @Schema(implementation = UserRequestDto.class), examples = @ExampleObject(value = """
                    {
                        "email": "john.doe@example.com",
                        "password": "securePassword123"
                    }
                    """))) @RequestBody UserRequestDto userDto) {
        AuthResponse authenticatedUser = userService.signIn(userDto);
        return ResponseEntity.ok(authenticatedUser);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user profile", description = "Retrieves user profile information by user ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<UserResponseDto> getUser(
            @Parameter(description = "User ID", required = true, example = "1") @PathVariable("userId") Long userId) {
        User user = userService.getUser(userId);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user account", description = "Permanently deletes a user account and all associated data", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "User with ID 1 is deleted"))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "User ID to delete", required = true, example = "1") @PathVariable("userId") Long userId) {
        userService.delete(userId);
        return ResponseEntity.ok("User with ID %d is deleted".formatted(userId));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user profile", description = "Updates user profile information such as name and email", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "User ID to update", required = true, example = "1") @PathVariable("userId") Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated user information", required = true, content = @Content(schema = @Schema(implementation = UserRequestDto.class), examples = @ExampleObject(value = """
                    {
                        "fullName": "John Updated Doe",
                        "email": "john.updated@example.com"
                    }
                    """))) @RequestBody UserRequestDto userDto) {
        User updatedUser = userService.updateUser(userId, userDto);
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change user password", description = "Changes the password for the authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Password changed successfully"))),
            @ApiResponse(responseCode = "400", description = "Invalid current password", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<String> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Password change details", required = true, content = @Content(schema = @Schema(implementation = ChangePasswordDto.class), examples = @ExampleObject(value = """
                    {
                        "currentPassword": "oldPassword123",
                        "newPassword": "newSecurePassword456"
                    }
                    """))) @RequestBody ChangePasswordDto passwordDto) {
        userService.changePassword(passwordDto);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Sends a password reset email to the user's registered email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Password reset email sent"))),
            @ApiResponse(responseCode = "404", description = "Email not found", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<String> forgotPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email address for password reset", required = true, content = @Content(schema = @Schema(implementation = ForgotPasswordDto.class), examples = @ExampleObject(value = """
                    {
                        "email": "john.doe@example.com"
                    }
                    """))) @RequestBody ForgotPasswordDto forgotPasswordDto) {
        userService.requestPasswordReset(forgotPasswordDto.email());
        return ResponseEntity.ok("Password reset email sent");
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh JWT token", description = "Generates a new JWT token using a valid refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<TokenResponse> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Refresh token", required = true, content = @Content(schema = @Schema(implementation = RefreshTokenDto.class), examples = @ExampleObject(value = """
                    {
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                    }
                    """))) @RequestBody RefreshTokenDto refreshTokenDto) {
        String newToken = userService.refreshToken(refreshTokenDto.refreshToken());
        return ResponseEntity.ok(new TokenResponse(newToken));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address", description = "Verifies user's email address using verification code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Email verified successfully"))),
            @ApiResponse(responseCode = "400", description = "Invalid verification code", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<String> verifyEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email verification code", required = true, content = @Content(schema = @Schema(implementation = VerificationDto.class), examples = @ExampleObject(value = """
                    {
                        "code": "123456"
                    }
                    """))) @RequestBody VerificationDto verificationDto) {
        userService.verifyEmail(verificationDto.code());
        return ResponseEntity.ok("Email verified successfully");
    }

    @GetMapping("/{userId}/stats")
    @Operation(summary = "Get user statistics", description = "Retrieves comprehensive statistics about user's medicine usage and adherence", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User statistics retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserStatsDto.class), examples = @ExampleObject(value = """
                    {
                        "totalMedicines": 5,
                        "activeMedicines": 3,
                        "expiredMedicines": 1,
                        "lowStockMedicines": 2,
                        "adherenceRate": 85.5,
                        "totalDosesTaken": 42,
                        "totalDosesMissed": 8
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<UserStatsDto> getUserStats(
            @Parameter(description = "User ID", required = true, example = "1") @PathVariable("userId") Long userId) {
        UserStatsDto stats = userService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }
}