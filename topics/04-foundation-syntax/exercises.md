# Exercises · Foundation syntax

Create your solutions in `src/main/java/com/jdk/basics/` (with tests in `src/test/java/com/jdk/basics/`)
or as single files next to this one. Try each without looking at the practical files first.

## Variables, types, operators
1. Read two integers and print their sum, difference, product, integer quotient, remainder and real quotient.
2. Convert a temperature from Celsius to Fahrenheit (`F = C * 9 / 5 + 32`). Why does `C * (9 / 5)` give the wrong answer?
3. Print the min and max values of every primitive type using the wrapper constants.
4. Swap two variables **without** a third variable (use arithmetic, then use XOR).
5. Calculate compound interest with `BigDecimal` rounded to 2 decimals.

## Strings
6. Count vowels, consonants, digits and spaces in a sentence.
7. Reverse each word of a sentence but keep the word order: `"hello java"` → `"olleh avaj"`.
8. Check whether two strings are anagrams.
9. Compress a string: `"aaabbc"` → `"a3b2c1"` using `StringBuilder`.

## Conditions
10. Print the grade (A/B/C/D/F) for a score with `if-else`, then with a switch expression.
11. Leap year: divisible by 4 and (not by 100 or by 400).
12. A simple calculator that reads `a op b` and uses a `switch` on `op`.

## Loops
13. Print the multiplication table 1…10 formatted in columns with `printf`.
14. FizzBuzz 1…100.
15. Print all primes below 100 (then optimise: stop at √n).
16. Print these patterns with nested loops:
    ```
    *        1          *
    **       12        ***
    ***      123      *****
    ```
17. Sum of digits of a number, reverse a number, check palindrome number.
18. Guess-the-number game with `Random` and `do-while`.

## Arrays
19. Find min, max, sum and average of an array in one loop.
20. Reverse an array in place.
21. Remove duplicates from a sorted array.
22. Rotate an array k positions to the right.
23. Multiply two 2D matrices.
24. Implement bubble sort and binary search yourself, then compare with `Arrays.sort` / `Arrays.binarySearch`.

## Methods
25. Write overloaded `area` methods for circle, rectangle and triangle.
26. Write `static int max(int... values)` with varargs.
27. Factorial and Fibonacci recursively and iteratively. What happens with `factorial(10_000)` recursively?
28. Prove pass-by-value: write a `swap(int a, int b)` method and show it doesn't swap the caller's variables.

## Dates
29. Calculate your exact age in years, months and days with `Period.between`.
30. Print the date 100 days from today formatted as `dd/MM/yyyy` and its day of the week.
