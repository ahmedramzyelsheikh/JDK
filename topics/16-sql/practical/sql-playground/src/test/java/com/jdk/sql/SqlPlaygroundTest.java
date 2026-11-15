package com.jdk.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Every SQL concept of topic 16 as an executable example with its expected result.
 * Each test gets a brand-new database (schema.sql + data.sql), so tests never affect each other.
 */
class SqlPlaygroundTest {

    Database db;

    @BeforeEach
    void freshDatabase() {
        db = Database.createShop();
    }

    @Nested
    class Querying {

        @Test
        void filterAndSort() {
            assertThat(db.rows("""
                    select name, price from product
                    where price < 50
                    order by price desc
                    """)).containsExactly(
                    "Effective Java | 45", "Clean Code | 40", "SQL Antipatterns | 35", "Desk Lamp | 30", "Mouse | 25");
        }

        @Test
        void innerJoin() {
            assertThat(db.rows("""
                    select o.id, c.name, o.ordered_on
                    from orders o
                    join customer c on c.id = o.customer_id
                    order by o.id
                    """)).containsExactly(
                    "1 | Ali | 2026-01-10", "2 | Ali | 2026-02-14", "3 | Sara | 2026-02-20",
                    "4 | Omar | 2026-03-05", "5 | Mona | 2026-03-18");
        }

        @Test
        void leftJoinKeepsCustomersWithoutOrders() {
            assertThat(db.rows("""
                    select c.name, count(o.id) as orders
                    from customer c
                    left join orders o on o.customer_id = c.id
                    group by c.id, c.name
                    order by c.id
                    """)).containsExactly("Ali | 2", "Sara | 1", "Omar | 1", "Mona | 1", "Hany | 0");
        }

        @Test
        void antiJoinTwoWays() {
            assertThat(db.rows("""
                    select c.name from customer c
                    left join orders o on o.customer_id = c.id
                    where o.id is null
                    """)).containsExactly("Hany");
            assertThat(db.rows("""
                    select c.name from customer c
                    where not exists (select 1 from orders o where o.customer_id = c.id)
                    """)).containsExactly("Hany");
        }

        @Test
        void semiJoinWithExists() {
            assertThat(db.rows("""
                    select c.name from customer c
                    where exists (
                        select 1 from orders o
                        join order_item oi on oi.order_id = o.id
                        join product p on p.id = oi.product_id
                        join category cat on cat.id = p.category_id
                        where o.customer_id = c.id and cat.name = 'Books')
                    order by c.name
                    """)).containsExactly("Ali", "Mona");
        }

        @Test
        void groupByWithHaving() {
            assertThat(db.rows("""
                    select cat.name, sum(oi.quantity * oi.unit_price) as revenue
                    from order_item oi
                    join orders o     on o.id = oi.order_id
                    join product p    on p.id = oi.product_id
                    join category cat on cat.id = p.category_id
                    where o.status <> 'CANCELLED'
                    group by cat.name
                    having sum(oi.quantity * oi.unit_price) > 100
                    order by revenue desc
                    """)).containsExactly("Laptops | 1500", "Tech | 330", "Books | 230");
        }

        @Test
        void scalarSubquery() {
            assertThat(db.rows("""
                    select name from product
                    where price > (select avg(price) from product)
                    order by price desc
                    """)).containsExactly("Laptop Pro", "Laptop Air");
        }

        @Test
        void conditionalAggregationWithFilter() {
            assertThat(db.rows("""
                    select count(*) as total,
                           count(*) filter (where status = 'SHIPPED')   as shipped,
                           count(*) filter (where status = 'CANCELLED') as cancelled
                    from orders
                    """)).containsExactly("5 | 2 | 1");
        }
    }

    @Nested
    class CtesAndWindowFunctions {

