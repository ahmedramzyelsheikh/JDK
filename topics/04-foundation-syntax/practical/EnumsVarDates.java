import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * Enums (basic), var, final constants and the java.time API.
 * Run: java EnumsVarDates.java
 */
public class EnumsVarDates {

    enum Status { ACTIVE, SUSPENDED, CLOSED }

    static final int MAX_LOGIN_ATTEMPTS = 3;          // constant

    public static void main(String[] args) {
        System.out.println("=== enum ===");
        Status status = Status.valueOf("ACTIVE");
        System.out.println(status + " name=" + status.name() + " ordinal=" + status.ordinal());
        for (Status s : Status.values()) {
            System.out.print(s + " ");
        }
        System.out.println("\nACTIVE == Status.ACTIVE ? " + (status == Status.ACTIVE) + " (enums can be compared with ==)");
        try {
            Status.valueOf("DELETED");
        } catch (IllegalArgumentException e) {
            System.out.println("valueOf(\"DELETED\") -> IllegalArgumentException");
        }

        System.out.println("\n=== var ===");
        var names = new ArrayList<String>();          // ArrayList<String>
        names.add("Ali");
        var total = 10 + 2.5;                         // double
        System.out.println("names is " + names.getClass().getSimpleName() + ", total is a double: " + total);
        System.out.println("MAX_LOGIN_ATTEMPTS = " + MAX_LOGIN_ATTEMPTS);

        System.out.println("\n=== java.time ===");
        LocalDate birthday = LocalDate.of(2000, 5, 14);
        LocalDate today = LocalDate.of(2026, 11, 15);   // fixed so the output is reproducible; use LocalDate.now()
        Period age = Period.between(birthday, today);
        System.out.println("age: " + age.getYears() + "y " + age.getMonths() + "m " + age.getDays() + "d (" + age + ")");
        System.out.println("days lived: " + ChronoUnit.DAYS.between(birthday, today));

        LocalDate in100 = today.plusDays(100);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        System.out.println("100 days later: " + in100.format(fmt) + " (" + in100.getDayOfWeek() + ")");
        System.out.println("parse \"25/12/2026\": " + LocalDate.parse("25/12/2026", fmt));
        System.out.println("is 2028 leap? " + LocalDate.of(2028, 1, 1).isLeapYear());
        System.out.println("next Friday: " + today.with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.FRIDAY)));

        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 30);
        System.out.println("work duration: " + Duration.between(start, end) + " = " + Duration.between(start, end).toMinutes() + " min");

        LocalDateTime meeting = LocalDateTime.of(2026, 11, 15, 10, 0);
        ZonedDateTime cairo = meeting.atZone(ZoneId.of("Africa/Cairo"));
        ZonedDateTime tokyo = cairo.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));
        System.out.println("meeting Cairo " + cairo + " = Tokyo " + tokyo.toLocalDateTime());
        System.out.println("as Instant (UTC): " + cairo.toInstant() + ", epoch seconds " + cairo.toInstant().getEpochSecond());
        System.out.println("Instant.EPOCH = " + Instant.EPOCH);

        LocalDate d = LocalDate.of(2026, 1, 31);
        d.plusMonths(1);                               // immutable: result ignored!
        System.out.println("immutability: d is still " + d + ", d.plusMonths(1) = " + d.plusMonths(1) + " (end of month adjusted)");
    }
}
