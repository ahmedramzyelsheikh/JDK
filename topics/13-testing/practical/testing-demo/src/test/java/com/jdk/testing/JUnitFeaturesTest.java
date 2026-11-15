package com.jdk.testing;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

/** A tour of JUnit Jupiter features using plain JUnit assertions (AssertJ is shown elsewhere). */
@Tag("fast")
class JUnitFeaturesTest {

    static final List<String> LIFECYCLE = new ArrayList<>();
    private final List<String> perTestState = new ArrayList<>();     // fresh for every test (new instance)

    @BeforeAll
    static void beforeAll() {
        LIFECYCLE.add("beforeAll");
    }

    @BeforeEach
    void beforeEach() {
        LIFECYCLE.add("beforeEach");
        perTestState.add("x");
    }

    @AfterEach
    void afterEach() {
        LIFECYCLE.add("afterEach");
    }

    @AfterAll
    static void afterAll() {
        assertEquals("beforeAll", LIFECYCLE.getFirst());
        System.out.println("JUnit lifecycle calls: " + LIFECYCLE.size());
    }

    @Test
    void eachTestGetsANewInstance() {
        assertEquals(1, perTestState.size());       // never 2: fields are not shared between tests
    }

    @Test
    void assertAllReportsEveryFailureAtOnce() {
        String name = "Spring Boot";
        assertAll("name",
                () -> assertTrue(name.startsWith("Spring")),
                () -> assertEquals(11, name.length()),
                () -> assertTrue(name.contains(" ")));
    }

    @Test
    void assertThrowsReturnsTheException() {
        NumberFormatException e = assertThrows(NumberFormatException.class, () -> Integer.parseInt("12a"));
        assertTrue(e.getMessage().contains("12a"));
    }

    @Test
    void assumptionsSkipInsteadOfFail() {
        assumeTrue(Runtime.version().feature() >= 21, "needs Java 21");
        assertTrue(Thread.ofVirtual().unstarted(() -> { }).isVirtual());
    }

    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    void finishesQuickly() {
        assertEquals(500_000_500_000L, Stream.iterate(1L, i -> i + 1).limit(1_000_000).mapToLong(Long::longValue).sum());
    }

    @RepeatedTest(3)
    void repeated(RepetitionInfo info) {
        assertTrue(info.getCurrentRepetition() <= info.getTotalRepetitions());
    }

    @Test
    void tempDirIsCreatedAndCleanedUp(@TempDir Path dir) throws IOException {
        Path file = Files.writeString(dir.resolve("note.txt"), "hello");
        assertEquals("hello", Files.readString(file));
    }

    @TestFactory
    Stream<DynamicTest> dynamicTestsFromData() {
        return Stream.of("level", "radar", "noon")
                .map(word -> dynamicTest("palindrome: " + word,
                        () -> assertEquals(word, new StringBuilder(word).reverse().toString())));
    }

    @Test
    @Disabled("example of a skipped test - always say why")
    void disabledTest() {
        throw new IllegalStateException("never runs");
    }
}
