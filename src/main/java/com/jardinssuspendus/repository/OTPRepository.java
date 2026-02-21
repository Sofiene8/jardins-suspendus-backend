package com.jardinssuspendus.repository;

import com.jardinssuspendus.entity.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTP, Long> {

    Optional<OTP> findByEmail(String email);

    Optional<OTP> findByEmailAndCode(String email, String code);

    @Query("SELECT o FROM OTP o WHERE o.email = :email AND o.used = false AND o.expiryDate > :now")
    Optional<OTP> findValidOTPByEmail(
        @Param("email") String email,
        @Param("now") LocalDateTime now
    );

    @Transactional
    @Modifying
    @Query("DELETE FROM OTP o WHERE o.email = :email")
    void deleteByEmail(@Param("email") String email);

    @Transactional
    @Modifying
    @Query("DELETE FROM OTP o WHERE o.expiryDate < :now")
    void deleteExpiredOTPs(@Param("now") LocalDateTime now);
}