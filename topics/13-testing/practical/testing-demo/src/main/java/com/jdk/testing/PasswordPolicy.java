package com.jdk.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Written test-first (see the TDD table in the README): every rule started as a failing test
 * in PasswordPolicyTest.
 */
public class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    public record Result(List<String> violations) {
        public boolean valid() {
            return violations.isEmpty();
        }
    }

    public Result check(String username, String password) {
        List<String> violations = new ArrayList<>();
        if (password == null || password.isEmpty()) {
            return new Result(List.of("password is required"));
        }
        if (password.length() < MIN_LENGTH) {
            violations.add("at least " + MIN_LENGTH + " characters");
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            violations.add("at least one digit");
        }
        if (password.chars().noneMatch(Character::isUpperCase)) {
            violations.add("at least one upper-case letter");
        }
        if (password.chars().noneMatch(Character::isLowerCase)) {
            violations.add("at least one lower-case letter");
        }
        if (username != null && !username.isBlank()
                && password.toLowerCase(Locale.ROOT).contains(username.toLowerCase(Locale.ROOT))) {
            violations.add("must not contain the username");
        }
        return new Result(List.copyOf(violations));
    }
}
