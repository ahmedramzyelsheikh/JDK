package com.jdk.testing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("fast")
@DisplayName("PasswordPolicy (built with TDD)")
class PasswordPolicyTest {

    private final PasswordPolicy policy = new PasswordPolicy();      // new instance per test

    @ParameterizedTest(name = "rejects \"{0}\" as missing")
    @NullAndEmptySource
    @DisplayName("step 1: a password is required")
    void requiresAPassword(String password) {
        assertThat(policy.check("ali", password).violations()).containsExactly("password is required");
    }

    @Nested
    @DisplayName("single rules")
    class SingleRules {

        @Test
        @DisplayName("step 2: at least 8 characters")
        void minimumLength() {
            assertThat(policy.check("ali", "Ab1").violations()).contains("at least 8 characters");
        }

        @Test
        @DisplayName("step 3: needs a digit")
        void needsDigit() {
            assertThat(policy.check("ali", "Abcdefgh").violations()).containsExactly("at least one digit");
        }

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @CsvSource({
                "abcdefg1, at least one upper-case letter",
                "ABCDEFG1, at least one lower-case letter"
        })
        @DisplayName("step 4: needs upper and lower case")
        void needsBothCases(String password, String expectedViolation) {
            assertThat(policy.check("ali", password).violations()).containsExactly(expectedViolation);
        }

        @Test
        @DisplayName("step 6: must not contain the username (case-insensitive)")
        void mustNotContainUsername() {
            assertThat(policy.check("Sara", "mySARA2026x").violations()).containsExactly("must not contain the username");
        }
    }

    @Test
    @DisplayName("step 5: reports ALL violations, not just the first")
    void reportsAllViolations() {
        assertThat(policy.check("ali", "abc").violations())
                .containsExactlyInAnyOrder("at least 8 characters", "at least one digit", "at least one upper-case letter");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Secure123", "CorrectHorse9", "Java21Rocks"})
    @DisplayName("accepts strong passwords")
    void acceptsStrongPasswords(String password) {
        assertThat(policy.check("ali", password).valid()).isTrue();
    }
}
