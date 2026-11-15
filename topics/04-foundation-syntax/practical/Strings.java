import java.util.Arrays;

/**
 * String pool, equality, immutability, StringBuilder, text blocks and formatting.
 * Run: java Strings.java
 */
public class Strings {

    public static void main(String[] args) {
        System.out.println("=== Pool and equality ===");
        String s1 = "java";
        String s2 = "java";
        String s3 = new String("java");
        String s4 = "ja" + "va";                         // compile-time constant -> pooled
        String part = "ja";
        String s5 = part + "va";                         // computed at runtime -> new object
        System.out.println("s1 == s2: " + (s1 == s2));
        System.out.println("s1 == s3: " + (s1 == s3));
        System.out.println("s1.equals(s3): " + s1.equals(s3));
        System.out.println("s1 == \"ja\" + \"va\": " + (s1 == s4));
        System.out.println("s1 == part + \"va\": " + (s1 == s5));
        System.out.println("s1 == s5.intern(): " + (s1 == s5.intern()));

        System.out.println("\n=== Immutability ===");
        String name = "ali";
        name.toUpperCase();                              // result ignored: name is unchanged
        System.out.println("after name.toUpperCase(): " + name);
        name = name.toUpperCase();                       // assign the new String
        System.out.println("after name = name.toUpperCase(): " + name);

        System.out.println("\n=== Common methods ===");
        String text = "  Learn Java, Spring and SQL  ";
        System.out.println("length=" + text.length() + " strip='" + text.strip() + "'");
        System.out.println("indexOf(\"Java\")=" + text.indexOf("Java") + " contains(\"SQL\")=" + text.contains("SQL"));
        System.out.println("substring(8, 12)='" + text.substring(8, 12) + "'");
        System.out.println("split: " + Arrays.toString(text.strip().split("[ ,]+")));
        System.out.println("replace: " + text.strip().replace("Java", "Kotlin"));
        System.out.println("join: " + String.join(" | ", "a", "b", "c"));
        System.out.println("repeat: " + "-".repeat(10) + "  isBlank(\"  \"): " + "  ".isBlank());
        System.out.println("compareTo: \"apple\" vs \"banana\" = " + "apple".compareTo("banana"));
        System.out.println("equalsIgnoreCase: " + "JAVA".equalsIgnoreCase("java"));
        System.out.println("chars count of 'a': " + "banana".chars().filter(c -> c == 'a').count());

        System.out.println("\n=== StringBuilder ===");
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i).append(i < 5 ? "," : "");       // no new String per iteration
        }
        sb.insert(0, "[").append("]");
        System.out.println(sb + " reversed: " + new StringBuilder("racecar").reverse());

        System.out.println("\n=== Text block and formatting ===");
        String json = """
                {
                  "name": "%s",
                  "age": %d
                }""".formatted("Sara", 24);
        System.out.println(json);
        System.out.printf("%-8s|%6d|%10.2f|%,d%n", "Ali", 25, 1234.5678, 1234567);
        System.out.println(String.format("%05d %x %o %e %b %c", 42, 255, 8, 12345.678, true, 'Z'));
    }
}
