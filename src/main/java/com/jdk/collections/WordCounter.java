package com.jdk.collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Topic 04 - Collections: List, Set, Map, sorting with Comparator.
 */
public final class WordCounter {

    private WordCounter() {
    }

    /** Map: count how often each word appears (case-insensitive). */
    public static Map<String, Integer> count(String text) {
        Map<String, Integer> counts = new HashMap<>();
        for (String word : text.toLowerCase().split("\\W+")) {
            if (!word.isBlank()) {
                counts.merge(word, 1, Integer::sum);
            }
        }
        return counts;
    }

    /** List + Comparator: the n most frequent words, ties broken alphabetically. */
    public static List<String> topWords(String text, int n) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(count(text).entrySet());
        entries.sort(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry.comparingByKey()));
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(n, entries.size()); i++) {
            result.add(entries.get(i).getKey());
        }
        return result;
    }

    /** Set: unique words, keeping first-seen order. */
    public static Set<String> uniqueWords(String text) {
        Set<String> unique = new LinkedHashSet<>();
        for (String word : text.toLowerCase().split("\\W+")) {
            if (!word.isBlank()) {
                unique.add(word);
            }
        }
        return unique;
    }

    public static void main(String[] args) {
        String text = "the cat and the hat and the bat";
        System.out.println("Counts: " + count(text));
        System.out.println("Top 2: " + topWords(text, 2));
        System.out.println("Unique: " + uniqueWords(text));
    }
}