        @Test
        void topTwoProductsPerCategory() {
            assertThat(db.rows("""
                    with ranked as (
                        select p.name, cat.name as category, p.category_id,
                               row_number() over (partition by p.category_id order by p.price desc) as rn
                        from product p join category cat on cat.id = p.category_id
                    )
                    select category, name, rn from ranked
                    where rn <= 2
                    order by category_id, rn
                    """)).containsExactly(
                    "Tech | Monitor | 1", "Tech | Keyboard | 2",
                    "Books | Effective Java | 1", "Books | Clean Code | 2",
                    "Home | Office Chair | 1", "Home | Desk Lamp | 2",
                    "Laptops | Laptop Pro | 1", "Laptops | Laptop Air | 2");
        }

        @Test
        void runningTotalAndLag() {
            assertThat(db.rows("""
                    with order_total as (
                        select o.id, o.ordered_on, sum(oi.quantity * oi.unit_price) as revenue
                        from orders o join order_item oi on oi.order_id = o.id
                        group by o.id, o.ordered_on
                    )
                    select id, revenue,
                           sum(revenue) over (order by ordered_on) as running_total,
                           revenue - lag(revenue) over (order by ordered_on) as change
                    from order_total
                    order by ordered_on
                    """)).containsExactly(
                    "1 | 1550 | 1550 | null",
                    "2 | 125 | 1675 | -1425",
                    "3 | 340 | 2015 | 215",
                    "4 | 1100 | 3115 | 760",
                    "5 | 105 | 3220 | -995");
        }

        @Test
        void recursiveCteWalksTheCategoryTree() {
            assertThat(db.rows("""
                    with recursive tree(id, path) as (
                        select id, cast(name as varchar(200)) from category where parent_id is null
                        union all
                        select c.id, t.path || ' > ' || c.name
                        from category c join tree t on c.parent_id = t.id
                    )
                    select path from tree order by path
                    """)).containsExactly("All", "All > Books", "All > Home", "All > Tech", "All > Tech > Laptops");
        }
    }

    @Nested
    class NullLogic {

        @Test
        void notEqualsSilentlyDropsNulls() {
            assertThat(db.rows("select name from customer where city <> 'Cairo' order by id"))
                    .containsExactly("Sara", "Hany");                                    // Mona (NULL city) is missing!
            assertThat(db.rows("select name from customer where city is distinct from 'Cairo' order by id"))
                    .containsExactly("Sara", "Mona", "Hany");
        }

        @Test
        void notInWithANullReturnsNothing() {
            assertThat(db.rows("select name from customer where city not in ('Giza', null)")).isEmpty();
        }

        @Test
        void coalesceAndCountColumnIgnoresNulls() {
            assertThat(db.rows("""
                    select coalesce(city, 'unknown') as city, count(*) from customer
                    group by coalesce(city, 'unknown') order by 1
                    """)).containsExactly("Alexandria | 1", "Cairo | 2", "Giza | 1", "unknown | 1");
            assertThat(db.rows("select count(*), count(city) from customer")).containsExactly("5 | 4");
        }
    }

    @Nested
    class Writing {

        @Test
        void mergeUpdatesExistingAndInsertsNew() {
            String merge = """
                    merge into product p
                    using (select cast(? as varchar(100)) as name, 2 as category_id, cast(? as numeric(10,2)) as price) s
                    on p.name = s.name
                    when matched then update set price = s.price
                    when not matched then insert (name, category_id, price) values (s.name, s.category_id, s.price)
                    """;
            try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(merge)) {
                ps.setString(1, "Mouse");
                ps.setBigDecimal(2, new java.math.BigDecimal("19.99"));
                ps.executeUpdate();
                ps.setString(1, "Webcam");
                ps.setBigDecimal(2, new java.math.BigDecimal("75.00"));
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }

            assertThat(db.rows("select name, price from product where name in ('Mouse', 'Webcam') order by name"))
                    .containsExactly("Mouse | 19.99", "Webcam | 75");
            assertThat(db.rows("select count(*) from product")).containsExactly("11");
        }

        @Test
        void constraintsProtectTheData() {
            assertThatThrownBy(() -> db.update("insert into product (name, category_id, price) values ('Bad', 2, -1)"))
                    .hasMessageContaining("Check constraint");
            assertThatThrownBy(() -> db.update("insert into order_item values (1, 999, 1, 10)"))
                    .hasMessageContaining("Referential integrity");
            assertThatThrownBy(() -> db.update("insert into customer (email, name) values ('ali@x.com', 'Ali 2')"))
                    .hasMessageContaining("Unique index or primary key violation");
        }

