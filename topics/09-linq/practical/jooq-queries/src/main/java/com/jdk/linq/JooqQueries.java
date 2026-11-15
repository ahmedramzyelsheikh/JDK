package com.jdk.linq;

import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.noCondition;
import static org.jooq.impl.DSL.sum;
import static org.jooq.impl.DSL.table;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

/**
 * jOOQ plays the role of LINQ to Entities in Java: queries are built as OBJECTS (not strings, not lambdas),
 * can be composed step by step like an IQueryable, rendered to SQL to inspect them (like EF's ToQueryString),
 * and only run when you call fetch().
 *
 * Normally jOOQ generates PRODUCT/ORDERS classes from your schema; here tables and fields are declared by
 * hand so the example needs no code generation.
 * Run: ./mvnw -q -f topics/pom.xml -pl 09-linq/practical/jooq-queries compile exec:java
 */
public class JooqQueries {

    // ---- schema description (what jOOQ's code generator would create for you) ----
    static final Table<Record> PRODUCT = table(name("product"));
    static final Field<Integer> ID = field(name("id"), SQLDataType.INTEGER);
    static final Field<String> NAME = field(name("name"), SQLDataType.VARCHAR(50));
    static final Field<String> CATEGORY = field(name("category"), SQLDataType.VARCHAR(30));
    static final Field<BigDecimal> PRICE = field(name("price"), SQLDataType.DECIMAL(10, 2));
    static final Field<Integer> SOLD = field(name("sold"), SQLDataType.INTEGER);

    static final Table<Record> ORDERS = table(name("orders"));
    static final Field<Integer> ORDER_ID = field(name("order_id"), SQLDataType.INTEGER);
    static final Field<Integer> PRODUCT_ID = field(name("product_id"), SQLDataType.INTEGER);
    static final Field<Integer> QUANTITY = field(name("quantity"), SQLDataType.INTEGER);

    public record CategoryRevenue(String category, BigDecimal revenue) { }

    public record ProductLine(String name, Integer quantity) { }

    private final DSLContext ctx;

    public JooqQueries(Connection connection) {
        this.ctx = DSL.using(connection, SQLDialect.H2);
    }

    public void createAndFill() {
        ctx.createTable(PRODUCT).column(ID).column(NAME).column(CATEGORY).column(PRICE).column(SOLD)
                .primaryKey(ID).execute();
        ctx.createTable(ORDERS).column(ORDER_ID).column(PRODUCT_ID).column(QUANTITY).primaryKey(ORDER_ID).execute();

        Object[][] rows = {
                {1, "Laptop", "Tech", "1200", 5}, {2, "Mouse", "Tech", "25", 40}, {3, "Desk", "Home", "300", 7},
                {4, "Lamp", "Home", "40", 15}, {5, "Novel", "Books", "15", 60}, {6, "Atlas", "Books", "8", 12},
                {7, "Monitor", "Tech", "280", 9}};
        for (Object[] r : rows) {
            ctx.insertInto(PRODUCT, ID, NAME, CATEGORY, PRICE, SOLD)
                    .values((Integer) r[0], (String) r[1], (String) r[2], new BigDecimal((String) r[3]), (Integer) r[4])
                    .execute();
        }
        ctx.insertInto(ORDERS, ORDER_ID, PRODUCT_ID, QUANTITY)
                .values(1, 1, 1).values(2, 2, 2).values(3, 5, 3).values(4, 7, 1)
                .execute();
    }

    /** LINQ: products.Where(p => p.Price > min).OrderByDescending(p => p.Price).Select(p => p.Name) */
    public ResultQuery<Record1<String>> expensiveProductNames(BigDecimal min) {
        return ctx.select(NAME).from(PRODUCT).where(PRICE.gt(min)).orderBy(PRICE.desc());   // built, NOT executed
    }

    /** The "top categories by revenue" query: Where + GroupBy + Sum + OrderByDescending + Take. */
    public List<CategoryRevenue> topCategoriesByRevenue(int top) {
        Field<BigDecimal> revenue = sum(PRICE.mul(SOLD)).as("revenue");
        return ctx.select(CATEGORY, revenue)
                .from(PRODUCT)
                .where(PRICE.gt(BigDecimal.TEN))
                .groupBy(CATEGORY)
                .orderBy(revenue.desc())
                .limit(top)
                .fetchInto(CategoryRevenue.class);
    }

    /** Like composing an IQueryable step by step: optional filters become one SQL WHERE clause. */
    public ResultQuery<Record2<String, BigDecimal>> search(String category, BigDecimal minPrice, BigDecimal maxPrice) {
        Condition where = noCondition();
        if (category != null) {
            where = where.and(CATEGORY.eq(category));
        }
        if (minPrice != null) {
            where = where.and(PRICE.ge(minPrice));
        }
        if (maxPrice != null) {
            where = where.and(PRICE.le(maxPrice));
        }
        return ctx.select(NAME, PRICE).from(PRODUCT).where(where).orderBy(NAME);
    }

    /** LINQ Join: orders.Join(products, o => o.ProductId, p => p.Id, (o, p) => new { p.Name, o.Quantity }) */
    public List<ProductLine> orderLines() {
        return ctx.select(NAME, QUANTITY)
                .from(ORDERS)
                .join(PRODUCT).on(PRODUCT_ID.eq(ID))
                .orderBy(ORDER_ID)
                .fetchInto(ProductLine.class);
    }

    /** LINQ GroupBy + Count. */
    public Result<Record2<String, Integer>> countPerCategory() {
        return ctx.select(CATEGORY, count().as("products")).from(PRODUCT).groupBy(CATEGORY).orderBy(CATEGORY).fetch();
    }

    public static void main(String[] args) throws SQLException {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:shop")) {
            JooqQueries q = new JooqQueries(connection);
            q.createAndFill();

            ResultQuery<Record1<String>> query = q.expensiveProductNames(new BigDecimal("100"));
            System.out.println("Deferred query object, SQL not executed yet:");
            System.out.println("  " + query.getSQL(ParamType.INLINED));
            System.out.println("Executed: " + query.fetch(NAME));

            System.out.println("\nTop categories by revenue: " + q.topCategoriesByRevenue(3));

            ResultQuery<Record2<String, BigDecimal>> search = q.search("Tech", new BigDecimal("20"), null);
            System.out.println("\nComposed search SQL:\n  " + search.getSQL(ParamType.INLINED));
            System.out.println("Result: " + search.fetch().map(r -> r.get(NAME) + "=" + r.get(PRICE)));
            System.out.println("No filters SQL:\n  " + q.search(null, null, null).getSQL(ParamType.INLINED));

            System.out.println("\nJoin: " + q.orderLines());
            System.out.println("GroupBy + Count: " + q.countPerCategory().map(r -> r.get(CATEGORY) + "=" + r.get("products")));
        }
    }
}
