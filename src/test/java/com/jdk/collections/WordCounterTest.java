package com.jdk.collections;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WordCounterTest {

    private static final String TEXT = "The cat and the hat and THE bat";

    @Test
    void countsWordsIgnoringCase() {
        assertThat(WordCounter.count(TEXT))
                .containsEntry("the", 3)
                .containsEntry("and", 2)
                .containsEntry("cat", 1);
    }

    @Test
    void returnsTopWordsByFrequencyThenAlphabetically() {
        assertThat(WordCounter.topWords(TEXT, 3)).containsExactly("the", "and", "bat");
    }

    @Test
    void keepsUniqueWordsInFirstSeenOrder() {
        assertThat(WordCounter.uniqueWords(TEXT)).containsExactly("the", "cat", "and", "hat", "bat");
    }
}
