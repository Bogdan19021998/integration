drop database if exists mysql_sync_test;
create database mysql_sync_test;


CREATE TABLE mysql_sync_test.distil_customers
(
  id         INT            NOT NULL PRIMARY KEY,
  birth_date DATE           NOT NULL,
  first_name VARCHAR(14)    NOT NULL,
  last_name  VARCHAR(16)    NOT NULL,
  gender     ENUM('M', 'F') NOT NULL,
  hire_date  DATE           NOT NULL,
  some_null  VARCHAR(1024)
);

insert into mysql_sync_test.distil_customers
values (1, '2019-01-01', 'name 1', 'last name 1', 'M', '2019-01-01', null);
insert into mysql_sync_test.distil_customers
values (2, '2029-02-02', 'name 2', 'last name 2', 'M', '2029-02-02', null);
insert into mysql_sync_test.distil_customers
values (3, '3039-03-03', 'name 3', 'last name 3', 'F', '3039-03-03', null);
