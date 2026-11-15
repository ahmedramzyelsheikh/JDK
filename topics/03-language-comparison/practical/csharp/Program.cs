// C# version. Memory: garbage collected like Java. LINQ instead of Streams.
// Run: dotnet run   (inside this folder)

var students = new List<Student>
{
    new("Ali", 88.5),
    new("Sara", 93.0),
    new("Omar", 76.0),
    new("Mona", 81.5),
};

var sorted = students.OrderByDescending(s => s.Grade).ToList();   // LINQ
double average = students.Average(s => s.Grade);                  // LINQ

foreach (var s in sorted)
{
    Console.WriteLine($"{s.Name,-6} {s.Grade,6:F2}");               // string interpolation
}
Console.WriteLine($"Average: {average:F2}");

// record with properties Name and Grade (generated, immutable)
record Student(string Name, double Grade);
