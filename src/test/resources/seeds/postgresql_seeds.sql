drop table if exists distil_customers;

CREATE TABLE if not exists distil_customers
(
  id         INT         NOT NULL PRIMARY KEY,
  birth_date DATE        NOT NULL,
  first_name VARCHAR(14) NOT NULL,
  last_name  VARCHAR(16) NOT NULL,
  hire_date  DATE        NOT NULL,
  some_null  VARCHAR(1024)
);

insert into distil_customers
values (1, '2019-01-01', 'name 1', 'last name 1', '2019-01-01', null);
insert into distil_customers
values (2, '2029-02-02', 'name 2', 'last name 2', '2029-02-02', null);
insert into distil_customers
values (3, '3039-03-03', 'name 3', 'last name 3', '3039-03-03', null);