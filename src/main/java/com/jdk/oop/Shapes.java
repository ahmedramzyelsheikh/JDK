package com.jdk.oop;

/**
 * Topic 02 - OOP: interfaces, records, sealed types, polymorphism, pattern matching.
 */
public final class Shapes {

    private Shapes() {
    }

    /** A sealed interface restricts which classes may implement it. */
    public sealed interface Shape permits Circle, Rectangle, Square {
        double area();
    }

    /** Records are immutable data carriers with generated constructor, accessors, equals/hashCode/toString. */
    public record Circle(double radius) implements Shape {
        public Circle {
            if (radius <= 0) {
                throw new IllegalArgumentException("radius must be positive");
            }
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }
    }

    public record Rectangle(double width, double height) implements Shape {
        @Override
        public double area() {
            return width * height;
        }
    }

    public record Square(double side) implements Shape {
        @Override
        public double area() {
            return side * side;
        }
    }

    /** Pattern matching for switch over a sealed hierarchy: the compiler checks every case is covered. */
    public static String describe(Shape shape) {
        return switch (shape) {
            case Circle c -> "Circle with radius " + c.radius();
            case Rectangle r -> "Rectangle " + r.width() + "x" + r.height();
            case Square s -> "Square with side " + s.side();
        };
    }

    public static void main(String[] args) {
        Shape[] shapes = {new Circle(1), new Rectangle(2, 3), new Square(4)};
        for (Shape shape : shapes) {
            System.out.printf("%s -> area %.2f%n", describe(shape), shape.area());
        }
    }
}
