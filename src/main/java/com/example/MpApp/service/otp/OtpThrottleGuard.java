package com.example.MpApp.service.otp;

import com.example.MpApp.exception.RateLimitExceededException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory send-side throttle for the OTP flow.
 *
 * <p>The database tracks failed <em>verification</em> attempts, which says nothing
 * about how often someone can ask for new codes. Without this, a caller could
 * request a code every 30 seconds indefinitely, which is both a mail-bombing
 * vector and a way to keep rolling the dice against a 6-digit space.
 *
 * <p>State is per-process, which matches the existing {@code LoginRateLimitFilter}.
 * A multi-instance deployment would need this moved to Redis.
 */
@Component
public class OtpThrottleGuard {

    /** Sliding window in which a single account may request this many codes. */
    private static final int MAX_SENDS_PER_WINDOW = 5;
    private static final Duration WINDOW = Duration.ofHours(1);

    /**
     * Backstop against unbounded growth.
     *
     * <p>Every address that is ever asked about gets an entry, and the map is
     * keyed by attacker-supplied input. That made this a memory-exhaustion
     * vector: posting a few million distinct addresses was enough to hold a
     * {@code Window} per address for the lifetime of the process, while giving
     * the attacker no protection whatsoever, since randomising the address also
     * randomises the throttle key. {@link #evictIdleWindows()} normally reclaims
     * them; this ceiling bounds the damage in between.
     */
    private static final int MAX_TRACKED_KEYS = 50_000;
    private static final long EVICTION_INTERVAL_MS = 300_000L;

    private final Map<String, Window> sendWindows = new ConcurrentHashMap<>();

    public String keyFor(String email, String role) {
        return role + ":" + email.toLowerCase();
    }

    /**
     * @throws RateLimitExceededException if the account is inside its cooldown
     *         or has used up its hourly allowance
     */
    public void checkSendAllowed(String key) {
        Instant now = Instant.now();

        // One lookup, used for both the check and the record. The previous
        // version looked the window up twice, so a key evicted between the two
        // calls could be checked against one instance and recorded on another.
        Window window = sendWindows.computeIfAbsent(key, k -> new Window());

        synchronized (window) {
            window.prune(now);

            Instant lastSend = window.lastSentAt;
            if (lastSend != null) {
                long elapsed = Duration.between(lastSend, now).getSeconds();
                if (elapsed < OtpService.RESEND_COOLDOWN.getSeconds()) {
                    long retryAfter = OtpService.RESEND_COOLDOWN.getSeconds() - elapsed;
                    throw new RateLimitExceededException(
                            "Please wait before requesting another OTP.", retryAfter);
                }
            }

            if (window.timestamps.size() >= MAX_SENDS_PER_WINDOW) {
                long retryAfter = Math.max(1,
                        Duration.between(window.timestamps.peekFirst(), now.plus(WINDOW)).getSeconds());
                throw new RateLimitExceededException(
                        "Too many OTP requests for this account. Please try again later.", retryAfter);
            }

            // Recorded for every accepted request, including the ones for
            // addresses that do not exist. Otherwise response timing would
            // reveal which emails are registered.
            window.timestamps.add(now);
            window.lastSentAt = now;
        }
    }

    /** Called after a successful verification so a legitimate user is not throttled later. */
    public void clear(String key) {
        sendWindows.remove(key);
    }

    /**
     * Reclaims entries whose window has fully lapsed.
     *
     * <p>Runs on a schedule because nothing else touches the map once a request
     * finishes: a key is only ever re-examined if the same address asks again,
     * so a burst of unique addresses was never revisited at all.
     */
    @Scheduled(fixedRate = EVICTION_INTERVAL_MS)
    public void evictIdleWindows() {
        evictIdleSince(Instant.now().minus(WINDOW));

        // Still oversized, so the addresses are arriving faster than the window
        // lapses. Fall back to the shorter interval, past which an entry no
        // longer affects the cooldown check anyway.
        if (sendWindows.size() > MAX_TRACKED_KEYS) {
            evictIdleSince(Instant.now().minus(OtpService.RESEND_COOLDOWN));
        }
    }

    private void evictIdleSince(Instant cutoff) {
        sendWindows.forEach((key, window) -> {
            if (window.isIdleSince(cutoff)) {
                // Conditional remove: never drop an instance that replaced this
                // one while we were inspecting it.
                sendWindows.remove(key, window);
            }
        });
    }

    private static final class Window {

        private final ArrayDeque<Instant> timestamps = new ArrayDeque<>();
        private Instant lastSentAt;

        private synchronized void prune(Instant now) {
            Instant cutoff = now.minus(WINDOW);
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.pollFirst();
            }
        }

        private synchronized boolean isIdleSince(Instant cutoff) {
            return lastSentAt == null || lastSentAt.isBefore(cutoff);
        }
    }
}
