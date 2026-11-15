import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dependency, association, aggregation, composition, and composition-over-inheritance.
 * Run: java Relationships.java
 */
public class Relationships {

    // ---------- dependency: uses a Car only as a parameter ----------
    record Car(String plate) { }

    static class Driver {
        String drive(Car car) { return "driving " + car.plate(); }
    }

    // ---------- association: a teacher knows students (both live independently) ----------
    record Student(String name) { }

    static class Teacher {
        private final List<Student> students = new ArrayList<>();
        void teach(Student s) { students.add(s); }
        int studentCount() { return students.size(); }
    }

    // ---------- aggregation: department HAS professors created elsewhere ----------
    record Professor(String name) { }

    static class Department {
        private final List<Professor> professors;
        Department(List<Professor> professors) { this.professors = List.copyOf(professors); }
        List<Professor> professors() { return professors; }
    }

    // ---------- composition: a house OWNS rooms it creates; they die with it ----------
    static class House {
        private final List<Room> rooms = new ArrayList<>();

        House(int roomCount) {
            for (int i = 1; i <= roomCount; i++) {
                rooms.add(new Room("room-" + i));                  // created inside, never shared
            }
        }

        private record Room(String name) { }

        int size() { return rooms.size(); }
    }

    // ---------- composition over inheritance ----------
    /** Inheritance version: double counts because HashSet.addAll calls add() internally. */
    static class CountingHashSet<E> extends HashSet<E> {
        int added;
        @Override public boolean add(E e) { added++; return super.add(e); }
        @Override public boolean addAll(Collection<? extends E> c) { added += c.size(); return super.addAll(c); }
    }

    /** Composition version: wraps any Set and delegates; no dependency on HashSet internals. */
    static class CountingSet<E> {
        private final Set<E> delegate;
        int added;
        CountingSet(Set<E> delegate) { this.delegate = delegate; }
        boolean add(E e) { added++; return delegate.add(e); }
        boolean addAll(Collection<? extends E> c) { added += c.size(); return delegate.addAll(c); }
        int size() { return delegate.size(); }
    }

    public static void main(String[] args) {
        System.out.println("dependency:  " + new Driver().drive(new Car("ABC-123")));

        Teacher teacher = new Teacher();
        Student ali = new Student("Ali");
        teacher.teach(ali);
        System.out.println("association: teacher has " + teacher.studentCount() + " student; " + ali + " still exists alone");

        List<Professor> profs = List.of(new Professor("Dr. Sara"), new Professor("Dr. Omar"));
        Department cs = new Department(profs);
        System.out.println("aggregation: department " + cs.professors() + " (professors created outside, can outlive it)");

        House house = new House(3);
        System.out.println("composition: house built its own " + house.size() + " rooms");

        CountingHashSet<String> inherited = new CountingHashSet<>();
        inherited.addAll(List.of("a", "b", "c"));
        CountingSet<String> composed = new CountingSet<>(new HashSet<>());
        composed.addAll(List.of("a", "b", "c"));
        System.out.println("\nfragile base class: inheritance counted " + inherited.added + " (wrong!), composition counted " + composed.added);
    }
}
