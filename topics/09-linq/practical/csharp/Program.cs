// The same queries as ../StreamsAsLinq.java, written with LINQ. Run: dotnet run
using System.Globalization;

CultureInfo.CurrentCulture = CultureInfo.InvariantCulture;

var products = new List<Product>
{
    new(1, "Laptop", "Tech", 1200m, 5),
    new(2, "Mouse", "Tech", 25m, 40),
    new(3, "Desk", "Home", 300m, 7),
    new(4, "Lamp", "Home", 40m, 15),
    new(5, "Novel", "Books", 15m, 60),
    new(6, "Atlas", "Books", 8m, 12),
    new(7, "Monitor", "Tech", 280m, 9),
};
var customers = new List<Customer> { new(1, "Ali", "Cairo"), new(2, "Sara", "Alex"), new(3, "Omar", "Cairo") };
var orders = new List<Order> { new(1, 1, 1, 1), new(2, 1, 2, 2), new(3, 2, 5, 3), new(4, 1, 7, 1) };

// query syntax
var expensiveQuery = from p in products
                     where p.Price > 100
                     select p.Name;
Show("Where/Select (query syntax)", expensiveQuery);

// method syntax
Show("Where/Select", products.Where(p => p.Price > 100).Select(p => p.Name));
Show("OrderBy/ThenByDescending", products.OrderBy(p => p.Category, StringComparer.Ordinal).ThenByDescending(p => p.Price).Select(p => p.Name));
Show("GroupBy + Count", products.GroupBy(p => p.Category).OrderBy(g => g.Key, StringComparer.Ordinal).Select(g => $"{g.Key}={g.Count()}"));

var top = products.Where(p => p.Price > 10)
                  .GroupBy(p => p.Category)
                  .Select(g => new { Category = g.Key, Revenue = g.Sum(p => p.Price * p.Sold) })   // anonymous type
                  .OrderByDescending(x => x.Revenue)
                  .Take(3);
Show("Top categories by revenue", top.Select(x => $"{x.Category}={x.Revenue}"));

Show("SelectMany", customers.SelectMany(c => orders.Where(o => o.CustomerId == c.Id), (c, o) => $"{c.Name}#{o.Id}"));
Show("Join", orders.Join(products, o => o.ProductId, p => p.Id, (o, p) => $"{p.Name} x{o.Quantity}"));
Show("GroupJoin", customers.GroupJoin(orders, c => c.Id, o => o.CustomerId, (c, os) => $"{c.Name}={os.Count()}"));
Show("Distinct", products.Select(p => p.Category).Distinct());
Show("DistinctBy", products.DistinctBy(p => p.Category).Select(p => p.Name));
Show("Skip(2).Take(3)", products.Skip(2).Take(3).Select(p => p.Name));
Show("TakeWhile(price<1000)", products.TakeWhile(p => p.Price < 1000).Select(p => p.Name));
Show("SkipWhile(cat==Tech)", products.SkipWhile(p => p.Category == "Tech").Select(p => p.Name));
Show("First(cat==Home)", products.First(p => p.Category == "Home").Name);
Show("FirstOrDefault(cat==Toys)", products.FirstOrDefault(p => p.Category == "Toys")?.Name ?? "null");
Show("Single(id==3)", products.Single(p => p.Id == 3).Name);
Show("Last()", products.Last().Name);
Show("ElementAt(1)", products.ElementAt(1).Name);
Show("Any(price>1000)", products.Any(p => p.Price > 1000));
Show("All(sold>0)", products.All(p => p.Sold > 0));
Show("Contains(\"Tech\")", products.Select(p => p.Category).Contains("Tech"));
Show("Count(cat==Books)", products.Count(p => p.Category == "Books"));
Show("Sum(Sold)", products.Sum(p => p.Sold));
Show("Average(Price)", products.Average(p => p.Price).ToString("F2"));
Show("Min(Price)", products.Min(p => p.Price));
Show("MaxBy(Sold)", products.MaxBy(p => p.Sold)!.Name);
Show("Aggregate(names)", products.Select(p => p.Name).Aggregate((acc, n) => acc + "|" + n));
Show("ToDictionary(Name -> Price)", products.Take(3).ToDictionary(p => p.Name, p => p.Price).Select(kv => $"{kv.Key}={kv.Value}"));
Show("ToLookup(Category)", products.ToLookup(p => p.Category).OrderBy(g => g.Key, StringComparer.Ordinal).Select(g => $"{g.Key}=[{string.Join(", ", g.Select(p => p.Name))}]"));
Show("Zip", new[] { "x", "y", "z" }.Zip(new[] { 1, 2 }, (s, n) => s + n));
Show("Concat", new[] { "Tech" }.Concat(new[] { "Garden" }));
var cats1 = new[] { "Home", "Tech" };
var cats2 = new[] { "Home", "Books" };
Show("Union", cats1.Union(cats2));
Show("Intersect", cats2.Intersect(cats1));
Show("Except", cats2.Except(cats1));
Show("OfType<string>", new object[] { 1, "two", 3.0, "four", 5 }.OfType<string>());
Show("Range(1,5)", Enumerable.Range(1, 5));
Show("Repeat(\"ab\",3)", Enumerable.Repeat("ab", 3));
Show("Chunk(3)", products.Select(p => p.Id).Chunk(3).Select(c => "[" + string.Join(", ", c) + "]"));

// deferred execution: the query runs when enumerated, so it sees later additions - and can run again
var source = new List<Product>(products);
var deferred = source.Where(p => p.Category == "Garden").Select(p => p.Name);
source.Add(new Product(8, "Shovel", "Garden", 30m, 3));
Show("Deferred: sees later additions", deferred);
Show("...and can be enumerated again", deferred.Count());

static void Show(string label, object value)
{
    var text = value is System.Collections.IEnumerable e && value is not string
        ? "[" + string.Join(", ", e.Cast<object>()) + "]"
        : value.ToString();
    Console.WriteLine($"{label,-32} {text}");
}

record Product(int Id, string Name, string Category, decimal Price, int Sold);
record Customer(int Id, string Name, string City);
record Order(int Id, int CustomerId, int ProductId, int Quantity);
