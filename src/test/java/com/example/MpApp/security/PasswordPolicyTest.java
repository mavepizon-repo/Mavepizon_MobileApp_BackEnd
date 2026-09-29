package com.example.MpApp.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Guards the agreement between the DTO-level {@link PasswordPolicy#PATTERN} and
 * {@link PasswordPolicy#validate(String)}.
 *
 * <p>These two used to be written out separately, so they drifted: the DTO only
 * required {@code @Size(min = 8)}. A password of {@code mypassword} passed
 * validation and was then rejected inside the service, by which point the OTP
 * had already been consumed, so the user had to restart the whole flow. Keeping
 * the regex in one place removes the possibility; these tests make sure it stays
 * in step with the human-readable checks.
 */
class PasswordPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {"Password1!", "Abcdefg1@", "aA1!aaaa", "Tmp@abc123def9A"})
    @DisplayName("a password satisfying every rule is accepted by both the regex and validate()")
    void strongPasswordsPassBoth(String password) {
        assertThat(password).matches(PasswordPolicy.PATTERN);
        assertThat(PasswordPolicy.isStrong(password)).isTrue();
        assertThatCode(() -> PasswordPolicy.validate(password)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "abcdefgh",      // no uppercase, no digit, no special
            "ABCDEFGH",      // no lowercase, no digit, no special
            "Abcdefgh",      // no digit, no special
            "Abcdef1",       // no special
            "Abcde1!",       // too short
            "12345678",      // no letter, no special
            "!@#$%^&*",      // no letter, no digit
    })
    @DisplayName("a weak password is rejected by both, so the boundary and the service agree")
    void weakPasswordsFailBoth(String password) {
        assertThat(password).doesNotMatch(PasswordPolicy.PATTERN);
        assertThat(PasswordPolicy.isStrong(password)).isFalse();
        assertThatThrownBy(() -> PasswordPolicy.validate(password))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("null is rejected without throwing NullPointerException")
    void nullIsRejected() {
        assertThat(PasswordPolicy.isStrong(null)).isFalse();
        assertThatThrownBy(() -> PasswordPolicy.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least");
    }

    @Test
    @DisplayName("the generated temporary password satisfies the policy")
    void generatedTemporaryPasswordIsStrong() {
        String generated = PasswordPolicy.generateTemporaryPassword();

        assertThat(PasswordPolicy.isStrong(generated))
                .as("temporary password %s must pass validate()", generated)
                .isTrue();
        assertThatCode(() -> PasswordPolicy.validate(generated)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("exactly the minimum length is accepted, one character less is not")
    void boundaryLengthIsEnforced() {
        assertThat("Aa1!aaaa").hasSize(PasswordPolicy.MIN_LENGTH);

        assertThat(PasswordPolicy.isStrong("Aa1!aaaa")).isTrue();
        assertThat(PasswordPolicy.isStrong("Aa1!aaa")).isFalse();

        assertThatCode(() -> PasswordPolicy.validate("Aa1!aaaa")).doesNotThrowAnyException();
        assertThatThrownBy(() -> PasswordPolicy.validate("Aa1!aaa"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 8");
    }
}
