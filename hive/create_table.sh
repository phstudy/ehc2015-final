#!/usr/bin/env bash

echo 'create train_order table'
hive -e "
DROP TABLE IF EXISTS train_order;
CREATE TABLE train_order (
        pid STRING,
        ts timestamp,
        ip STRING,
        price INT,
        num INT,
        uid STRING,
        eruid STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '/root/dataset/order.csv' OVERWRITE INTO TABLE train_order";

echo 'create train_category table'
hive -e "
DROP TABLE IF EXISTS train_category;
CREATE TABLE train_category ( 
	pid STRING, 
	class1 STRING, 
	class2 STRING, 
	class3 STRING, 
	class4 STRING, 
	class5 STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '/root/dataset/train_category.csv' OVERWRITE INTO TABLE train_category";

echo 'create test_category table'
hive -e "
DROP TABLE IF EXISTS test_category;
CREATE TABLE test_category ( 
	pid STRING, 
	class1 STRING, 
	class2 STRING, 
	class3 STRING, 
	class4 STRING, 
	class5 STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '/root/dataset/test_category.csv' OVERWRITE INTO TABLE test_category";

echo 'create train_search table'
hive -e "
DROP TABLE IF EXISTS train_search;
CREATE TABLE train_search ( 
	ip STRING, 
	ts timestamp, 
	uid STRING, 
	keywords STRING, 
	eruid STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '/root/dataset/train_search.csv' OVERWRITE INTO TABLE train_search";

echo 'create test_search table'
hive -e "
DROP TABLE IF EXISTS test_search;
CREATE TABLE test_search ( 
	ip STRING, 
	ts timestamp, 
	uid STRING, 
	keywords STRING, 
	eruid STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '/root/dataset/test_search.csv' OVERWRITE INTO TABLE test_search";

echo 'create price table'
hive -e "
DROP TABLE IF EXISTS price;
CREATE TABLE price ( 
	pid STRING, 
	price INT
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '/root/dataset/price.csv' OVERWRITE INTO TABLE price";

echo 'create train_view table'
hive -e "
DROP TABLE IF EXISTS train_view;
CREATE TABLE train_view ( 
	pid STRING, 
	ts timestamp, 
	ip STRING, 
	uid STRING, 
	eruid STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '/root/dataset/train_view.csv' OVERWRITE INTO TABLE train_view";

echo 'create train_view_order table'
hive -e "
DROP TABLE IF EXISTS train_view_order;
CREATE TABLE train_view_order (
        pid STRING,
        ts timestamp,
        ip STRING,
        uid STRING,
        eruid STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '/root/dataset/train_view_order.csv' OVERWRITE INTO TABLE train_view_order";

echo 'create test_view table'
hive -e "
DROP TABLE IF EXISTS test_view;
CREATE TABLE test_view ( 
	pid STRING, 
	ts timestamp, 
	ip STRING, 
	uid STRING, 
	eruid STRING
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '/root/dataset/test_view.csv' OVERWRITE INTO TABLE test_view";
