import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Java version. Memory: objects on the heap, freed by the garbage collector.
 * Run: java Students.java
 */
public class Students {

    // record = immutable data class (Java 16+): constructor, accessors, equals, hashCode, toString
    record Student(String name, double grade) {}

    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();          // growable list, generic over Student
        students.add(new Student("Ali", 88.5));
        students.add(new Student("Sara", 93.0));
        students.add(new Student("Omar", 76.0));
        students.add(new Student("Mona", 81.5));

        students.sort(Comparator.comparingDouble(Student::grade).reversed());

        double average = students.stream().mapToDouble(Student::grade).average().orElse(0);

        for (Student s : students) {
            System.out.printf("%-6s %6.2f%n", s.name(), s.grade());
        }
        System.out.printf("Average: %.2f%n", average);
        // no free(): the GC reclaims the list and the students when they are unreachable
    }
}
