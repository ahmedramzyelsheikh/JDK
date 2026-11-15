import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * java.nio.file (Path/Files), buffered byte & character streams, encodings, Properties,
 * walking a directory tree, and a FileChannel. Everything happens in a temp directory.
 * Run: java FilesAndStreams.java
 */
public class FilesAndStreams {

    public static void main(String[] args) throws IOException {
        Path root = Files.createTempDirectory("jdk-io-demo");
        try {
            run(root);
        } finally {
            try (Stream<Path> paths = Files.walk(root)) {            // delete children before parents
                paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
            System.out.println("\ncleaned up " + root);
        }
    }

    static void run(Path root) throws IOException {
        System.out.println("=== Path operations ===");
        Path notes = root.resolve("docs").resolve("notes.txt");
        System.out.println("  fileName=" + notes.getFileName() + " parent=" + root.relativize(notes.getParent())
                + " normalize(a/../b)=" + Path.of("a", "..", "b").normalize());

        System.out.println("\n=== Write and read text ===");
        Files.createDirectories(notes.getParent());
        Files.writeString(notes, "learn java\nlearn spring\n", StandardCharsets.UTF_8);
        Files.writeString(notes, "practice sql\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        System.out.println("  readAllLines: " + Files.readAllLines(notes));
        System.out.println("  size: " + Files.size(notes) + " bytes, exists: " + Files.exists(notes));

        try (Stream<String> lines = Files.lines(notes)) {                 // lazy: fine for huge files
            System.out.println("  lines containing 'learn': " + lines.filter(l -> l.contains("learn")).count());
        }

        System.out.println("\n=== Buffered writer / reader (decorators) ===");
        Path report = root.resolve("report.csv");
        try (BufferedWriter out = Files.newBufferedWriter(report)) {
            out.write("name,salary");
            out.newLine();
            for (String[] row : List.of(new String[] {"Ali", "12000"}, new String[] {"Sara", "15000"})) {
                out.write(String.join(",", row));
                out.newLine();
            }
        }
        try (BufferedReader in = Files.newBufferedReader(report)) {
            String header = in.readLine();
            int total = 0;
            String line;
            while ((line = in.readLine()) != null) {
                total += Integer.parseInt(line.split(",")[1]);
            }
            System.out.println("  header '" + header + "', total salary " + total);
        }

        System.out.println("\n=== Bytes: buffered copy vs Files.copy ===");
        Path binary = root.resolve("data.bin");
        byte[] data = new byte[2_000_000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        Files.write(binary, data);
        Path copy1 = root.resolve("copy1.bin");
        try (InputStream in = new BufferedInputStream(Files.newInputStream(binary));
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(copy1))) {
            in.transferTo(out);                                          // Java 9
        }
        Path copy2 = Files.copy(binary, root.resolve("copy2.bin"), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("  copies identical: " + (Files.mismatch(binary, copy1) == -1 && Files.mismatch(binary, copy2) == -1));

        System.out.println("\n=== FileChannel + ByteBuffer ===");
        try (FileChannel channel = FileChannel.open(binary, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            channel.position(1000);                                       // random access
            channel.read(buffer);
            buffer.flip();                                                // switch from writing to reading
            System.out.println("  8 bytes at offset 1000: first=" + buffer.get() + ", channel size=" + channel.size());
        }

        System.out.println("\n=== Encodings ===");
        String arabic = "العربية";
        byte[] utf8 = arabic.getBytes(StandardCharsets.UTF_8);
        String wrong = new String(utf8, StandardCharsets.ISO_8859_1);
        System.out.println("  '" + arabic + "' = " + arabic.length() + " chars, " + utf8.length + " UTF-8 bytes");
        System.out.println("  decoded with the wrong charset (mojibake) has " + wrong.length() + " chars");
        String emoji = "😀";
        System.out.println("  emoji length()=" + emoji.length() + ", codePointCount=" + emoji.codePointCount(0, emoji.length()));
        System.out.println("  default charset: " + Charset.defaultCharset());

        System.out.println("\n=== Properties ===");
        Properties props = new Properties();
        props.load(new StringReader("app.name=JDK course\napp.port=8080\n# comment\n"));
        System.out.println("  app.name=" + props.getProperty("app.name") + ", port+1=" + (Integer.parseInt(props.getProperty("app.port")) + 1)
                + ", missing default=" + props.getProperty("app.debug", "false"));

        System.out.println("\n=== Walk the tree ===");
        try (Stream<Path> walk = Files.walk(root)) {
            Map<String, Long> sizes = walk.filter(Files::isRegularFile)
                    .collect(Collectors.toMap(p -> root.relativize(p).toString(), FilesAndStreams::size,
                            (a, b) -> a, java.util.TreeMap::new));
            sizes.forEach((name, size) -> System.out.printf("  %-18s %,10d bytes%n", name, size));
        }
    }

    static long size(Path p) {
        try {
            return Files.size(p);
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }
}
