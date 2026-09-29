package com.example.MpApp.scheduler;

import com.example.MpApp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * How long a verified-but-unused code is retained. Long enough to finish the
     * reset, short enough that the table stays small.
     */
    private static final java.time.Duration VERIFIED_CODE_GRACE = java.time.Duration.ofHours(1);

    private final OtpRepository otpRepository;

    /**
     * Run every 5 minutes and remove every OTP whose expiry time has passed.
     *
     * The sweep deliberately keeps verified-but-unconsumed codes, because a
     * verified code is still needed by the reset step. Codes verified more than
     * VERIFIED_CODE_GRACE ago are dropped, since a password change invalidates
     * every outstanding JWT and the code has served its purpose.
     *
     * Marked transactional because the bulk delete is a modifying query.
     */
    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void deleteExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = otpRepository.deleteStale(now, now.minus(VERIFIED_CODE_GRACE));
        if (deleted > 0) {
            log.info("OTP cleanup removed {} stale OTP record(s)", deleted);
        }
    }
}
