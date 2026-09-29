package com.example.MpApp;

import com.example.MpApp.entity.OtpEntity;
import com.example.MpApp.entity.student.Student;
import com.example.MpApp.repository.OtpRepository;
import com.example.MpApp.repository.student.StudentRepository;
import com.example.MpApp.service.EmailService;
import com.example.MpApp.service.otp.OtpThrottleGuard;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end checks over the real HTTP layer for the forgot-password flow and
 * the change-password ownership fix.
 *
 * <p>Schema is built from the entities (ddl-auto=create-drop) rather than from
 * the migrations, because no migration in this project creates the domain
 * tables. The migration scripts are covered separately by
 * {@link OtpMigrationScriptIT}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIT {

    private static EmbeddedPostgres postgres;

    private static final String VICTIM = "victim@example.com";
    private static final String ATTACKER = "attacker@example.com";
    private static final String OLD_PASSWORD = "Old@Pass123";

    @Autowired
    private MockMvc mockMvc;

    // Spring Boot 4 auto-configures Jackson 3 (tools.jackson), which is not the
    // legacy com.fasterxml type, so the test keeps its own mapper for payloads.
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private OtpThrottleGuard throttleGuard;

    /** Outbound mail is replaced so the plaintext code can be captured. */
    @MockitoBean
    private EmailService emailService;

    @BeforeAll
    static void startPostgres() throws IOException {
        postgres = EmbeddedPostgres.builder().start();
    }

    @AfterAll
    static void stopPostgres() throws IOException {
        if (postgres != null) {
            postgres.close();
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.mail.username", () -> "test@example.com");
        registry.add("spring.mail.password", () -> "test");
        registry.add("jwt.secret", () -> "test-secret-that-is-long-enough-for-hmac-sha256-signing");
        registry.add("cloudinary.cloud-name", () -> "test-cloud");
        registry.add("cloudinary.api-key", () -> "000000000000000");
        registry.add("cloudinary.api-secret", () -> "00000000000000000000000000000000");
        registry.add("razorpay.key.id", () -> "rzp_test_0000000000000000");
        registry.add("razorpay.key.secret", () -> "0000000000000000000000000000000000000000");
    }

    @BeforeEach
    void seed() {
        otpRepository.deleteAll();
        studentRepository.deleteAll();
        studentRepository.save(student(VICTIM));
        studentRepository.save(student(ATTACKER));

        // The send throttle lives in memory and would otherwise leak its cooldown
        // into the next test.
        throttleGuard.clear(throttleGuard.keyFor(VICTIM, "STUDENT"));
        throttleGuard.clear(throttleGuard.keyFor(ATTACKER, "STUDENT"));
        throttleGuard.clear(throttleGuard.keyFor("ghost@example.com", "STUDENT"));
    }

    private Student student(String email) {
        Student s = new Student();
        s.setName("Test " + email);
        s.setEmail(email);
        s.setPassword(encoder.encode(OLD_PASSWORD));
        s.setStudentId("S" + Math.abs(email.hashCode()));
        return s;
    }

    private String json(Object o) throws Exception {
        return mapper.writeValueAsString(o);
    }

    /** The HTTP body is flat: {token, role, studentId, email, message}. */
    private String loginToken(String email) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/student/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", OLD_PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();
        @SuppressWarnings("unchecked")
        Map<String, Object> body = mapper.readValue(
                r.getResponse().getContentAsString(), Map.class);
        return String.valueOf(body.get("token"));
    }

    /** Replays the plaintext code the service handed to the (mocked) mailer. */
    private String captureOtp(String email) {
        ArgumentCaptor<String> otp = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendOtpEmail(eq(email), otp.capture());
        return otp.getValue();
    }

    // ── forgot password ────────────────────────────────────────────

    @Test
    @DisplayName("unknown and known addresses get the identical response")
    void sendOtpDoesNotRevealWhetherAnAccountExists() throws Exception {
        String known = mockMvc.perform(post("/api/student/forgot-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String unknown = mockMvc.perform(post("/api/student/forgot-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "ghost@example.com"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(known).isEqualTo(unknown);
        assertThat(otpRepository.findByEmailAndRole(VICTIM, "STUDENT")).isPresent();
        assertThat(otpRepository.findByEmailAndRole("ghost@example.com", "STUDENT")).isEmpty();
    }

    @Test
    @DisplayName("wrong OTP is a 400 and the code survives for another try")
    void wrongOtpIsRejected() throws Exception {
        mockMvc.perform(post("/api/student/forgot-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM))))
                .andExpect(status().isOk());
        OtpEntity before = otpRepository.findByEmailAndRole(VICTIM, "STUDENT").orElseThrow();

        mockMvc.perform(post("/api/student/forgot-password/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM, "otp", "000000"))))
                .andExpect(status().isBadRequest());

        OtpEntity after = otpRepository.findByEmailAndRole(VICTIM, "STUDENT").orElseThrow();

        // The counter has to be committed even though the request failed. It
        // lives in its own transaction for exactly this reason: rolled back with
        // the exception, the 5-attempt lockout would never trip and a 6-digit
        // code would be brute-forceable.
        assertThat(after.getVerificationAttempts()).isEqualTo(before.getVerificationAttempts() + 1);
        // A wrong guess must not invalidate the code the real user is holding.
        assertThat(after.getOtpCode()).isEqualTo(before.getOtpCode());
    }

    @Test
    @DisplayName("full send -> verify -> reset works and the new password logs in")
    void fullResetFlowWorks() throws Exception {
        mockMvc.perform(post("/api/student/forgot-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM))))
                .andExpect(status().isOk());
        String otp = captureOtp(VICTIM);

        mockMvc.perform(post("/api/student/forgot-password/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM, "otp", otp))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM, "otp", otp, "newPassword", "New@Pass456"))))
                .andExpect(status().isOk());

        assertThat(otpRepository.findByEmailAndRole(VICTIM, "STUDENT")).isEmpty();
        assertThat(encoder.matches("New@Pass456",
                studentRepository.findByEmail(VICTIM).orElseThrow().getPassword())).isTrue();

        mockMvc.perform(post("/api/student/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM, "password", "New@Pass456"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("an OTP cannot be replayed for a second reset")
    void otpIsSingleUse() throws Exception {
        mockMvc.perform(post("/api/student/forgot-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM))))
                .andExpect(status().isOk());
        String otp = captureOtp(VICTIM);

        mockMvc.perform(post("/api/student/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM, "otp", otp, "newPassword", "First@Pass1"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM, "otp", otp, "newPassword", "Second@Pass2"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("a code issued for one role is not valid for another")
    void otpIsRoleScoped() throws Exception {
        mockMvc.perform(post("/api/student/forgot-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM))))
                .andExpect(status().isOk());
        String studentOtp = captureOtp(VICTIM);

        // The very same digits must not pass on the admin route.
        mockMvc.perform(post("/api/admin/forgot-password/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM, "otp", studentOtp))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("a password that breaks the policy is rejected without burning the OTP")
    void weakPasswordDoesNotConsumeTheOtp() throws Exception {
        mockMvc.perform(post("/api/student/forgot-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM))))
                .andExpect(status().isOk());
        String otp = captureOtp(VICTIM);

        mockMvc.perform(post("/api/student/forgot-password/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM, "otp", otp))))
                .andExpect(status().isOk());

        // Long enough to satisfy the old @Size(min = 8) and still missing an
        // uppercase letter, a digit and a special character. It used to pass DTO
        // validation and be rejected deeper in the service -- after the OTP had
        // already been deleted, so the user was stuck and had to start over.
        mockMvc.perform(post("/api/student/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM, "otp", otp, "newPassword", "mypassword"))))
                .andExpect(status().isBadRequest());

        assertThat(otpRepository.findByEmailAndRole(VICTIM, "STUDENT"))
                .as("the code must survive a rejected password so the user can retry")
                .isPresent();

        // The same code still completes the reset once a compliant password is used.
        mockMvc.perform(post("/api/student/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM, "otp", otp, "newPassword", "Good@Pass1"))))
                .andExpect(status().isOk());

        assertThat(encoder.matches("Good@Pass1",
                studentRepository.findByEmail(VICTIM).orElseThrow().getPassword())).isTrue();
    }

    @Test
    @DisplayName("the attempt limit drops the code once it has been exhausted")
    void wrongOtpExhaustionDropsTheCode() throws Exception {
        mockMvc.perform(post("/api/student/forgot-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM))))
                .andExpect(status().isOk());

        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/student/forgot-password/verify-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("email", VICTIM, "otp", "000000"))))
                    .andExpect(status().isBadRequest());
        }

        assertThat(otpRepository.findByEmailAndRole(VICTIM, "STUDENT"))
                .as("the code must be gone after five wrong guesses")
                .isEmpty();
    }

    @Test
    @DisplayName("an unmapped path answers 404 rather than a server error")
    void unmappedPathIsNotFound() throws Exception {
        // Route drift between client and server used to surface as a 500, which
        // is indistinguishable from a genuine outage and is how a whole
        // forgot-password API could point at the wrong shape unnoticed.
        mockMvc.perform(post("/api/student/forgot-password/no-such-step")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM))))
                .andExpect(status().isNotFound());
    }

    // ── change password / ownership ────────────────────────────────

    @Test
    @DisplayName("change-password only ever touches the caller's own account")
    void changePasswordCannotTargetAnotherAccount() throws Exception {
        String token = loginToken(ATTACKER);

        // The body has no email field, so naming a victim must have no effect.
        mockMvc.perform(patch("/api/student/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"" + OLD_PASSWORD
                                + "\",\"newPassword\":\"Attacker@New1\",\"email\":\"" + VICTIM + "\"}"))
                .andExpect(status().isOk());

        assertThat(encoder.matches("Attacker@New1",
                studentRepository.findByEmail(ATTACKER).orElseThrow().getPassword())).isTrue();
        assertThat(encoder.matches(OLD_PASSWORD,
                studentRepository.findByEmail(VICTIM).orElseThrow().getPassword())).isTrue();
    }

    @Test
    @DisplayName("a password change revokes tokens issued before it")
    void passwordChangeInvalidatesExistingToken() throws Exception {
        String token = loginToken(VICTIM);

        mockMvc.perform(patch("/api/student/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("oldPassword", OLD_PASSWORD, "newPassword", "Rotated@Pass1"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("a password reset revokes tokens issued before it")
    void passwordResetInvalidatesExistingToken() throws Exception {
        String token = loginToken(VICTIM);

        mockMvc.perform(post("/api/student/forgot-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM))))
                .andExpect(status().isOk());
        String otp = captureOtp(VICTIM);

        mockMvc.perform(post("/api/student/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", VICTIM, "otp", otp, "newPassword", "Reset@Pass99"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
