#!/bin/bash

echo ...Creating Kafka Topics...
kafka-topics --zookeeper localhost:2181 --create --topic ratings --partitions 1 --replication-factor 1

echo ...Creating Cassandra Keyspaces Column Families and Tables...
#cqlsh -e "DROP KEYSPACE IF EXISTS pipeline;"
cqlsh -e "CREATE KEYSPACE pipeline WITH REPLICATION = { 'class': 'SimpleStrategy',  'replication_factor':1};"
#cqlsh -e "USE pipeline; DROP TABLE IF EXISTS real_time_ratings;"
cqlsh -e "USE pipeline; CREATE TABLE real_time_ratings (fromUserId int, toUserId int, rating int, batchTime bigint, PRIMARY KEY(fromUserId, toUserId));"

echo ...Creating Reference Data in Hive...
#spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'DROP TABLE IF EXISTS gender_json_file'
spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'CREATE TABLE gender_json_file(id INT, gender STRING) USING org.apache.spark.sql.json OPTIONS (path "datasets/dating/gender.json.bz2")'
