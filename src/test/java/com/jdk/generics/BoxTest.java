package com.jdk.generics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class BoxTest {

    @Test
    void mapsToAnotherType() {
        Box<Integer> length = new Box<>("generics").map(String::length);
        assertThat(length.value()).isEqualTo(8);
    }

    @Test
    void findsMaxOfComparables() {
        assertThat(Box.max(List.of("pear", "apple", "zucchini"))).isEqualTo("zucchini");
        assertThat(Box.max(List.of(3, 7, 5))).isEqualTo(7);
    }

    @Test
    void sumsAnyNumberType() {
        assertThat(Box.sum(List.of(1, 2, 3))).isEqualTo(6.0);
        assertThat(Box.sum(List.of(1.5, 2.5))).isEqualTo(4.0);
    }
}
