#!/usr/bin/env bash

echo 'create parquet_train_category table'
impala-shell -q "
DROP TABLE IF EXISTS parquet_train_category;
CREATE TABLE parquet_train_category ( 
	pid STRING, 
	class1 STRING, 
	class2 STRING, 
	class3 STRING, 
	class4 STRING, 
	class5 STRING
) stored as parquetfile;
insert into parquet_train_category select * from train_category;"

echo 'create parquet_test_category table'
impala-shell -q "
DROP TABLE IF EXISTS parquet_test_category;
CREATE TABLE parquet_test_category ( 
	pid STRING, 
	class1 STRING, 
	class2 STRING, 
	class3 STRING, 
	class4 STRING, 
	class5 STRING
) stored as parquetfile;
insert into parquet_test_category select * from test_category";

echo 'create parquet_train_search table'
impala-shell -q "
DROP TABLE IF EXISTS parquet_train_search;
CREATE TABLE parquet_train_search ( 
	ip STRING, 
	ts timestamp, 
	uid STRING, 
	keywords STRING, 
	eruid STRING
) stored as parquetfile;
insert into parquet_train_search select * from train_search";

echo 'create parquet_test_search table'
impala-shell -q "
DROP TABLE IF EXISTS parquet_test_search;
CREATE TABLE parquet_test_search ( 
	ip STRING, 
	ts timestamp, 
	uid STRING, 
	keywords STRING, 
	eruid STRING
) stored as parquetfile;
insert into parquet_test_search select * from test_search";

echo 'create parquet_price table'
impala-shell -q "
DROP TABLE IF EXISTS parquet_price;
CREATE TABLE parquet_price ( 
	pid STRING, 
	price INT
) stored as parquetfile;
insert into parquet_price select * from price";

echo 'create parquet_train_view table'
impala-shell -q "
DROP TABLE IF EXISTS parquet_train_view;
CREATE TABLE parquet_train_view ( 
	pid STRING, 
	ts timestamp, 
	ip STRING, 
	uid STRING, 
	eruid STRING
) stored as parquetfile;
insert into parquet_train_view select * from train_view";

echo 'create parquet_train_view_order table'
impala-shell -q "
DROP TABLE IF EXISTS parquet_train_view_order;
CREATE TABLE parquet_train_view_order ( 
	pid STRING, 
	ts timestamp, 
	ip STRING, 
	uid STRING, 
	eruid STRING,
	eturec INT,
    buy INT,
    num INT,
    viewnum INT
) stored as parquetfile;
insert into parquet_train_view_order select * from train_view_order";

echo 'create parquet_test_view table'
impala-shell -q "
DROP TABLE IF EXISTS parquet_test_view;
CREATE TABLE parquet_test_view ( 
	pid STRING, 
	ts timestamp, 
	ip STRING, 
	uid STRING, 
	eruid STRING
) stored as parquetfile;
insert into parquet_test_view select * from test_view";
