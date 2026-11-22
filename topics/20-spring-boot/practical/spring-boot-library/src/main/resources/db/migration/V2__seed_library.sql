insert into author (name, country) values ('Joshua Bloch', 'USA');
insert into author (name, country) values ('Robert C. Martin', 'USA');
insert into author (name, country) values ('Martin Fowler', 'UK');

insert into book (title, isbn, price, published_on, author_id) values ('Effective Java', '9780134685991', 45.00, date '2018-01-06', 1);
insert into book (title, isbn, price, published_on, author_id) values ('Java Puzzlers', '9780321336781', 30.00, date '2005-07-04', 1);
insert into book (title, isbn, price, published_on, author_id) values ('Clean Code', '9780132350884', 40.00, date '2008-08-01', 2);
insert into book (title, isbn, price, published_on, author_id) values ('Clean Architecture', '9780134494166', 35.00, date '2017-09-10', 2);
insert into book (title, isbn, price, published_on, author_id) values ('Refactoring', '9780134757599', 50.00, date '2018-11-20', 3);
