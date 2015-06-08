#!/usr/bin/env bash

hive -e "DROP TABLE IF EXISTS userdata_train"
hive -e "DROP TABLE IF EXISTS userdata_test"
hive -e "DROP TABLE IF EXISTS product_train"
hive -e "DROP TABLE IF EXISTS product_test"

hive -e "DROP TABLE IF EXISTS parquet_userdata_train"
hive -e "DROP TABLE IF EXISTS parquet_userdata_test"
hive -e "DROP TABLE IF EXISTS parquet_product_train"
hive -e "DROP TABLE IF EXISTS parquet_product_test"

hdfs dfs -expunge
