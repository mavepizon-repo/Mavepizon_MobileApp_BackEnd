package com.example.MpApp.service.otp;

import com.example.MpApp.entity.OtpEntity;
import com.example.MpApp.exception.OtpException;
import com.example.MpApp.repository.OtpRepository;
import com.example.MpApp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Single implementation of the forgot-password OTP state machine.
 *
 * <p>Every role used to carry its own copy of this logic, which meant a fix had
 * to be applied six times and routinely was not. All roles now delegate here.
 *
 * <p>The flow is: {@link #issueOtp} stores a code, {@link #verifyOtp} marks it
 * verified, and {@link #consumeVerifiedOtp} deletes it. Verification deliberately
 * does not delete the row -- that was the original bug, which made the subsequent
 * reset step fail with "OTP not requested".
 *
 * <p>Rows are scoped by {@code (email, role)}. One address can exist in several
 * role tables, and keying on email alone let those accounts overwrite each other.
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    public static final int MAX_VERIFY_ATTEMPTS = 5;
    public static final Duration OTP_TTL = Duration.ofMinutes(5);
    public static final Duration RESEND_COOLDOWN = Duration.ofSeconds(30);

    /**
     * Returned whether or not the address exists. Distinguishing the two would
     * let anyone enumerate which emails are registered.
     */
    public static final String ISSUE_ACK = "If an account exists for this email, an OTP has been sent.";

    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpThrottleGuard throttleGuard;

    /**
     * Issues a fresh code when the account exists.
     *
     * <p>{@code accountExists} deliberately drives whether an email is sent rather
     * than whether an error is raised, so the response is identical for real and
     * unknown addresses. The throttle slot is consumed either way, which keeps
     * the timing from giving the game away and stops an attacker from using this
     * endpoint to mail-bomb a known address.
     *
     * <p>The reply is unconditional: a delivery failure cannot change it. When
     * SMTP is broken, a registered address used to produce a 500 while an unknown
     * one produced the normal acknowledgement, which turned an outage into a
     * perfect account-enumeration oracle.
     */
    @Transactional
    public String issueOtp(String email, String role, boolean accountExists) {
        String key = throttleGuard.keyFor(email, role);
        throttleGuard.checkSendAllowed(key);

        if (!accountExists) {
            return ISSUE_ACK;
        }

        String otp = String.format("%06d", OTP_RANDOM.nextInt(1_000_000));

        // Replace any prior code for this role. A code for a different role is
        // left untouched.
        otpRepository.deleteByEmailAndRole(email, role);

        OtpEntity entity = new OtpEntity();
        entity.setEmail(email);
        entity.setRole(role);
        entity.setOtpCode(passwordEncoder.encode(otp));
        entity.setExpiryTime(LocalDateTime.now().plus(OTP_TTL));
        entity.setVerificationAttempts(0);
        entity.setVerified(false);
        entity.setLastSentAt(LocalDateTime.now());
        otpRepository.save(entity);

        // Never throws, so the row above is committed either way.
        emailService.sendOtpEmail(email, otp);

        return ISSUE_ACK;
    }

    /**
     * Marks the stored code as verified. Idempotent: re-verifying an
     * already-verified code succeeds so a client that retries is not punished.
     *
     * <p>Not transactional on purpose. A wrong code throws, and rolling that
     * transaction back used to silently discard the failed-attempt counter (and
     * the row deletion at the cap), so the 5-attempt lockout never actually ran.
     * Each write below commits in its own transaction instead.
     *
     * <p>This is also why callers must not wrap this in a transaction. A role
     * service that did exactly that reintroduced the lockout bug for that one
     * role while the other five behaved correctly.
     */
    public void verifyOtp(String email, String role, String otp) {
        OtpEntity entity = load(email, role);

        // Expiry is checked before the idempotent shortcut below. Otherwise an
        // already-verified but expired code reported success here and then failed
        // at the reset step, so the two steps disagreed about the same code.
        if (isExpired(entity)) {
            otpRepository.delete(entity);
            throw new OtpException("OTP_EXPIRED", "This OTP has expired. Please request a new one.");
        }

        if (entity.isVerified()) {
            return;
        }

        // Fast path for the ordinary sequential case, so the common exhaustion
        // case still reports ATTEMPTS_EXCEEDED rather than a generic INVALID.
        // It is not relied upon: the atomic update below is the real gate.
        if (entity.getVerificationAttempts() >= MAX_VERIFY_ATTEMPTS) {
            otpRepository.delete(entity);
            throw new OtpException("OTP_ATTEMPTS_EXCEEDED",
                    "Too many incorrect OTP attempts. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(otp, entity.getOtpCode())) {
            // The cap is enforced by the database, not by this method. Parallel
            // guesses cannot all observe a below-cap counter, because only one
            // update per remaining attempt is accepted and the rest are rejected.
            int incremented = otpRepository.incrementVerificationAttempts(email, role, MAX_VERIFY_ATTEMPTS);

            if (incremented == 0) {
                // Someone else consumed the last remaining attempt while this
                // request was in flight.
                otpRepository.deleteByEmailAndRole(email, role);
                throw new OtpException("OTP_ATTEMPTS_EXCEEDED",
                        "Too many incorrect OTP attempts. Please request a new OTP.");
            }

            // Delete only once the cap has actually been reached, again decided
            // by the database rather than by a re-read.
            otpRepository.deleteIfAttemptsExhausted(email, role, MAX_VERIFY_ATTEMPTS);

            throw new OtpException("OTP_INVALID", "The OTP you entered is incorrect.");
        }

        entity.setVerified(true);
        entity.setVerifiedAt(LocalDateTime.now());
        entity.setVerificationAttempts(0);
        otpRepository.save(entity);

        // The user proved control of the inbox, so the send-side limits are no
        // longer useful to them. Without this the guard kept the entry alive for
        // the full window, which is both a needless memory cost and the reason
        // the clear() method existed but was never wired up.
        throttleGuard.clear(throttleGuard.keyFor(email, role));
    }

    /**
     * Verifies and then destroys the code in one step, for clients that go
     * straight to reset without calling verify first. Either way the code is
     * single-use: this method always deletes the row.
     *
     * <p>Callers must invoke this from inside their own transaction, alongside
     * the password update. If it commits separately, a password that then fails
     * {@code PasswordPolicy.validate} leaves the code already destroyed and the
     * user has to restart the whole flow from requesting an OTP.
     */
    @Transactional
    public void consumeVerifiedOtp(String email, String role, String otp) {
        OtpEntity entity = load(email, role);

        if (isExpired(entity)) {
            otpRepository.delete(entity);
            throw new OtpException("OTP_EXPIRED", "This OTP has expired. Please request a new one.");
        }

        if (!entity.isVerified() && !passwordEncoder.matches(otp, entity.getOtpCode())) {
            throw new OtpException("OTP_NOT_VERIFIED", "Please verify your OTP before resetting your password.");
        }

        otpRepository.delete(entity);
    }

    private boolean isExpired(OtpEntity entity) {
        return entity.getExpiryTime().isBefore(LocalDateTime.now());
    }

    private OtpEntity load(String email, String role) {
        return otpRepository.findByEmailAndRole(email, role)
                .orElseThrow(() -> new OtpException("OTP_NOT_REQUESTED",
                        "No OTP has been requested for this account. Please request an OTP first."));
    }
}
