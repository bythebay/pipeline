# pipeline

[![Join the chat at https://gitter.im/bythebay/pipeline](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/bythebay/pipeline?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
Complete Pipeline Training at Big Data Scala By the Bay

We want to create a uniform data pipeline, e.g. tweets, flowing through Akka=>Kafka=>Spark=>Cassandra.  E.g. [Helena Edelson](https://twitter.com/helenaedelson) has a [KillrWeather](https://github.com/killrweather/killrweather) app that can be reused.

We want to make sure that each system can be setup and the pipeline run from the same repo.  We'll converge on the exact app together.

The training will be a single day, with 5 companies teaching an hour-90 min segment each in sequence, in the above order.  Mesosphere will be underpinning it with Mesos and go first.


Kafka Server Setup - Local Only Testing
===================================

Start Kafka, create the topics and test:

bin/zookeeper-server-start.sh config/zookeeper.properties

bin/kafka-server-start.sh config/server.properties

bin/kafka-create-topic.sh --zookeeper localhost:2181 --replica 1 --partition 1 --topic ratings

bin/kafka-list-topic.sh --zookeeper localhost:2181

bin/kafka-console-producer.sh --broker-list localhost:9092 --topic ratings

bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic ratings --from-beginning

Kafka Server Setup - Remote Server Setup
===================================

mkdir runlogs

nohup bin/zookeeper-server-start.sh config/zookeeper.properties > runlogs/zookeeper.log 2> runlogs/zookeeper.err < /dev/null &

nohup bin/kafka-server-start.sh config/server.properties > runlogs/kafka.log 2> runlogs/kafka.err < /dev/null &

bin/kafka-create-topic.sh --zookeeper localhost:2181 --replica 1 --partition 1 --topic acc_data

bin/kafka-list-topic.sh --zookeeper localhost:2181

bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic acc_data --from-beginning

Setup SBT
====================================

[Install sbt] (http://www.scala-sbt.org/release/tutorial/Setup.html)

Cassandra Setup
=====================================

Install Cassandra as usal.  With EC2 the following changes need to be made:
Modify cassandra.yaml
Change broadcast_address to be the outside address in EC2  - broadcast_rpc_address: 52.8.63.225
Modify the seeds to point to other servers


Rating Data Analyzer Setup - Local Server
====================================

sbt run -Dspark.cassandra.connection.host=52.8.63.225 -Dzookeeper.host=localhost:2181

Rating Data Setup - Remote Server
====================================

on local machine bundle the server for deployment:

sbt assembly

copy target/scala-2.10/SensorAnalyzer-assembly-0.2.0.jar to remote server.

on remote machine:

mkdir runlogs

nohup java  -Dspark.cassandra.connection.host=52.8.63.225 -Dzookeeper.host=localhost:2181 -jar SensorAnalyzer-assembly-0.2.0.jar  > runlogs/analyzer.log 2> runlogs/analyzer.err < /dev/null &

./bin/spark-submit --name "My app" --master local[4] --conf spark.shuffle.spill=false
  --conf "spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps" myApp.jar


How to Verify Results in Cassandra
========================================

* verify result in cqlsh:

dse/bin/cqlsh

cqlsh> use sensors;

cqlsh:test> select * from sensors.acceleration limit 10;
