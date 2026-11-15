# Exercises · OOP

1. **BankAccount**: private balance, `deposit`, `withdraw` (reject negative amounts and overdrafts), a static
   counter of created accounts, and an immutable `Transaction` record history.
2. **Shapes**: abstract class `Shape` with abstract `area()`/`perimeter()`, subclasses `Circle`, `Rectangle`,
   `Triangle`. Store them in a `List<Shape>` and print the total area (polymorphism).
3. **Employees**: `Employee` → `Manager`, `Engineer`, `Intern` each with its own `salary()`. Sort by salary with a
   `Comparator`. Then redesign with an interface `Payable` and composition (a `SalaryPolicy` strategy).
4. **equals/hashCode**: write a `Point(x, y)` class *without* records. Put two equal points in a `HashSet`. Remove
   `hashCode` and observe. Then mutate a point after inserting it and try `contains`.
5. **Immutable class**: `Order(id, List<Item> items, LocalDate date)` that can't be modified from outside, with a
   `withItem(Item)` method returning a new order.
6. **Interface diamond**: two interfaces with the same default method; make a class implement both and resolve the
   conflict using `A.super.method()`.
7. **Sealed + records**: model `Expression` = `Number | Add | Multiply` and write `evaluate(Expression)` with a
   pattern-matching `switch` (no `default`).
8. **Enum with behaviour**: `Operation { PLUS, MINUS, TIMES, DIVIDE }` each implementing `apply(a, b)`.
9. **Library system**: `Library` *aggregates* `Book`s, a `Book` is *composed* of `Page`s, `Member` *associates*
   with borrowed books. Draw the UML class diagram first (Mermaid), then implement.
10. **Parking lot** (classic interview design): levels, spots of different sizes, vehicles (`Car`, `Bike`, `Truck`),
    park/unpark, fees. Focus on responsibilities and encapsulation.
