# Exercises · SQL

Use the shop schema in `practical/sql-playground/src/main/resources/sql/schema.sql` (data in `data.sql`).
Write the query first, then compare with the test that covers the same idea in `SqlPlaygroundTest`.

1. List products cheaper than 50, most expensive first, showing name and price.
2. Show every order with the customer's name and the order date.
3. List **all** customers with their number of orders, including customers with 0 orders.
4. Find customers who have **never** placed an order (two ways: `LEFT JOIN ... IS NULL` and `NOT EXISTS`).
5. Total revenue per category (quantity × unit_price), only categories above 100, highest first.
6. Products more expensive than the average product price (scalar subquery).
7. Customers who bought at least one product of the category "Books" (`EXISTS`).
8. The 2 most expensive products in each category (`ROW_NUMBER` in a CTE).
9. Running total of order revenue by order date (window `SUM ... OVER (ORDER BY ...)`).
10. For each order, its revenue and the difference with the previous order's revenue (`LAG`).
11. Count customers per city, showing cities with no city value as "unknown" (`COALESCE`).
12. Insert a product or update its price if the name already exists (`MERGE`).
13. In a transaction, move 5 units of stock from one product to another; roll back if any product would go negative.
14. Explain why `SELECT * FROM customer WHERE city <> 'Cairo'` does not return customers with a NULL city, and fix it.
15. Create an index that makes "orders of a customer in a date range" fast, and show it in `EXPLAIN`.
