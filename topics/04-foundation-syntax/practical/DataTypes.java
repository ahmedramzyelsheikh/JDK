/**
 * Primitive types, wrappers, autoboxing, casting and their corner cases.
 * Run: java DataTypes.java
 */
public class DataTypes {

    public static void main(String[] args) {
        section("Ranges of the primitive types");
        System.out.println("byte   " + Byte.MIN_VALUE + " .. " + Byte.MAX_VALUE);
        System.out.println("short  " + Short.MIN_VALUE + " .. " + Short.MAX_VALUE);
        System.out.println("int    " + Integer.MIN_VALUE + " .. " + Integer.MAX_VALUE);
        System.out.println("long   " + Long.MIN_VALUE + " .. " + Long.MAX_VALUE);
        System.out.println("float  " + Float.MIN_VALUE + " .. " + Float.MAX_VALUE);
        System.out.println("double " + Double.MIN_VALUE + " .. " + Double.MAX_VALUE);
        System.out.println("char   " + (int) Character.MIN_VALUE + " .. " + (int) Character.MAX_VALUE);

        section("Literals");
        int million = 1_000_000;
        int hex = 0xFF;
        int binary = 0b1010;
        int octal = 010;                       // leading zero = octal!
        long big = 9_000_000_000L;             // needs L, too big for int
        float f = 3.14f;                       // needs f, decimals are double by default
        char letter = 'A';
        char unicode = 'ب';               // Arabic letter beh
        System.out.println(million + " " + hex + " " + binary + " " + octal + " " + big + " " + f + " " + letter + " " + unicode);

        section("Widening (automatic) and narrowing (cast)");
        int i = 100;
        long l = i;                            // widening
        double d = l;                          // widening
        System.out.println("int -> long -> double: " + d);
        double price = 9.99;
        System.out.println("(int) 9.99 = " + (int) price + "  (truncates)");
        System.out.println("Math.round(9.99) = " + Math.round(price));
        System.out.println("(byte) 130 = " + (byte) 130 + "  (wraps around)");
        System.out.println("'A' + 1 = " + ('A' + 1) + ", (char) ('A' + 1) = " + (char) ('A' + 1));
        byte b = 10;
        b += 5;                                // OK: compound assignment casts implicitly
        // b = b + 5;                          // compile error: b + 5 is int
        System.out.println("byte after += 5: " + b);

        section("Overflow and division");
        System.out.println("Integer.MAX_VALUE + 1 = " + (Integer.MAX_VALUE + 1));
        System.out.println("1000*1000*1000*10 (int math) = " + (1000 * 1000 * 1000 * 10));
        System.out.println("1000*1000*1000*10L            = " + (1000 * 1000 * 1000 * 10L));
        try {
            Math.addExact(Integer.MAX_VALUE, 1);
        } catch (ArithmeticException e) {
            System.out.println("Math.addExact -> " + e.getMessage());
        }
        System.out.println("5 / 2 = " + (5 / 2) + ", 5 / 2.0 = " + (5 / 2.0) + ", -7 % 3 = " + (-7 % 3)
                + ", Math.floorMod(-7, 3) = " + Math.floorMod(-7, 3));
        System.out.println("10.0 / 0 = " + (10.0 / 0) + ", 0.0 / 0 = " + (0.0 / 0));
        double nan = 0.0 / 0;
        System.out.println("NaN == NaN ? " + (nan == nan) + ", Double.isNaN ? " + Double.isNaN(nan));
        try {
            System.out.println(10 / zero());
        } catch (ArithmeticException e) {
            System.out.println("10 / 0 (int) -> ArithmeticException: " + e.getMessage());
        }

        section("Wrappers, autoboxing and the Integer cache");
        Integer a1 = 127, a2 = 127;
        Integer b1 = 128, b2 = 128;
        System.out.println("127 == 127 ? " + (a1 == a2) + "   (cached objects)");
        System.out.println("128 == 128 ? " + (b1 == b2) + "  (different objects!)");
        System.out.println("128 equals 128 ? " + b1.equals(b2) + "  (always use equals)");
        System.out.println("parseInt(\"42\") + 1 = " + (Integer.parseInt("42") + 1));
        System.out.println("toBinaryString(10) = " + Integer.toBinaryString(10));
        Integer nothing = null;
        try {
            int unboxed = nothing;             // calls nothing.intValue()
            System.out.println(unboxed);
        } catch (NullPointerException e) {
            System.out.println("Unboxing null -> NullPointerException");
        }
    }

    static int zero() {
        return 0;
    }

    static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
