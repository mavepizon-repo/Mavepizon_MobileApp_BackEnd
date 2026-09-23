package com.example.MpApp.scheduler;

import com.example.MpApp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Periodically removes expired OTP records so the OTP table does not grow
 * indefinitely. Verification/resend logic still performs its own expiry
 * checks; this job is the database cleanup mechanism.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupService {

    private final OtpRepository otpRepository;

    /**
     * Run every 5 minutes and remove every OTP whose expiry time has passed.
     */
    @Scheduled(fixedRate = 300_000)
    public void deleteExpiredOtps() {
        int deleted = otpRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("OTP cleanup removed {} expired OTP record(s)", deleted);
        }
    }
}
