package com.medtrack.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.medtrack.dto.AuthResponse;
import com.medtrack.dto.ChangePasswordDto;
import com.medtrack.dto.UserRequestDto;
import com.medtrack.dto.UserStatsDto;
import com.medtrack.exceptions.AuthException;
import com.medtrack.mapper.UserMapper;
import com.medtrack.model.HealthProduct;
import com.medtrack.model.MedicineUsageLog;
import com.medtrack.model.User;
import com.medtrack.repository.HealthProductRepo;
import com.medtrack.repository.MedicineUsageLogRepo;
import com.medtrack.repository.UserRepo;
import com.medtrack.security.JwtUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;
    private final HealthProductRepo healthProductRepo;
    private final MedicineUsageLogRepo medicineUsageLogRepo;

    public User signUp(UserRequestDto userDto) {

        userRepo.findOneByEmail(userDto.email()).ifPresent(existingUser -> {
            throw new AuthException("User with Email %s already exists.".formatted(existingUser.getEmail()));
        });

        User user = userMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.password()));

        return userRepo.save(user);
    }

    public AuthResponse signIn(UserRequestDto userDto) {
        User existingUser = userRepo.findOneByEmail(userDto.email()).orElseThrow(
                () -> new EntityNotFoundException("User with email %s not found".formatted(userDto.email())));

        if (!passwordEncoder.matches(userDto.password(), existingUser.getPassword())) {
            throw new AuthException("Invalid Password");
        }

        String token = jwtUtil.generateToken(existingUser.getEmail());

        return new AuthResponse(userMapper.toDto(existingUser), token);
    }

    public User getUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID %d not found".formatted(userId)));
    }

    public void delete(Long id) {
        userRepo.deleteById(id);
    }

    // Add these methods to your UserService.java

    public User updateUser(Long userId, UserRequestDto userDto) {
        User existingUser = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID %d not found".formatted(userId)));

        // Update fields
        if (userDto.fullName() != null && !userDto.fullName().trim().isEmpty()) {
            existingUser.setFullname(userDto.fullName().trim());
        }

        if (userDto.email() != null && !userDto.email().trim().isEmpty()) {
            // Check if email is already taken by another user
            userRepo.findOneByEmail(userDto.email()).ifPresent(user -> {
                if (!user.getId().equals(userId)) {
                    throw new AuthException("Email is already taken by another user");
                }
            });
            existingUser.setEmail(userDto.email().trim());
        }

        return userRepo.save(existingUser);
    }

    public void changePassword(ChangePasswordDto passwordDto) {
        // Implementation depends on how you get current user
        // You might need to get user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepo.findOneByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(passwordDto.currentPassword(), user.getPassword())) {
            throw new AuthException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.newPassword()));
        userRepo.save(user);
    }

    public void requestPasswordReset(String email) {
        User user = userRepo.findOneByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email %s not found".formatted(email)));

        // Generate reset token and send email
        // This is a simplified version - you should implement proper token generation
        // and email sending logic
        String resetToken = generateResetToken(user);
        // Send email with reset token
        // mailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    public String refreshToken(String refreshToken) {
        // Implement refresh token logic
        // This is a simplified version
        if (jwtUtil.isTokenValid(refreshToken)) {
            String username = jwtUtil.extractUsername(refreshToken);
            return jwtUtil.generateToken(username);
        }
        throw new AuthException("Invalid refresh token");
    }

    public void verifyEmail(String verificationCode) {
        // Implement email verification logic
        // This would typically involve checking the code against stored verification
        // codes
        // and updating user's email verification status
        throw new RuntimeException("Email verification not implemented yet");
    }

    private String generateResetToken(User user) {
        // Generate a secure random token for password reset
        // This is a simplified version - implement proper token generation
        return java.util.UUID.randomUUID().toString();
    }

    public UserStatsDto getUserStats(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Get health products stats
        List<HealthProduct> allProducts = healthProductRepo.findByUserId(userId);
        LocalDate currentDate = LocalDate.now();

        int totalMedicines = allProducts.size();
        int activeMedicines = (int) allProducts.stream()
                .filter(p -> p.getExpiryDate().isAfter(currentDate) && p.getAvailableQuantity() > 0)
                .count();
        int expiredMedicines = (int) allProducts.stream()
                .filter(p -> p.getExpiryDate().isBefore(currentDate))
                .count();
        int lowStockMedicines = (int) allProducts.stream()
                .filter(p -> p.getAvailableQuantity() <= p.getThresholdQuantity() &&
                        p.getExpiryDate().isAfter(currentDate))
                .count();

        // Get usage logs for adherence calculation
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<MedicineUsageLog> recentLogs = medicineUsageLogRepo.findAllByUserIdAndCreatedAtIsAfter(userId, weekAgo);

        long totalDosesTaken = recentLogs.stream()
                .filter(MedicineUsageLog::getIsTaken)
                .count();
        long totalDosesMissed = recentLogs.size() - totalDosesTaken;

        double adherenceRate = recentLogs.isEmpty() ? 0.0 : (double) totalDosesTaken / recentLogs.size() * 100;

        return new UserStatsDto(
                totalMedicines,
                activeMedicines,
                expiredMedicines,
                lowStockMedicines,
                Math.round(adherenceRate * 100.0) / 100.0,
                totalDosesTaken,
                totalDosesMissed);
    }

}
