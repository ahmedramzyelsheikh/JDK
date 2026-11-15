-- Repeatable migration: re-applied every time this file changes (good for views and functions).
create or replace view customer_summary as
select c.id, c.email, c.first_name, c.last_name, count(o.id) as order_count
from customer c
left join orders o on o.customer_id = c.id
group by c.id, c.email, c.first_name, c.last_name;
