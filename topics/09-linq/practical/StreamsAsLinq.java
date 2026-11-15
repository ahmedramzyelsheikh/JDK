import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toMap;

/**
 * Every common LINQ operator, written with Java Streams. Each line shows the C# version in a comment.
 * Compare the output with practical/csharp (dotnet run): the results are identical.
 * Run: java StreamsAsLinq.java
 */
public class StreamsAsLinq {

    record Product(int id, String name, String category, BigDecimal price, int sold) { }
    record Customer(int id, String name, String city) { }
    record Order(int id, int customerId, int productId, int quantity) { }
    record CategoryRevenue(String category, BigDecimal revenue) { }      // instead of an anonymous type

    static final List<Product> PRODUCTS = List.of(
            new Product(1, "Laptop", "Tech", new BigDecimal("1200"), 5),
            new Product(2, "Mouse", "Tech", new BigDecimal("25"), 40),
            new Product(3, "Desk", "Home", new BigDecimal("300"), 7),
            new Product(4, "Lamp", "Home", new BigDecimal("40"), 15),
            new Product(5, "Novel", "Books", new BigDecimal("15"), 60),
            new Product(6, "Atlas", "Books", new BigDecimal("8"), 12),
            new Product(7, "Monitor", "Tech", new BigDecimal("280"), 9));

    static final List<Customer> CUSTOMERS = List.of(
            new Customer(1, "Ali", "Cairo"), new Customer(2, "Sara", "Alex"), new Customer(3, "Omar", "Cairo"));

    static final List<Order> ORDERS = List.of(
            new Order(1, 1, 1, 1), new Order(2, 1, 2, 2), new Order(3, 2, 5, 3), new Order(4, 1, 7, 1));

