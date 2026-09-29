package com.example.MpApp.service.otp;

import com.example.MpApp.entity.OtpEntity;
import com.example.MpApp.exception.OtpException;
import com.example.MpApp.exception.RateLimitExceededException;
import com.example.MpApp.repository.OtpRepository;
import com.example.MpApp.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the shared OTP state machine. Pure Mockito, so no database or
 * Spring context is required.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OtpServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String ROLE = "STUDENT";

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private EmailService emailService;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private OtpService otpService;
    private OtpThrottleGuard throttleGuard;

    @BeforeEach
    void setUp() {
        throttleGuard = new OtpThrottleGuard();
        otpService = new OtpService(otpRepository, encoder, emailService, throttleGuard);
    }

    /**
     * Stands in for the database accepting the increment. Mockito returns 0 for
     * an unstubbed int, which the service reads as "the cap was already
     * reached", so every test that exercises the wrong-code path must be
     * explicit about which outcome it is simulating.
     */
    private void acceptAttemptIncrement() {
        when(otpRepository.incrementVerificationAttempts(EMAIL, ROLE, OtpService.MAX_VERIFY_ATTEMPTS))
                .thenReturn(1);
    }

    private OtpEntity existingOtp(String plainOtp, boolean verified) {
        OtpEntity entity = new OtpEntity();
        entity.setEmail(EMAIL);
        entity.setRole(ROLE);
        entity.setOtpCode(encoder.encode(plainOtp));
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        entity.setVerificationAttempts(0);
        entity.setVerified(verified);
        entity.setLastSentAt(LocalDateTime.now());
        return entity;
    }

    @Test
    @DisplayName("known address: code is stored hashed and mailed, acknowledgement is generic")
    void issueOtpStoresHashedCodeAndMails() {
        String ack = otpService.issueOtp(EMAIL, ROLE, true);

        assertThat(ack).isEqualTo(OtpService.ISSUE_ACK);

        ArgumentCaptor<OtpEntity> captor = ArgumentCaptor.forClass(OtpEntity.class);
        verify(otpRepository).save(captor.capture());
        OtpEntity saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getRole()).isEqualTo(ROLE);
        // The code must never be stored in the clear.
        assertThat(saved.getOtpCode()).isNotEqualTo("123456");
        assertThat(saved.getOtpCode()).startsWith("$2");
        assertThat(saved.isVerified()).isFalse();
        verify(emailService).sendOtpEmail(eq(EMAIL), anyString());
    }

    @Test
    @DisplayName("unknown address: identical acknowledgement and no email sent")
    void issueOtpForUnknownAddressDoesNotLeakExistence() {
        String ack = otpService.issueOtp("nobody@example.com", ROLE, false);

        assertThat(ack).isEqualTo(OtpService.ISSUE_ACK);
        verify(otpRepository, never()).save(any());
        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("issuing a new code clears the previous one for the same role only")
    void issueOtpReplacesPreviousCodeForSameRole() {
        otpService.issueOtp(EMAIL, ROLE, true);

        verify(otpRepository).deleteByEmailAndRole(EMAIL, ROLE);
        verify(otpRepository, never()).deleteByEmailAndRole(EMAIL, "ADMIN");
    }

    @Test
    @DisplayName("second request inside the cooldown is throttled")
    void resendInsideCooldownIsRejected() {
        otpService.issueOtp(EMAIL, ROLE, true);

        assertThatThrownBy(() -> otpService.issueOtp(EMAIL, ROLE, true))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    @DisplayName("correct code marks the row verified and keeps it for the reset step")
    void verifyOtpMarksVerifiedWithoutDeleting() {
        OtpEntity entity = existingOtp("123456", false);
        when(otpRepository.findByEmailAndRole(EMAIL, ROLE)).thenReturn(Optional.of(entity));

        otpService.verifyOtp(EMAIL, ROLE, "123456");

        assertThat(entity.isVerified()).isTrue();
        assertThat(entity.getVerifiedAt()).isNotNull();
        verify(otpRepository, times(1)).save(entity);
        verify(otpRepository, never()).delete(any(OtpEntity.class));
    }

    @Test
    @DisplayName("a successful verification releases the send-side throttle")
    void successfulVerificationClearsSendThrottle() {
        OtpEntity entity = existingOtp("123456", false);
        when(otpRepository.findByEmailAndRole(EMAIL, ROLE)).thenReturn(Optional.of(entity));

        // Burn part of the hourly allowance, then verify.
        otpService.issueOtp(EMAIL, ROLE, true);
        otpService.issueOtp("someone-else@example.com", ROLE, true);
        otpService.verifyOtp(EMAIL, ROLE, "123456");

        // The guard was emptied for this address, so the next request is served
        // by a fresh window rather than the consumed one.
        assertThat(throttleGuard.keyFor(EMAIL, ROLE)).isNotNull();
        otpService.issueOtp(EMAIL, ROLE, true);
    }

    @Test
    @DisplayName("wrong code is rejected and the cap is checked in the database")
    void verifyOtpWithWrongCodeCountsAttempt() {
        OtpEntity entity = existingOtp("123456", false);
        when(otpRepository.findByEmailAndRole(EMAIL, ROLE)).thenReturn(Optional.of(entity));
        acceptAttemptIncrement();

        assertThatThrownBy(() -> otpService.verifyOtp(EMAIL, ROLE, "000000"))
                .isInstanceOf(OtpException.class)
                .hasMessageContaining("incorrect");

        // The increment carries the cap in its WHERE clause, so the service
        // never has to read the row back to find out whether it is allowed.
        verify(otpRepository).incrementVerificationAttempts(EMAIL, ROLE, OtpService.MAX_VERIFY_ATTEMPTS);
        verify(otpRepository).deleteIfAttemptsExhausted(EMAIL, ROLE, OtpService.MAX_VERIFY_ATTEMPTS);
        assertThat(entity.isVerified()).isFalse();
        verify(otpRepository, never()).delete(any(OtpEntity.class));
    }

    @Test
    @DisplayName("a counter already at the cap is dropped before any comparison")
    void verifyOtpDeletesCodeAfterMaxAttempts() {
        OtpEntity entity = existingOtp("123456", false);
        entity.setVerificationAttempts(OtpService.MAX_VERIFY_ATTEMPTS);
        when(otpRepository.findByEmailAndRole(EMAIL, ROLE)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> otpService.verifyOtp(EMAIL, ROLE, "000000"))
                .isInstanceOf(OtpException.class)
                .hasMessageContaining("Too many incorrect");

        verify(otpRepository).delete(entity);
        // Never spend a comparison against a code that is already dead.
        verify(otpRepository, never()).incrementVerificationAttempts(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("parallel guesses cannot push the counter past the cap")
    void concurrentGuessesBeyondTheCapAreRejected() {
        OtpEntity entity = existingOtp("123456", false);
        entity.setVerificationAttempts(OtpService.MAX_VERIFY_ATTEMPTS - 1);
        when(otpRepository.findByEmailAndRole(EMAIL, ROLE)).thenReturn(Optional.of(entity));

        // The service's own read still shows one attempt left, so it proceeds --
        // but the database rejects the update because another request took the
        // last slot first. A zero return means "cap reached", which is what makes
        // the limit exact under concurrency rather than only sequentially.
        when(otpRepository.incrementVerificationAttempts(EMAIL, ROLE, OtpService.MAX_VERIFY_ATTEMPTS))
                .thenReturn(0);

        assertThatThrownBy(() -> otpService.verifyOtp(EMAIL, ROLE, "000000"))
                .isInstanceOf(OtpException.class)
                .hasMessageContaining("Too many incorrect");

        verify(otpRepository).deleteByEmailAndRole(EMAIL, ROLE);
        verify(otpRepository, never()).deleteIfAttemptsExhausted(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("expired code is rejected and swept")
    void verifyOtpRejectsExpiredCode() {
        OtpEntity entity = existingOtp("123456", false);
        entity.setExpiryTime(LocalDateTime.now().minusMinutes(1));
        when(otpRepository.findByEmailAndRole(EMAIL, ROLE)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> otpService.verifyOtp(EMAIL, ROLE, "123456"))
                .isInstanceOf(OtpException.class)
                .hasMessageContaining("expired");

        verify(otpRepository).delete(entity);
    }

    @Test
    @DisplayName("an expired code is rejected even after it was verified")
    void verifiedButExpiredCodeIsRejected() {
        // Expiry used to be checked after the already-verified shortcut, so this
        // reported success here and then failed at the reset step. The two steps
        // disagreed about the same code.
        OtpEntity entity = existingOtp("123456", true);
        entity.setExpiryTime(LocalDateTime.now().minusMinutes(1));
        when(otpRepository.findByEmailAndRole(EMAIL, ROLE)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> otpService.verifyOtp(EMAIL, ROLE, "123456"))
                .isInstanceOf(OtpException.class)
                .hasMessageContaining("expired");

        verify(otpRepository).delete(entity);
        verify(otpRepository, never()).save(any(OtpEntity.class));
    }

    @Test
    @DisplayName("re-verifying a live code stays idempotent")
    void verifyingTwiceStillSucceeds() {
        OtpEntity entity = existingOtp("123456", true);
        when(otpRepository.findByEmailAndRole(EMAIL, ROLE)).thenReturn(Optional.of(entity));

        otpService.verifyOtp(EMAIL, ROLE, "123456");

        verify(otpRepository, never()).save(any(OtpEntity.class));
        verify(otpRepository, never()).delete(any(OtpEntity.class));
    }

    @Test
    @DisplayName("reset without any prior request is rejected")
    void consumeWithoutRequestIsRejected() {
        when(otpRepository.findByEmailAndRole(EMAIL, ROLE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> otpService.consumeVerifiedOtp(EMAIL, ROLE, "123456"))
                .isInstanceOf(OtpException.class)
                .hasMessageContaining("request an OTP");
    }

    @Test
    @DisplayName("verified code is consumed exactly once")
    void consumeVerifiedOtpDeletesRow() {
        OtpEntity entity = existingOtp("123456", true);
        when(otpRepository.findByEmailAndRole(EMAIL, ROLE)).thenReturn(Optional.of(entity));

        otpService.consumeVerifiedOtp(EMAIL, ROLE, "123456");

        verify(otpRepository).delete(entity);
    }

    @Test
    @DisplayName("a code cannot be replayed after it was consumed")
    void consumedCodeCannotBeReused() {
        when(otpRepository.findByEmailAndRole(EMAIL, ROLE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> otpService.consumeVerifiedOtp(EMAIL, ROLE, "123456"))
                .isInstanceOf(OtpException.class);
    }

    @Test
    @DisplayName("codes for different roles do not collide on the same address")
    void otpRowsAreScopedPerRole() {
        when(otpRepository.findByEmailAndRole(EMAIL, "STUDENT"))
                .thenReturn(Optional.empty());
        OtpEntity adminRow = existingOtp("654321", false);
        adminRow.setRole("ADMIN");
        when(otpRepository.findByEmailAndRole(EMAIL, "ADMIN"))
                .thenReturn(Optional.of(adminRow));

        // A code issued for ADMIN is invisible to the STUDENT lookup.
        assertThatThrownBy(() -> otpService.verifyOtp(EMAIL, "STUDENT", "654321"))
                .isInstanceOf(OtpException.class)
                .hasMessageContaining("request an OTP");

        // ...and verifies normally against its own role.
        otpService.verifyOtp(EMAIL, "ADMIN", "654321");
        assertThat(adminRow.isVerified()).isTrue();
        verify(otpRepository).save(adminRow);
    }
}
