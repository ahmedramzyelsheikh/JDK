import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * HashMap vs LinkedHashMap vs TreeMap, the modern Map API, an LRU cache, bucket index calculation
 * and colliding keys.
 * Run: java MapsDemo.java
 */
public class MapsDemo {

    /** LRU cache: access-ordered LinkedHashMap that evicts the eldest entry. */
    static class LruCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        LruCache(int capacity) {
            super(16, 0.75f, true);                      // true = access order
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    /** All instances have the same hashCode -> every key collides into one bucket. */
    record BadKey(int id) {
        @Override
        public int hashCode() {
            return 42;
        }
    }

    enum Day { MON, TUE, WED }

    public static void main(String[] args) {
        String[] words = "the quick brown fox jumps over the lazy dog the end".split(" ");

        System.out.println("=== Ordering ===");
        Map<String, Integer> hash = new HashMap<>();
        Map<String, Integer> linked = new LinkedHashMap<>();
        Map<String, Integer> tree = new TreeMap<>();
        for (String w : words) {
            hash.merge(w, 1, Integer::sum);              // word count
            linked.merge(w, 1, Integer::sum);
            tree.merge(w, 1, Integer::sum);
        }
        System.out.println("  HashMap:       " + hash);
        System.out.println("  LinkedHashMap: " + linked);
        System.out.println("  TreeMap:       " + tree);

        System.out.println("\n=== Modern Map API ===");
        System.out.println("  getOrDefault(\"cat\", 0) = " + hash.getOrDefault("cat", 0));
        Map<Integer, List<String>> byLength = new TreeMap<>();
        for (String w : linked.keySet()) {
            byLength.computeIfAbsent(w.length(), k -> new ArrayList<>()).add(w);   // multimap pattern
        }
        System.out.println("  group by length: " + byLength);
        hash.computeIfPresent("the", (k, v) -> v * 100);
        hash.putIfAbsent("cat", 1);
        System.out.println("  computeIfPresent(the) -> " + hash.get("the") + ", putIfAbsent(cat) -> " + hash.get("cat"));
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : tree.entrySet()) {   // iterate keys + values efficiently
            if (e.getValue() > 1) {
                sb.append(e.getKey()).append('=').append(e.getValue()).append(' ');
            }
        }
        System.out.println("  entries with count > 1: " + sb);

        System.out.println("\n=== TreeMap navigation: score -> grade ===");
        TreeMap<Integer, Character> grades = new TreeMap<>(Map.of(0, 'F', 50, 'D', 65, 'C', 75, 'B', 90, 'A'));
        for (int score : new int[] {95, 77, 50, 12}) {
            System.out.println("  " + score + " -> " + grades.floorEntry(score).getValue());
        }
        System.out.println("  firstEntry=" + grades.firstEntry() + " lastKey=" + grades.lastKey() + " headMap(65)=" + grades.headMap(65));

        System.out.println("\n=== LRU cache (capacity 3) ===");
        LruCache<String, String> cache = new LruCache<>(3);
        cache.put("a", "A");
        cache.put("b", "B");
        cache.put("c", "C");
        cache.get("a");                                  // a becomes most recently used
        cache.put("d", "D");                             // evicts b (least recently used)
        System.out.println("  " + cache.keySet() + "  (b was evicted)");

        System.out.println("\n=== Inside HashMap: bucket index ===");
        int n = 16;
        for (String key : new String[] {"Ali", "Sara", "Omar"}) {
            int h = key.hashCode();
            int spread = h ^ (h >>> 16);
            System.out.printf("  %-5s hashCode=%11d -> bucket %2d of %d%n", key, h, spread & (n - 1), n);
        }
        Map<BadKey, String> colliding = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            colliding.put(new BadKey(i), "v" + i);      // all in ONE bucket -> list, then red-black tree
        }
        System.out.println("  20 colliding keys still work (equals decides): get(BadKey(7)) = " + colliding.get(new BadKey(7)));
        Map<String, String> nulls = new HashMap<>();
        nulls.put(null, "null key allowed once");
        System.out.println("  HashMap null key: " + nulls.get(null));

        System.out.println("\n=== EnumMap ===");
        Map<Day, Integer> hours = new EnumMap<>(Day.class);
        hours.put(Day.WED, 6);
        hours.put(Day.MON, 8);
        System.out.println("  " + hours + " (always in enum declaration order)");

        System.out.println("\n=== Immutable maps ===");
        Map<String, Integer> fixed = Map.of("a", 1, "b", 2);
        try {
            fixed.put("c", 3);
        } catch (UnsupportedOperationException e) {
            System.out.println("  Map.of(...).put -> UnsupportedOperationException");
        }
        try {
            Map.of("a", 1, "a", 2);
        } catch (IllegalArgumentException e) {
            System.out.println("  Map.of with duplicate keys -> " + e.getMessage());
        }
    }
}
