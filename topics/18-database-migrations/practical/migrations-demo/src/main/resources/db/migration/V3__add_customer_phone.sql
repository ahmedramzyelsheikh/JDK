-- A new optional column: nullable, so the currently running app version keeps working.
alter table customer add column phone varchar(20);
