CREATE KEYSPACE pocdb1
WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 3}
AND durable_writes = true;

USE pocdb1 ;

CREATE TABLE products(
  id bigint PRIMARY KEY,
  name text,
  description text,
  weight float
) WITH cdc=true;

CREATE TABLE products_on_hand (
  product_id bigint PRIMARY KEY,
  quantity bigint,
) WITH cdc=true;

CREATE TABLE customers (
  id bigint PRIMARY KEY,
  first_name varchar,
  last_name varchar,
  email varchar,
  insertdate text,
) WITH cdc=true;



