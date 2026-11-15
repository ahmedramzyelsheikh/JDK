/**
 * if/else, ternary, classic switch with fall-through, switch expressions and pattern matching.
 * Run: java Conditions.java
 */
public class Conditions {

    enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

    public static void main(String[] args) {
        for (int score : new int[] {95, 80, 60, 30}) {
            System.out.printf("score %d -> if/else %s, switch %s%n", score, gradeIf(score), gradeSwitch(score));
        }

        System.out.println("\nClassic switch fall-through (missing break):");
        classicFallThrough(2);

        System.out.println("\nEnum switch expression:");
        for (Day d : Day.values()) {
            System.out.print(d + "=" + dayType(d) + " ");
        }
        System.out.println();

        System.out.println("\nPattern matching switch:");
        for (Object o : new Object[] {42, 500, "hello", 3.5, null}) {
            System.out.println("  " + describe(o));
        }

        boolean flag = false;
        if (flag = true) {        // assignment, not comparison! Always true.
            System.out.println("\nCorner case: if (flag = true) always runs");
        }
    }

    static char gradeIf(int score) {
        if (score >= 90) {
            return 'A';
        } else if (score >= 75) {
            return 'B';
        } else if (score >= 50) {
            return 'C';
        } else {
            return 'F';
        }
    }

    static char gradeSwitch(int score) {
        return switch (score / 10) {
            case 10, 9 -> 'A';
            case 8 -> 'B';
            case 7, 6, 5 -> {
                char g = score >= 75 ? 'B' : 'C';
                yield g;                                  // yield returns a value from a block
            }
            default -> 'F';
        };
    }

    static void classicFallThrough(int n) {
        switch (n) {
            case 1:
                System.out.println("  one");
            case 2:
                System.out.println("  two");
            case 3:
                System.out.println("  three   <- ran because case 2 had no break");
                break;
            default:
                System.out.println("  other");
        }
    }

    static String dayType(Day day) {
        return switch (day) {                             // exhaustive over the enum: no default needed
            case SAT, SUN -> "weekend";
            case MON, TUE, WED, THU, FRI -> "work";
        };
    }

    static String describe(Object o) {
        return switch (o) {
            case null -> "null value";
            case Integer i when i > 100 -> "big integer " + i;
            case Integer i -> "integer " + i;
            case String s -> "string of length " + s.length();
            default -> "other type: " + o.getClass().getSimpleName();
        };
    }
}
