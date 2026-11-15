import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * equals/hashCode contract and how to break it, toString, shallow vs deep copy.
 * Run: java ObjectMethods.java
 */
public class ObjectMethods {

    /** Correct: equals + hashCode + toString. */
    static final class Point {
        private int x, y;                                   // mutable on purpose (to show the key trap)

        Point(int x, int y) { this.x = x; this.y = y; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;                     // reflexive + fast path
            if (!(o instanceof Point p)) return false;      // also handles null
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "Point(" + x + "," + y + ")";
        }
    }

    /** Broken: equals without hashCode. */
    static final class BadPoint {
        final int x, y;
        BadPoint(int x, int y) { this.x = x; this.y = y; }

        @Override
        public boolean equals(Object o) {
            return o instanceof BadPoint p && x == p.x && y == p.y;
        }
        // hashCode NOT overridden -> identity hash
    }

    /** Broken: overloads equals instead of overriding it. */
    static final class Overloaded {
        final int id;
        Overloaded(int id) { this.id = id; }

        public boolean equals(Overloaded other) {           // parameter type is not Object!
            return other != null && id == other.id;
        }

        @Override public int hashCode() { return id; }
    }

    static class Team {
        String name;
        List<String> members;

        Team(String name, List<String> members) { this.name = name; this.members = members; }

        Team shallowCopy() { return new Team(name, members); }                       // shares the list
        Team deepCopy() { return new Team(name, new ArrayList<>(members)); }        // copies the list
    }

    public static void main(String[] args) {
        System.out.println("=== Default toString / equals ===");
        Object plain = new Object();
        System.out.println("  Object.toString(): " + plain + "  (ClassName@hash)");

        System.out.println("\n=== Correct equals + hashCode ===");
        Set<Point> points = new HashSet<>();
        points.add(new Point(1, 2));
        System.out.println("  contains(new Point(1,2)): " + points.contains(new Point(1, 2)));
        System.out.println("  equals(null): " + new Point(1, 2).equals(null));

        System.out.println("\n=== equals without hashCode ===");
        Set<BadPoint> bad = new HashSet<>();
        bad.add(new BadPoint(1, 2));
        bad.add(new BadPoint(1, 2));
        System.out.println("  equal objects: " + new BadPoint(1, 2).equals(new BadPoint(1, 2))
                + ", but set size = " + bad.size() + " and contains = " + bad.contains(new BadPoint(1, 2)));

        System.out.println("\n=== Mutating a key after insertion ===");
        Point key = new Point(5, 5);
        Set<Point> set = new HashSet<>(List.of(key));
        key.x = 99;                                         // hashCode changes, entry is in the old bucket
        System.out.println("  set contains its own element? " + set.contains(key) + "  <- lost!");
        System.out.println("  set still has size " + set.size() + ": " + set);

        System.out.println("\n=== Overloading equals by mistake ===");
        Overloaded a = new Overloaded(7);
        Object b = new Overloaded(7);
        System.out.println("  a.equals((Overloaded) b): " + a.equals((Overloaded) b));
        System.out.println("  a.equals(b as Object):    " + a.equals(b) + "  <- Object.equals called");
        System.out.println("  List.of(a).contains(b):   " + List.of(a).contains(b));

        System.out.println("\n=== Shallow vs deep copy ===");
        Team original = new Team("Backend", new ArrayList<>(List.of("Ali")));
        Team shallow = original.shallowCopy();
        Team deep = original.deepCopy();
        original.members.add("Sara");
        System.out.println("  original " + original.members + ", shallow " + shallow.members + ", deep " + deep.members);

        System.out.println("\n=== Objects helpers ===");
        String maybe = null;
        System.out.println("  Objects.equals(null, null): " + Objects.equals(maybe, null));
        System.out.println("  Objects.toString(null, \"n/a\"): " + Objects.toString(maybe, "n/a"));
        try {
            Objects.requireNonNull(maybe, "name is required");
        } catch (NullPointerException e) {
            System.out.println("  requireNonNull -> " + e.getMessage());
        }
    }
}
