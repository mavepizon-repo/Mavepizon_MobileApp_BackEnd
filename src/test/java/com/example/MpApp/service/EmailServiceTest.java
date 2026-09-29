package com.example.MpApp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "configuredFrom", "no-reply@example.com");
        ReflectionTestUtils.setField(emailService, "mailUsername", "smtp-user@example.com");
        ReflectionTestUtils.setField(emailService, "devLogOtp", false);
    }

    @Test
    @DisplayName("the OTP is delivered and the configured From address is used")
    void sendsMessageWithConfiguredFrom() {
        emailService.sendOtpEmail("user@example.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("user@example.com");
        assertThat(sent.getFrom()).isEqualTo("no-reply@example.com");
        assertThat(sent.getText()).contains("123456");
        assertThat(sent.getSubject()).isNotBlank();
    }

    @Test
    @DisplayName("From falls back to the SMTP user when none is configured")
    void fallsBackToSmtpUsername() {
        ReflectionTestUtils.setField(emailService, "configuredFrom", "");

        emailService.sendOtpEmail("user@example.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getFrom()).isEqualTo("smtp-user@example.com");
    }

    @Test
    @DisplayName("a transport failure is swallowed, never propagated to the caller")
    void transportFailureIsSwallowed() {
        // This is the enumeration guard. OtpService.issueOtp is transactional and
        // the response is meant to be identical for a registered address and an
        // unknown one. Letting MailSendException escape rolled back the OTP row
        // and returned 500 for exactly the registered addresses, turning a mail
        // outage into a perfect account-enumeration oracle.
        doThrow(new MailSendException("smtp unreachable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> emailService.sendOtpEmail("user@example.com", "123456"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("an unexpected runtime failure from the mailer is swallowed too")
    void runtimeFailureIsSwallowed() {
        doThrow(new IllegalStateException("mailer misconfigured"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> emailService.sendOtpEmail("user@example.com", "123456"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("the body states the validity window and the single-use nature")
    void bodyExplainsTheRules() {
        emailService.sendOtpEmail("user@example.com", "654321");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String body = captor.getValue().getText();
        assertThat(body).contains("654321");
        assertThat(body).contains("5 minutes");
        assertThat(body).contains("only once");
    }

    @Test
    @DisplayName("no message is attempted when there is no recipient")
    void nothingIsSentWithoutRecipient() {
        // Not a scenario the flow can reach today -- sendOtpEmail is only ever
        // called for an address that resolved from the users table -- but a
        // blank recipient would otherwise become a 500 from the mailer.
        assertThatCode(() -> emailService.sendOtpEmail("", "123456"))
                .doesNotThrowAnyException();

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
