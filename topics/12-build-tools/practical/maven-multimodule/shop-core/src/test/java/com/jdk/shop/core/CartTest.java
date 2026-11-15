package com.jdk.shop.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CartTest {

    @Test
    void totalsAllLines() {
        Cart cart = new Cart().add("book", "12.50", 2).add("pen", "1.99", 3);

        assertThat(cart.total()).isEqualByComparingTo("30.97");
        assertThat(cart.lines()).extracting(Cart.Line::product).containsExactly("Book", "Pen");
    }

    @Test
    void rejectsInvalidLines() {
        assertThatThrownBy(() -> new Cart().add(" ", "1", 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Cart().add("x", "1", 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