        @Test
        void deleteCascadesToOrderItems() {
            db.update("delete from orders where id = 1");
            assertThat(db.rows("select count(*) from order_item where order_id = 1")).containsExactly("0");
        }
    }

    @Nested
    class Transactions {

        @Test
        void allOrNothingWithRollback() throws SQLException {
            try (Connection c = db.connect()) {
                c.setAutoCommit(false);                                       // BEGIN
                try (Statement st = c.createStatement()) {
                    st.executeUpdate("update product set stock = stock + 50 where id = 3");   // succeeds...
                    st.executeUpdate("update product set stock = stock - 50 where id = 1");   // ...violates stock >= 0
                    c.commit();
                } catch (SQLException e) {
                    c.rollback();                                             // undo BOTH updates
                }
            }
            assertThat(db.rows("select id, stock from product where id in (1, 3) order by id"))
                    .containsExactly("1 | 5", "3 | 100");
        }

        @Test
        void committedTransferIsVisible() throws SQLException {
            try (Connection c = db.connect()) {
                c.setAutoCommit(false);
                try (Statement st = c.createStatement()) {
                    st.executeUpdate("update product set stock = stock - 5 where id = 3");
                    st.executeUpdate("update product set stock = stock + 5 where id = 5");
                }
                c.commit();
            }
            assertThat(db.rows("select id, stock from product where id in (3, 5) order by id"))
                    .containsExactly("3 | 95", "5 | 45");
        }

        @Test
        void readCommittedPreventsDirtyReads() throws SQLException {
            try (Connection writer = db.connect(); Connection reader = db.connect()) {
                writer.setAutoCommit(false);
                writer.createStatement().executeUpdate("update product set price = 999 where id = 3");   // not committed

                assertThat(price(reader)).isEqualTo("25.00");               // reader does NOT see the uncommitted 999

                writer.rollback();
            }
        }

        @Test
        void repeatableReadPreventsNonRepeatableReads() throws SQLException {
            try (Connection reader = db.connect(); Connection writer = db.connect()) {
                reader.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                reader.setAutoCommit(false);
                String first = price(reader);

                writer.createStatement().executeUpdate("update product set price = 30 where id = 3");   // auto-commit

                String second = price(reader);
                reader.commit();

                assertThat(first).isEqualTo("25.00");
                assertThat(second).isEqualTo("25.00");                       // same snapshot inside the transaction
                assertThat(price(reader)).isEqualTo("30.00");                // new transaction sees the update
            }
        }

        private String price(Connection c) throws SQLException {
            try (ResultSet rs = c.createStatement().executeQuery("select price from product where id = 3")) {
                rs.next();
                return rs.getBigDecimal(1).toPlainString();
            }
        }
    }

    @Nested
    class Indexes {

        @Test
        void compositeIndexIsUsedForItsLeftmostColumn() {
            String usesIndex = String.join("\n", db.rows(
                    "explain select * from orders where customer_id = 1 and ordered_on > date '2026-01-01'"));
            String withoutLeftColumn = String.join("\n", db.rows(
                    "explain select * from orders where ordered_on > date '2026-01-01'"));

            assertThat(usesIndex.toLowerCase()).contains("idx_orders_customer_date");
            assertThat(withoutLeftColumn.toLowerCase()).doesNotContain("idx_orders_customer_date: customer_id");
        }

        @Test
        void sqlInjectionIsHarmlessWithBindParameters() throws SQLException {
            String attack = "x' or '1'='1";
            assertThat(db.rows("select name from customer where name = '" + attack + "'")).hasSize(5);   // concatenation: ALL rows!

            try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement("select name from customer where name = ?")) {
                ps.setString(1, attack);
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(Database.toRows(rs)).isEmpty();                                     // treated as a value
                }
            }
        }
    }
}
