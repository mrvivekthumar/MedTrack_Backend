package com.medtrack.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.medtrack.model.HealthProduct;

import jakarta.transaction.Transactional;

@Repository
public interface HealthProductRepo extends JpaRepository<HealthProduct, Long> {

        List<HealthProduct> findAllByUserIdAndAvailableQuantityGreaterThanAndExpiryDateAfter(Long userId,
                        Float quantity,
                        LocalDate currentDate);

        @Query("SELECT hp FROM HealthProduct hp WHERE hp.user.id = :userId " +
                        "AND hp.totalQuantity > 0 " +
                        "AND hp.expiryDate > :expiryDate " +
                        "AND hp.thresholdQuantity >= hp.availableQuantity")
        List<HealthProduct> findLowStockHealthProducts(
                        @Param("userId") Long userId,
                        @Param("expiryDate") LocalDate expiryDate);

        @Modifying
        @Transactional
        @Query("UPDATE HealthProduct hp SET hp.availableQuantity  = :availableQuantity  WHERE hp.id = :id")
        void updateAvailableQuantityById(@Param("id") Long id, @Param("availableQuantity") float availableQuantity);

        List<HealthProduct> findByUserId(Long userId);
}
