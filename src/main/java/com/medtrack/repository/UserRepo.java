package com.medtrack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medtrack.model.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    public Optional<User> findOneByEmail(String email);
}
