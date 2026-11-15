package com.jdk.oop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.jdk.oop.Shapes.Circle;
import com.jdk.oop.Shapes.Rectangle;
import com.jdk.oop.Shapes.Square;
import org.junit.jupiter.api.Test;

class ShapesTest {

    @Test
    void computesAreas() {
        assertThat(new Circle(1).area()).isCloseTo(Math.PI, within(1e-9));
        assertThat(new Rectangle(2, 3).area()).isEqualTo(6);
        assertThat(new Square(4).area()).isEqualTo(16);
    }

    @Test
    void recordsHaveValueEquality() {
        assertThat(new Square(2)).isEqualTo(new Square(2));
    }

    @Test
    void validatesInCompactConstructor() {
        assertThatThrownBy(() -> new Circle(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void describesWithPatternMatching() {
        assertThat(Shapes.describe(new Rectangle(2, 3))).isEqualTo("Rectangle 2.0x3.0");
    }
}
