package org.example.synjiserver.repository;

import org.example.synjiserver.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    // 查找最新的一条未使用的、未过期的验证码
    Optional<VerificationCode> findFirstByPhoneNumberAndCodeAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String phoneNumber, String code, LocalDateTime now);
}
