package com.jdk.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

/**
 * Topic 07 - File I/O with java.nio.file: Path, Files, try-with-resources.
 */
public final class NotesFile {

    private final Path file;

    public NotesFile(Path file) {
        this.file = file;
    }

    public void append(String note) throws IOException {
        Files.writeString(file, note + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public List<String> readAll() throws IOException {
        if (Files.notExists(file)) {
            return List.of();
        }
        return Files.readAllLines(file, StandardCharsets.UTF_8);
    }

    /** try-with-resources closes the stream (and the file handle) automatically. */
    public long countContaining(String word) throws IOException {
        if (Files.notExists(file)) {
            return 0;
        }
        try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
            return lines.filter(line -> line.contains(word)).count();
        }
    }

    public static void main(String[] args) throws IOException {
        Path temp = Files.createTempFile("notes", ".txt");
        NotesFile notes = new NotesFile(temp);
        notes.append("learn streams");
        notes.append("learn spring boot");
        System.out.println(notes.readAll());
        System.out.println("Lines with 'learn': " + notes.countContaining("learn"));
        Files.deleteIfExists(temp);
    }
}
