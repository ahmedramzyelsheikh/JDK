-- EXPAND step of an expand/contract change: add the new columns next to the old one and backfill.
-- The old "name" column stays until every app version stops using it (dropped in a later CONTRACT migration).
alter table customer add column first_name varchar(50);
alter table customer add column last_name varchar(50);

update customer
set first_name = substring(name, 1, position(' ' in name) - 1),
    last_name  = substring(name, position(' ' in name) + 1)
where position(' ' in name) > 0;
