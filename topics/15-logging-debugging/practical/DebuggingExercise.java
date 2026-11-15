import java.util.ArrayList;
import java.util.List;

/**
 * DEBUGGING EXERCISE. This program runs without crashing but prints WRONG results.
 * Open it in IntelliJ / VS Code and use the debugger (don't just read the code!):
 *
 *   1. Put a LINE breakpoint in averageGrade() and step (F8) through the loop. Watch `sum` and `i`.
 *   2. Add a CONDITIONAL breakpoint in topStudents() with condition: s.grade() == 90
 *   3. Use EVALUATE EXPRESSION (Alt+F8) while paused: students.stream().mapToInt(Student::grade).sum()
 *   4. Add an EXCEPTION breakpoint for NumberFormatException, then change parseGrades input to include "9O".
 *
 * Expected output:  average = 80.0   top students = [Mona, Sara]   passed = 3
 * There are three bugs. Solutions are at the bottom of the file (no peeking!).
 * Run: java DebuggingExercise.java
 */
public class DebuggingExercise {

    record Student(String name, int grade) { }

    static double averageGrade(List<Student> students) {
        int sum = 0;
        for (int i = 1; i < students.size(); i++) {           // bug?
            sum += students.get(i).grade();
        }
        return sum / students.size();                          // bug?
    }

    static List<String> topStudents(List<Student> students, int threshold) {
        List<String> top = new ArrayList<>();
        for (Student s : students) {
            if (s.grade() > threshold) {                       // bug?
                top.add(s.name());
            }
        }
        return top;
    }

    static long passed(List<Student> students) {
        return students.stream().filter(s -> s.grade() >= 60).count();
    }

    static List<Student> parseGrades(String csv) {
        List<Student> list = new ArrayList<>();
        for (String part : csv.split(",")) {
            String[] kv = part.split("=");
            list.add(new Student(kv[0].strip(), Integer.parseInt(kv[1].strip())));
        }
        return list;
    }

    public static void main(String[] args) {
        List<Student> students = parseGrades("Ali=75, Mona=90, Omar=55, Sara=110");
        // Sara's 110 is a data-entry error capped at 100 below - stepping through shows when it is applied
        students = students.stream().map(s -> new Student(s.name(), Math.min(s.grade(), 100))).toList();

        System.out.println("average = " + averageGrade(students) + "   (expected 80.0: grades 75, 90, 55, 100)");
        System.out.println("top students = " + topStudents(students, 90) + "   (expected [Mona, Sara]: grade >= 90)");
        System.out.println("passed = " + passed(students) + "   (expected 3)");
    }
}

/*
 * SOLUTIONS
 * 1. averageGrade loop starts at i = 1 and skips the first student -> start at 0.
 * 2. sum / students.size() is INTEGER division -> (double) sum / students.size().
 *    With grades 75, 90, 55, 100 the correct average is 80.0.
 * 3. topStudents uses > instead of >= so a grade of exactly 90 is excluded.
 */
