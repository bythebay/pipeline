# ~/.profile: executed by Bourne-compatible login shells.

if [ "$BASH" ]; then
  if [ -f ~/.bashrc ]; then
    . ~/.bashrc
  fi
fi

mesg n

# Dev Install
export DEV_INSTALL_HOME=~

# Data Home
export DATA_HOME=~

# Logs Home
export LOGS_HOME=~

# Pipeline Home
export PIPELINE_HOME=~/pipeline

# Java Home
export JAVA_HOME=/usr

# MySQL
export MYSQL_CONNECTOR_JAR=/usr/share/java/mysql-connector-java.jar

# Cassandra
export CASSANDRA_HOME=$DEV_INSTALL_HOME/apache-cassandra-2.2.0
export PATH=$PATH:$CASSANDRA_HOME/bin

# Spark
export SPARK_HOME=$DEV_INSTALL_HOME/spark-1.4.1-bin-fluxcapacitor
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin
export SPARK_EXAMPLES_JAR=$SPARK_HOME/lib/spark-examples-1.4.1-hadoop2.6.0.jar

# Kafka
export KAFKA_HOME=$DEV_INSTALL_HOME/confluent-1.0
export PATH=$PATH:$KAFKA_HOME/bin

# ZooKeeper
export ZOOKEEPER_HOME=$KAFKA_HOME/bin
export PATH=$PATH:$ZOOKEEPER_HOME/bin

# SBT
export SBT_HOME=$DEV_INSTALL_HOME/sbt
export PATH=$PATH:$SBT_HOME/bin
export SBT_OPTS="-Xmx10G -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=2G"

# Spark Notebook
export SPARK_NOTEBOOK_HOME=$DEV_INSTALL_HOME/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet
export PATH=$PATH:$SPARK_NOTEBOOK_HOME/bin

# Spark JobServer
export SPARK_JOBSERVER_HOME=$DEV_INSTALL_HOME/spark-jobserver-0.5.2
