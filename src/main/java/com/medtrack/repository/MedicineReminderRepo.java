package com.medtrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medtrack.model.MedicineReminder;

@Repository
public interface MedicineReminderRepo extends JpaRepository<MedicineReminder, Long> {
}
