package com.example.MpApp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ConcurrentHashMap;

/** Temporary authentication cooldown. No permanent IP blocking. */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_FAILURES = 5;
    private static final Duration COOLDOWN = Duration.ofMinutes(2);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Map<String, AttemptState> ATTEMPTS = new ConcurrentHashMap<>();

    private static final Map<String, String> LOGIN_PATHS = Map.of(
            "/api/admin/login", "admin",
            "/api/student/login", "student",
            "/api/teamlead/login", "teamlead",
            "/api/officestaff/login", "officestaff",
            "/api/collegestaff/login", "collegestaff",
            "/api/freelancer/login", "freelancer"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String type = LOGIN_PATHS.get(request.getRequestURI());
        if (type == null || !"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String key = type + ":" + clientIp(request);
        AttemptState state = ATTEMPTS.get(key);
        Instant now = Instant.now();
        if (state != null && state.cooldownUntil != null) {
            if (now.isBefore(state.cooldownUntil)) {
                long seconds = Math.max(1, Duration.between(now, state.cooldownUntil).getSeconds());
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("Retry-After", String.valueOf(seconds));
                response.setContentType("application/json");
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("success", false);
                body.put("errorCode", "TOO_MANY_LOGIN_ATTEMPTS");
                body.put("timestamp", LocalDateTime.now().toString());
                body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
                body.put("error", "Too Many Login Attempts");
                body.put("message", "Login temporarily unavailable. Please try again after the cooldown.");
                body.put("path", request.getRequestURI());
                OBJECT_MAPPER.writeValue(response.getWriter(), body);
                return;
            }
            ATTEMPTS.remove(key);
        }

        chain.doFilter(request, response);

        int status = response.getStatus();
        if (status == 401 || status == 404) {
            AttemptState updated = ATTEMPTS.computeIfAbsent(key, k -> new AttemptState());
            updated.failures++;
            if (updated.failures >= MAX_FAILURES) {
                updated.cooldownUntil = Instant.now().plus(COOLDOWN);
            }
        } else if (status >= 200 && status < 400) {
            ATTEMPTS.remove(key);
        }
    }

    private String clientIp(HttpServletRequest request) {
        // Use the immediate peer address. Do not trust X-Forwarded-For from clients.
        return request.getRemoteAddr();
    }

    private static final class AttemptState {
        private int failures;
        private Instant cooldownUntil;
    }
}
