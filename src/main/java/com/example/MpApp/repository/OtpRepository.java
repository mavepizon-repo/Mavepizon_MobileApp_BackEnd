package com.example.MpApp.repository;

import com.example.MpApp.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    Optional<OtpEntity> findByEmailAndRole(String email, String role);

    @Transactional
    void deleteByEmailAndRole(String email, String role);

    /**
     * Increments the failed-attempt counter in one SQL statement, and only while
     * the counter is below {@code max}.
     *
     * <p>The guard lives in the WHERE clause on purpose. The previous version
     * incremented unconditionally and relied on the caller re-reading the row to
     * decide whether the cap had been hit, which is a read-modify-write race:
     * parallel guesses all read a below-cap value before any of them committed,
     * so the 5-attempt lockout only ever bit on the sixth <em>sequential</em>
     * request. Letting the database reject the update makes the cap exact no
     * matter how many requests arrive at once.
     *
     * <p>Also not part of the caller's transaction. These must not live inside a
     * transaction that throws on a wrong code, otherwise the rollback would
     * silently undo the increment and the lockout would never run at all.
     *
     * @return 1 when the counter was incremented, 0 when it was already at the cap
     */
    @Modifying
    @Transactional
    @Query("update OtpEntity o set o.verificationAttempts = o.verificationAttempts + 1 "
            + "where o.email = :email and o.role = :role and o.verificationAttempts < :max")
    int incrementVerificationAttempts(@Param("email") String email,
                                      @Param("role") String role,
                                      @Param("max") int max);

    /**
     * Drops the code once it has burned through its allowance, so the attacker
     * has to wait out the send cooldown instead of continuing against a dead
     * row. Atomic for the same reason as {@link #incrementVerificationAttempts}.
     */
    @Modifying
    @Transactional
    @Query("delete from OtpEntity o "
            + "where o.email = :email and o.role = :role "
            + "and o.verified = false and o.verificationAttempts >= :max")
    int deleteIfAttemptsExhausted(@Param("email") String email,
                                  @Param("role") String role,
                                  @Param("max") int max);

    int deleteByExpiryTimeBefore(LocalDateTime expiryTime);

    /**
     * Sweeps rows that are past expiry, or that were verified but never consumed
     * by a reset. Called on a schedule; both variants would otherwise linger.
     */
    @Modifying
    @Query("delete from OtpEntity o where o.expiryTime < :now or (o.verified = true and o.verifiedAt < :staleBefore)")
    int deleteStale(@Param("now") LocalDateTime now, @Param("staleBefore") LocalDateTime staleBefore);
}
