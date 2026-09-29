package com.example.MpApp.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    /**
     * Envelope sender. Left blank it falls back to the authenticated SMTP user,
     * which works for most providers but fails on the ones that insist on a
     * matching From header (Gmail being the common one).
     */
    @Value("${app.mail.from:}")
    private String configuredFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    /**
     * Writes the generated code to the log so the reset flow can be exercised
     * end to end without access to a real inbox.
     *
     * <p>Off by default. Anyone who can read the application log can reset any
     * account, so this must only ever be turned on for a local or staging
     * instance -- never on the deployed backend.
     */
    @Value("${app.otp.dev-log-otp:false}")
    private boolean devLogOtp;

    private static final int OTP_VALIDITY_MINUTES = 5;

    /**
     * Sends the reset code.
     *
     * <p>Deliberately never throws. A delivery failure used to propagate out of
     * {@code OtpService.issueOtp}, which is transactional, so the OTP row was
     * rolled back and the caller received a 500. That produced two failures at
     * once: the legitimate user got no code and no way forward, and because
     * unknown addresses took the success path, an attacker could distinguish a
     * registered account from an unknown one purely from the response.
     *
     * <p>Failures are logged at ERROR for operators and swallowed for callers.
     */
    public void sendOtpEmail(String to, String otp) {
        if (to == null || to.isBlank()) {
            // Not reachable from the forgot-password flow, which only ever calls
            // this with an address that resolved from the users table, but a
            // blank recipient is an exception from the mailer rather than a
            // useful log line.
            LOGGER.warn("Refusing to send a password reset OTP to a blank recipient");
            return;
        }

        String from = resolveFrom();

        if (devLogOtp) {
            LOGGER.warn("DEV MODE - password reset OTP for {} is {}", to, otp);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(to);
        message.setSubject("Your Password Reset OTP");
        message.setText(buildBody(otp));

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            // Never let a mail transport problem leak out of here. See the
            // method comment: it would both fail the user and re-open account
            // enumeration. The OTP row is left in place so a resend, or a
            // fixed SMTP configuration, recovers without a full restart.
            LOGGER.error("Failed to deliver password reset OTP to {}", to, ex);
        }
    }

    private String resolveFrom() {
        if (configuredFrom != null && !configuredFrom.isBlank()) {
            return configuredFrom;
        }
        if (mailUsername != null && !mailUsername.isBlank()) {
            return mailUsername;
        }
        return null;
    }

    private String buildBody(String otp) {
        return "Hello,\n\n"
                + "Your OTP for resetting your password is: " + otp + "\n\n"
                + "It is valid for " + OTP_VALIDITY_MINUTES + " minutes and can be used only once.\n"
                + "If you did not request this, you can safely ignore this email -- "
                + "your password will not change unless the code above is entered.\n";
    }
}
