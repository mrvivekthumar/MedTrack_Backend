package com.medtrack.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.medtrack.exceptions.AuthException;
import com.medtrack.mapper.UserMapper;
import com.medtrack.model.User;
import com.medtrack.repository.UserRepo;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User signUp(User user) {

        userRepo.findOneByEmail(user.getEmail()).ifPresent(existingUser -> {
            throw new AuthException("User with Email %s already exists.".formatted(existingUser.getEmail()));
        });

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepo.save(user);
    }

    public User signIn(User user) {

        var existingUser = userRepo.findOneByEmail(user.getEmail()).orElseThrow(
                () -> new EntityNotFoundException("User with email %s not found".formatted(user.getEmail())));

        if (!existingUser.getPassword().equals(user.getPassword())) {
            throw new AuthException("Invalid Password");
        }

        return existingUser;
    }

    public void delete(Long id) {

        userRepo.deleteById(id);
    }

}
