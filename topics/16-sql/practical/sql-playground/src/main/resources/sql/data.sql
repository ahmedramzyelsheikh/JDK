insert into category (name, parent_id) values ('All', null);          -- 1
insert into category (name, parent_id) values ('Tech', 1);             -- 2
insert into category (name, parent_id) values ('Books', 1);            -- 3
insert into category (name, parent_id) values ('Home', 1);             -- 4
insert into category (name, parent_id) values ('Laptops', 2);          -- 5

insert into product (name, category_id, price, stock) values ('Laptop Pro', 5, 1500.00, 5);        -- 1
insert into product (name, category_id, price, stock) values ('Laptop Air', 5, 1100.00, 8);        -- 2
insert into product (name, category_id, price, stock) values ('Mouse', 2, 25.00, 100);             -- 3
insert into product (name, category_id, price, stock) values ('Monitor', 2, 280.00, 15);           -- 4
insert into product (name, category_id, price, stock) values ('Keyboard', 2, 60.00, 40);           -- 5
insert into product (name, category_id, price, stock) values ('Effective Java', 3, 45.00, 30);     -- 6
insert into product (name, category_id, price, stock) values ('Clean Code', 3, 40.00, 25);         -- 7
insert into product (name, category_id, price, stock) values ('SQL Antipatterns', 3, 35.00, 10);   -- 8
insert into product (name, category_id, price, stock) values ('Desk Lamp', 4, 30.00, 50);          -- 9
insert into product (name, category_id, price, stock) values ('Office Chair', 4, 220.00, 6);       -- 10

insert into customer (email, name, city) values ('ali@x.com', 'Ali', 'Cairo');            -- 1
insert into customer (email, name, city) values ('sara@x.com', 'Sara', 'Alexandria');     -- 2
insert into customer (email, name, city) values ('omar@x.com', 'Omar', 'Cairo');          -- 3
insert into customer (email, name, city) values ('mona@x.com', 'Mona', null);             -- 4 (no city)
insert into customer (email, name, city) values ('hany@x.com', 'Hany', 'Giza');           -- 5 (never orders)

insert into orders (customer_id, ordered_on, status) values (1, date '2026-01-10', 'SHIPPED');    -- 1
insert into orders (customer_id, ordered_on, status) values (1, date '2026-02-14', 'PAID');       -- 2
insert into orders (customer_id, ordered_on, status) values (2, date '2026-02-20', 'SHIPPED');    -- 3
insert into orders (customer_id, ordered_on, status) values (3, date '2026-03-05', 'CANCELLED');  -- 4
insert into orders (customer_id, ordered_on, status) values (4, date '2026-03-18', 'NEW');        -- 5

insert into order_item (order_id, product_id, quantity, unit_price) values (1, 1, 1, 1500.00);
insert into order_item (order_id, product_id, quantity, unit_price) values (1, 3, 2, 25.00);
insert into order_item (order_id, product_id, quantity, unit_price) values (2, 6, 1, 45.00);
insert into order_item (order_id, product_id, quantity, unit_price) values (2, 7, 2, 40.00);
insert into order_item (order_id, product_id, quantity, unit_price) values (3, 4, 1, 280.00);
insert into order_item (order_id, product_id, quantity, unit_price) values (3, 9, 2, 30.00);
insert into order_item (order_id, product_id, quantity, unit_price) values (4, 2, 1, 1100.00);
insert into order_item (order_id, product_id, quantity, unit_price) values (5, 8, 3, 35.00);
