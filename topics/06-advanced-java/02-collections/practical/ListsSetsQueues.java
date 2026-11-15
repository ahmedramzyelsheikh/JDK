import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Lists, sets, queues, deques, immutable collections and a small ArrayList vs LinkedList timing.
 * Run: java ListsSetsQueues.java
 */
public class ListsSetsQueues {

    public static void main(String[] args) {
        System.out.println("=== List ===");
        List<Integer> numbers = new ArrayList<>(List.of(10, 20, 30, 1));
        numbers.remove(1);                                  // removes INDEX 1 (the 20)
        numbers.remove(Integer.valueOf(1));                 // removes VALUE 1
        System.out.println("  after remove(1) and remove(Integer.valueOf(1)): " + numbers);
        numbers.addFirst(5);                                // Java 21 sequenced collections
        System.out.println("  addFirst(5): " + numbers + ", getLast(): " + numbers.getLast() + ", reversed(): " + numbers.reversed());
        System.out.println("  Collections.max: " + Collections.max(numbers) + ", frequency of 10: " + Collections.frequency(numbers, 10));

        System.out.println("\n=== Set: order differences ===");
        List<String> input = List.of("banana", "apple", "cherry", "apple", "date");
        Set<String> hash = new HashSet<>(input);
        Set<String> linked = new LinkedHashSet<>(input);
        Set<String> tree = new TreeSet<>(input);
        System.out.println("  HashSet       (no order):        " + hash);
        System.out.println("  LinkedHashSet (insertion order): " + linked);
        System.out.println("  TreeSet       (sorted):          " + tree);
        TreeSet<Integer> scores = new TreeSet<>(List.of(55, 70, 85, 90, 99));
        System.out.println("  TreeSet navigation: floor(80)=" + scores.floor(80) + " ceiling(80)=" + scores.ceiling(80)
                + " headSet(85)=" + scores.headSet(85) + " descending=" + scores.descendingSet());

        TreeSet<String> caseInsensitive = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        caseInsensitive.addAll(List.of("Java", "JAVA", "java"));
        System.out.println("  TreeSet uses compare(), not equals(): " + caseInsensitive + " (size " + caseInsensitive.size() + ")");

        System.out.println("\n=== Queue (FIFO) and stack (LIFO) with ArrayDeque ===");
        Queue<String> queue = new ArrayDeque<>();
        queue.offer("first");
        queue.offer("second");
        queue.offer("third");
        System.out.println("  queue poll order: " + queue.poll() + ", " + queue.poll() + ", " + queue.poll() + ", then " + queue.poll());
        Deque<String> stack = new ArrayDeque<>();
        stack.push("a");
        stack.push("b");
        stack.push("c");
        System.out.println("  stack pop order: " + stack.pop() + ", " + stack.pop() + ", " + stack.pop());
        System.out.println("  balanced \"{[()]}\" ? " + balanced("{[()]}") + ", \"([)]\" ? " + balanced("([)]"));

        System.out.println("\n=== PriorityQueue (min-heap) ===");
        PriorityQueue<Integer> heap = new PriorityQueue<>(List.of(42, 7, 19, 3, 25));
        System.out.println("  toString is NOT sorted: " + heap);
        StringBuilder polled = new StringBuilder();
        while (!heap.isEmpty()) {
            polled.append(heap.poll()).append(' ');
        }
        System.out.println("  poll order is sorted:   " + polled);
        PriorityQueue<Integer> topK = new PriorityQueue<>();      // keep the 3 largest
        for (int n : new int[] {5, 1, 9, 3, 7, 8, 2}) {
            topK.offer(n);
            if (topK.size() > 3) {
                topK.poll();
            }
        }
        System.out.println("  3 largest of [5,1,9,3,7,8,2]: " + topK.stream().sorted(Comparator.reverseOrder()).toList());

        System.out.println("\n=== Immutable / unmodifiable ===");
        List<String> original = new ArrayList<>(List.of("x"));
        List<String> view = Collections.unmodifiableList(original);
        List<String> copy = List.copyOf(original);
        original.add("y");
        System.out.println("  unmodifiable VIEW sees change: " + view + ", copyOf does not: " + copy);
        String[] array = {"a", "b"};
        List<String> asList = Arrays.asList(array);
        asList.set(0, "CHANGED");
        System.out.println("  Arrays.asList writes through to the array: " + Arrays.toString(array));
        try {
            List.of("a", null);
        } catch (NullPointerException e) {
            System.out.println("  List.of(\"a\", null) -> NullPointerException");
        }

        System.out.println("\n=== ArrayList vs LinkedList (rough timing, 100k ops) ===");
        time("ArrayList  get(i)", new ArrayList<>(range(100_000)));
        time("LinkedList get(i)", new LinkedList<>(range(100_000)));
    }

    static boolean balanced(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(', '[', '{' -> stack.push(c);
                case ')', ']', '}' -> {
                    if (stack.isEmpty()) {
                        return false;
                    }
                    char open = stack.pop();
                    if ((c == ')' && open != '(') || (c == ']' && open != '[') || (c == '}' && open != '{')) {
                        return false;
                    }
                }
                default -> { }
            }
        }
        return stack.isEmpty();
    }

    static List<Integer> range(int n) {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        return list;
    }

    static void time(String label, List<Integer> list) {
        long start = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < list.size(); i += 10) {     // 10k random accesses
            sum += list.get(i);
        }
        System.out.printf("  %-18s %,10d us (sum %d)%n", label, (System.nanoTime() - start) / 1000, sum);
    }
}
