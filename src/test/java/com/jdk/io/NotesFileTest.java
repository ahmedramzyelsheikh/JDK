package com.jdk.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NotesFileTest {

    @TempDir
    Path tempDir;

    @Test
    void readsNothingWhenFileIsMissing() throws IOException {
        NotesFile notes = new NotesFile(tempDir.resolve("missing.txt"));
        assertThat(notes.readAll()).isEmpty();
        assertThat(notes.countContaining("x")).isZero();
    }

    @Test
    void appendsAndReadsBack() throws IOException {
        NotesFile notes = new NotesFile(tempDir.resolve("notes.txt"));
        notes.append("learn streams");
        notes.append("learn spring boot");
        notes.append("practice sql");

        assertThat(notes.readAll()).containsExactly("learn streams", "learn spring boot", "practice sql");
        assertThat(notes.countContaining("learn")).isEqualTo(2);
    }
}
