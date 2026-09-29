package com.example.MpApp;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test that the whole application context starts.
 *
 * <p>This previously relied on {@code SPRING_DATASOURCE_URL} and the other
 * secrets being present in the environment, so a plain {@code mvn test} on a
 * clean checkout failed with "Driver claims to not accept jdbcUrl,
 * ${SPRING_DATASOURCE_URL}". That matters beyond developer convenience: the same
 * command is what a release build runs, so it could not produce a jar without
 * someone exporting production credentials first.
 *
 * <p>Supplies every externally-required value here instead, using the embedded
 * PostgreSQL already on the test classpath, so the build is hermetic.
 */
@SpringBootTest
class MpAppApplicationTests {

    private static EmbeddedPostgres postgres;

    @Autowired
    private ApplicationContext applicationContext;

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
        // The migrations only create the OTP tables, not the domain ones, so the
        // schema is built from the entities for a smoke test.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.mail.username", () -> "test@example.com");
        registry.add("spring.mail.password", () -> "test");
        registry.add("app.mail.from", () -> "no-reply@example.com");
        registry.add("jwt.secret", () -> "test-secret-that-is-long-enough-for-hmac-sha256-signing");
        registry.add("cloudinary.cloud-name", () -> "test-cloud");
        registry.add("cloudinary.api-key", () -> "000000000000000");
        registry.add("cloudinary.api-secret", () -> "00000000000000000000000000000000");
        registry.add("razorpay.key.id", () -> "rzp_test_0000000000000000");
        registry.add("razorpay.key.secret", () -> "0000000000000000000000000000000000000000");
    }

    @Test
    @DisplayName("the application context starts")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("the forgot-password beans are wired in")
    void forgotPasswordBeansArePresent() {
        // The reset flow spans several beans and has broken before because one of
        // them was missing, so their presence is worth asserting explicitly
        // rather than trusting that the context merely opened.
        assertThat(applicationContext.getBean(
                com.example.MpApp.service.otp.OtpService.class)).isNotNull();
        assertThat(applicationContext.getBean(
                com.example.MpApp.service.otp.OtpThrottleGuard.class)).isNotNull();
        assertThat(applicationContext.getBean(
                com.example.MpApp.service.EmailService.class)).isNotNull();
        assertThat(applicationContext.getBean(
                com.example.MpApp.repository.OtpRepository.class)).isNotNull();
    }
}
