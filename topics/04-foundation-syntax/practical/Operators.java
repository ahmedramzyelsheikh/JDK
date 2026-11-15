import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Operators, short-circuit evaluation, increments, bitwise operators and exact decimals.
 * Run: java Operators.java
 */
public class Operators {

    public static void main(String[] args) {
        System.out.println("=== Increment ===");
        int i = 5;
        System.out.println("i++ returns " + i++ + ", then i = " + i);
        System.out.println("++i returns " + ++i);
        int j = 5;
        j = j++;
        System.out.println("j = j++ leaves j at " + j);

        System.out.println("\n=== Short-circuit ===");
        String s = null;
        boolean safe = s != null && s.length() > 0;
        System.out.println("s != null && s.length() > 0 -> " + safe + " (right side never evaluated)");
        try {
            boolean unsafe = s != null & s.length() > 0;
            System.out.println(unsafe);
        } catch (NullPointerException e) {
            System.out.println("Non-short-circuit & evaluated both sides -> NullPointerException");
        }

        System.out.println("\n=== Ternary ===");
        int score = 72;
        System.out.println(score + " -> " + (score >= 50 ? "pass" : "fail"));

        System.out.println("\n=== Bitwise ===");
        int a = 0b1100, b = 0b1010;
        System.out.printf("a=%s b=%s  a&b=%s a|b=%s a^b=%s ~a=%d%n",
                bin(a), bin(b), bin(a & b), bin(a | b), bin(a ^ b), ~a);
        System.out.println("1 << 4 = " + (1 << 4) + "   (multiply by 16)");
        System.out.println("-16 >> 2 = " + (-16 >> 2) + ", -16 >>> 28 = " + (-16 >>> 28) + " (zero fill)");
        System.out.println("is 7 odd? " + ((7 & 1) == 1));

        System.out.println("\n=== Precedence ===");
        System.out.println("2 + 3 * 4 = " + (2 + 3 * 4) + ", (2 + 3) * 4 = " + ((2 + 3) * 4));
        System.out.println("\"1\" + 2 + 3 = " + "1" + 2 + 3 + ", 1 + 2 + \"3\" = " + (1 + 2 + "3"));

        System.out.println("\n=== Floating point vs BigDecimal ===");
        System.out.println("0.1 + 0.2 = " + (0.1 + 0.2));
        System.out.println("new BigDecimal(0.1)   = " + new BigDecimal(0.1));
        System.out.println("new BigDecimal(\"0.1\") + 0.2 = " + new BigDecimal("0.1").add(new BigDecimal("0.2")));
        BigDecimal third = BigDecimal.TEN.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
        System.out.println("10 / 3 rounded HALF_UP to 2 = " + third);
        System.out.println("2.0 equals 2.00 ? " + new BigDecimal("2.0").equals(new BigDecimal("2.00"))
                + ", compareTo == 0 ? " + (new BigDecimal("2.0").compareTo(new BigDecimal("2.00")) == 0));

        System.out.println("\n=== Math ===");
        System.out.println("sqrt(16)=" + Math.sqrt(16) + " pow(2,10)=" + Math.pow(2, 10) + " round(2.5)=" + Math.round(2.5)
                + " round(-2.5)=" + Math.round(-2.5) + " ceil(1.1)=" + Math.ceil(1.1) + " floor(-1.1)=" + Math.floor(-1.1));
        System.out.println("Math.abs(Integer.MIN_VALUE) = " + Math.abs(Integer.MIN_VALUE) + "  (still negative!)");
    }

    static String bin(int x) {
        return String.format("%4s", Integer.toBinaryString(x & 0xF)).replace(' ', '0');
    }
}
