package com.medtrack.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.medtrack.model.MedicineUsageLog;

@Repository
public interface MedicineUsageLogRepo extends JpaRepository<MedicineUsageLog, Long> {

        @Query("SELECT p FROM MedicineUsageLog p WHERE p.user.id = :userId AND p.createdAt > :date")
        List<MedicineUsageLog> findAllByUserIdAndCreatedAtIsAfter(@Param("userId") Long userId,
                        @Param("date") LocalDateTime date);

        List<MedicineUsageLog> findAllByUserIdAndCreatedAtBetween(Long id, LocalDateTime startOfDay,
                        LocalDateTime endOfDay);
}