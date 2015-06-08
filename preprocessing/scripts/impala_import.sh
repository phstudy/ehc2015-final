#!/usr/bin/env bash
workspace=`pwd`/EHC/

echo '####### create userdata_train table #######'

hive -e "
CREATE TABLE IF NOT EXISTS userdata_train (
	eruid STRING,
	viewcount INT,
	uniq_viewcount INT,
	cat_0 INT,
	cat_1 INT,
	cat_A INT,
	cat_B INT,
	cat_C INT,
	cat_D INT,
	cat_E INT,
	cat_F INT,
	cat_G INT,
	cat_H INT,
	cat_I INT,
	cat_J INT,
	cat_K INT,
	cat_L INT,
	cat_O INT,
	cat_V INT,
	max_cat STRING,
	buyCount INT,
	buy INT
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '${workspace}userdata_train.csv' OVERWRITE INTO TABLE userdata_train";

impala-shell -q "
CREATE TABLE IF NOT EXISTS parquet_userdata_train (
eruid STRING,
	viewcount INT,
	uniq_viewcount INT,
	cat_0 INT,
	cat_1 INT,
	cat_A INT,
	cat_B INT,
	cat_C INT,
	cat_D INT,
	cat_E INT,
	cat_F INT,
	cat_G INT,
	cat_H INT,
	cat_I INT,
	cat_J INT,
	cat_K INT,
	cat_L INT,
	cat_O INT,
	cat_V INT,
	max_cat STRING,
	buyCount INT,
	buy INT
)  stored as parquetfile;
insert into parquet_userdata_train select * from userdata_train";

echo '####### create userdata_test table #######'
hive -e "
CREATE TABLE IF NOT EXISTS userdata_test (
eruid STRING,
	viewcount INT,
	uniq_viewcount INT,
	cat_0 INT,
	cat_1 INT,
	cat_A INT,
	cat_B INT,
	cat_C INT,
	cat_D INT,
	cat_E INT,
	cat_F INT,
	cat_G INT,
	cat_H INT,
	cat_I INT,
	cat_J INT,
	cat_K INT,
	cat_L INT,
	cat_O INT,
	cat_V INT,
	max_cat STRING,
	buyCount INT,
	buy INT
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '${workspace}userdata_test.csv' OVERWRITE INTO TABLE userdata_test";

impala-shell -q "
CREATE TABLE IF NOT EXISTS parquet_userdata_test (
eruid STRING,
	viewcount INT,
	uniq_viewcount INT,
	cat_0 INT,
	cat_1 INT,
	cat_A INT,
	cat_B INT,
	cat_C INT,
	cat_D INT,
	cat_E INT,
	cat_F INT,
	cat_G INT,
	cat_H INT,
	cat_I INT,
	cat_J INT,
	cat_K INT,
	cat_L INT,
	cat_O INT,
	cat_V INT,
	max_cat STRING,
	buyCount INT,
	buy INT
)  stored as parquetfile;
insert into parquet_userdata_test select * from userdata_test";

echo '####### create product_train table #######'
hive -e "
CREATE TABLE IF NOT EXISTS product_train (
	pid STRING,
	view INT,
	viewBySession INT,
	price INT,
	cat STRING,
	buyCount INT
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '${workspace}product_train.csv' OVERWRITE INTO TABLE product_train";

impala-shell -q "
CREATE TABLE IF NOT EXISTS parquet_product_train (
	pid STRING,
	view INT,
	viewBySession INT,
	price INT,
	cat STRING,
	buyCount INT
)  stored as parquetfile;
insert into parquet_product_train select * from product_train";

echo '####### create product_test table #######'
hive -e "
CREATE TABLE IF NOT EXISTS product_test (
	pid STRING,
	view INT,
	viewBySession INT,
	price INT,
	cat STRING,
	buyCount INT
) ROW FORMAT DELIMITED FIELDS TERMINATED BY \",\";
LOAD DATA LOCAL INPATH '${workspace}product_test.csv' OVERWRITE INTO TABLE product_test";

impala-shell -q "
CREATE TABLE parquet_product_test (
	pid STRING,
	view INT,
	viewBySession INT,
	price INT,
	cat STRING,
	buyCount INT
)  stored as parquetfile;
insert into parquet_product_test select * from product_test";

