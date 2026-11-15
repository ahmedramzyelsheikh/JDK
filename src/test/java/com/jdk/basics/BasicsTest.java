package com.jdk.basics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BasicsTest {

    @Test
    void sumsEvenNumbers() {
        assertThat(Basics.sumOfEvens(10)).isEqualTo(30);
    }

    @Test
    void findsMax() {
        assertThat(Basics.max(new int[] {3, -1, 9, 4})).isEqualTo(9);
        assertThatThrownBy(() -> Basics.max(new int[0])).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void detectsPalindromes() {
        assertThat(Basics.isPalindrome("Never odd or even")).isTrue();
        assertThat(Basics.isPalindrome("Java")).isFalse();
    }

    @Test
    void classifiesDays() {
        assertThat(Basics.dayType(1)).isEqualTo("weekday");
        assertThat(Basics.dayType(7)).isEqualTo("weekend");
    }
}