    public static void main(String[] args) {
        // Where + Select
        // products.Where(p => p.Price > 100).Select(p => p.Name)
        show("Where/Select", PRODUCTS.stream().filter(p -> p.price().compareTo(new BigDecimal("100")) > 0).map(Product::name).toList());

        // OrderBy + ThenByDescending
        // products.OrderBy(p => p.Category).ThenByDescending(p => p.Price).Select(p => p.Name)
        show("OrderBy/ThenByDescending", PRODUCTS.stream()
                .sorted(comparing(Product::category).thenComparing(Product::price, Comparator.reverseOrder()))
                .map(Product::name).toList());

        // GroupBy with count
        // products.GroupBy(p => p.Category).Select(g => new { g.Key, Count = g.Count() })
        show("GroupBy + Count", PRODUCTS.stream().collect(groupingBy(Product::category, java.util.TreeMap::new, counting())));

        // GroupBy + Sum + OrderByDescending + Take (the query from the README)
        // products.Where(p => p.Price > 10).GroupBy(p => p.Category)
        //         .Select(g => new { Category = g.Key, Revenue = g.Sum(p => p.Price * p.Sold) })
        //         .OrderByDescending(x => x.Revenue).Take(3)
        List<CategoryRevenue> top = PRODUCTS.stream()
                .filter(p -> p.price().compareTo(BigDecimal.TEN) > 0)
                .collect(groupingBy(Product::category,
                        reducing(BigDecimal.ZERO, p -> p.price().multiply(BigDecimal.valueOf(p.sold())), BigDecimal::add)))
                .entrySet().stream()
                .map(e -> new CategoryRevenue(e.getKey(), e.getValue()))
                .sorted(comparing(CategoryRevenue::revenue).reversed())
                .limit(3)
                .toList();
        show("Top categories by revenue", top);

        // SelectMany
        // customers.SelectMany(c => orders.Where(o => o.CustomerId == c.Id), (c, o) => c.Name + "#" + o.Id)
        show("SelectMany", CUSTOMERS.stream()
                .flatMap(c -> ORDERS.stream().filter(o -> o.customerId() == c.id()).map(o -> c.name() + "#" + o.id()))
                .toList());

        // Join (inner)
        // orders.Join(products, o => o.ProductId, p => p.Id, (o, p) => p.Name + " x" + o.Quantity)
        Map<Integer, Product> productById = PRODUCTS.stream().collect(toMap(Product::id, Function.identity()));
        show("Join", ORDERS.stream().map(o -> productById.get(o.productId()).name() + " x" + o.quantity()).toList());

        // GroupJoin (left outer join): customers with their order count, including customers with none
        // customers.GroupJoin(orders, c => c.Id, o => o.CustomerId, (c, os) => new { c.Name, Count = os.Count() })
        Map<Integer, List<Order>> ordersByCustomer = ORDERS.stream().collect(groupingBy(Order::customerId));
        show("GroupJoin", CUSTOMERS.stream()
                .map(c -> c.name() + "=" + ordersByCustomer.getOrDefault(c.id(), List.of()).size()).toList());

        // Distinct / DistinctBy
        // products.Select(p => p.Category).Distinct()        products.DistinctBy(p => p.Category).Select(p => p.Name)
        show("Distinct", PRODUCTS.stream().map(Product::category).distinct().toList());
        show("DistinctBy", PRODUCTS.stream()
                .collect(toMap(Product::category, Function.identity(), (first, second) -> first, LinkedHashMap::new))
                .values().stream().map(Product::name).toList());

        // Skip / Take / TakeWhile / SkipWhile
        show("Skip(2).Take(3)", PRODUCTS.stream().skip(2).limit(3).map(Product::name).toList());
        show("TakeWhile(price<1000)", PRODUCTS.stream().takeWhile(p -> p.price().intValue() < 1000).map(Product::name).toList());
        show("SkipWhile(cat==Tech)", PRODUCTS.stream().dropWhile(p -> p.category().equals("Tech")).map(Product::name).toList());

        // First / FirstOrDefault / Single / Last / ElementAt
        show("First(cat==Home)", PRODUCTS.stream().filter(p -> p.category().equals("Home")).findFirst().orElseThrow().name());
        show("FirstOrDefault(cat==Toys)", PRODUCTS.stream().filter(p -> p.category().equals("Toys")).findFirst().map(Product::name).orElse(null));
        show("Single(id==3)", single(PRODUCTS.stream().filter(p -> p.id() == 3)).name());
        show("Last()", PRODUCTS.getLast().name());
        show("ElementAt(1)", PRODUCTS.stream().skip(1).findFirst().orElseThrow().name());

        // Any / All / Contains / Count
        show("Any(price>1000)", PRODUCTS.stream().anyMatch(p -> p.price().intValue() > 1000));
        show("All(sold>0)", PRODUCTS.stream().allMatch(p -> p.sold() > 0));
        show("Contains(\"Tech\")", PRODUCTS.stream().map(Product::category).anyMatch("Tech"::equals));
        show("Count(cat==Books)", PRODUCTS.stream().filter(p -> p.category().equals("Books")).count());

        // Sum / Average / Min / Max / MaxBy / Aggregate
        show("Sum(Sold)", PRODUCTS.stream().mapToInt(Product::sold).sum());
        show("Average(Price)", "%.2f".formatted(PRODUCTS.stream().mapToDouble(p -> p.price().doubleValue()).average().orElse(0)));
        show("Min(Price)", PRODUCTS.stream().map(Product::price).min(Comparator.naturalOrder()).orElseThrow());
        show("MaxBy(Sold)", PRODUCTS.stream().max(comparing(Product::sold)).orElseThrow().name());
        show("Aggregate(names)", PRODUCTS.stream().map(Product::name).reduce("", (acc, n) -> acc.isEmpty() ? n : acc + "|" + n));

        // ToDictionary / ToLookup / ToHashSet
        show("ToDictionary(Name -> Price)", PRODUCTS.stream().limit(3).collect(toMap(Product::name, Product::price, (a, b) -> a, LinkedHashMap::new)));
        show("ToLookup(Category)", PRODUCTS.stream().collect(groupingBy(Product::category, java.util.TreeMap::new,
                Collectors.mapping(Product::name, Collectors.toList()))));

        // Zip / Concat / Union / Intersect / Except
        List<String> a = List.of("x", "y", "z");
        List<Integer> b = List.of(1, 2);
        show("Zip", zip(a, b, (s, n) -> s + n));
        show("Concat", Stream.concat(Stream.of("Tech"), Stream.of("Garden")).toList());
        Set<String> cats1 = Set.of("Tech", "Home");
        List<String> cats2 = List.of("Home", "Books");
        show("Union", Stream.concat(cats1.stream().sorted(), cats2.stream()).distinct().toList());
        show("Intersect", cats2.stream().filter(cats1::contains).toList());
        show("Except", cats2.stream().filter(c -> !cats1.contains(c)).toList());

        // OfType / Range / Repeat / Chunk
        List<Object> mixed = List.of(1, "two", 3.0, "four", 5);
        show("OfType<string>", mixed.stream().filter(String.class::isInstance).map(String.class::cast).toList());
        show("Range(1,5)", IntStream.rangeClosed(1, 5).boxed().toList());
        show("Repeat(\"ab\",3)", Stream.generate(() -> "ab").limit(3).toList());
        show("Chunk(3)", chunk(PRODUCTS.stream().map(Product::id).toList(), 3));

        // Deferred execution vs single-use
        List<Product> source = new ArrayList<>(PRODUCTS);
        Stream<Product> deferred = source.stream().filter(p -> p.category().equals("Garden"));
        source.add(new Product(8, "Shovel", "Garden", new BigDecimal("30"), 3));    // added BEFORE the terminal op
        show("Deferred: sees later additions", deferred.map(Product::name).toList());
    }

    static <T> T single(Stream<T> stream) {                       // LINQ Single(): exactly one element
        List<T> items = stream.limit(2).toList();
        if (items.size() != 1) {
            throw new IllegalStateException("expected exactly one element but found " + items.size());
        }
        return items.get(0);
    }

    static <A, B, R> List<R> zip(List<A> as, List<B> bs, BiFunction<A, B, R> f) {
        return IntStream.range(0, Math.min(as.size(), bs.size())).mapToObj(i -> f.apply(as.get(i), bs.get(i))).toList();
    }

    static <T> List<List<T>> chunk(List<T> list, int size) {      // Java 24: stream.gather(Gatherers.windowFixed(size))
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return chunks;
    }

    static void show(String label, Object value) {
        System.out.printf("%-32s %s%n", label, value);
    }
}
