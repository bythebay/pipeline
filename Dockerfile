FROM ubuntu:14.04

ENV HOME=/root
ENV SCALA_VERSION=2.10.4
ENV SPARK_VERSION=1.4.1
ENV JOBSERVER_VERSION=0.5.2

EXPOSE 80 4042 9160 9042 9200 7077 38080 38081 6060 6061 8090 8099 10000 50070 50090 9092 6066 9000 19999 6379 6081 7474 8787 5601 8989 7979 4040

RUN \
 apt-get update \
 && apt-get install -y curl \
 && apt-get install -y wget \
 && apt-get install -y vim \

# Start in Home Dir (/root)
 && cd ~ \

# Git
 && apt-get install -y git \

# SSH
 && apt-get install -y openssh-server \

# Java
 && apt-get install -y default-jdk \

# SBT
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/sbt-0.13.8.tgz \
 && tar xvzf sbt-0.13.8.tgz \
 && rm sbt-0.13.8.tgz \
 && ln -s /root/sbt/bin/sbt /usr/local/bin \
 && rm -rf /root/.ivy2 

RUN \
# Start from root
 cd ~ \

# MySql (Required by Hive Metastore)
# Generic Install?  http://dev.mysql.com/doc/refman/5.7/en/binary-installation.html
 && DEBIAN_FRONTEND=noninteractive apt-get -y install mysql-server \
 && apt-get install -y mysql-client \
 && apt-get install -y libmysql-java \

# Apache Cassandra
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/apache-cassandra-2.2.0-bin.tar.gz \
 && tar xvzf apache-cassandra-2.2.0-bin.tar.gz \
 && rm apache-cassandra-2.2.0-bin.tar.gz \

# Apache Kafka (Confluent Distribution)
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/confluent-1.0-2.10.4.tar.gz \
 && tar xvzf confluent-1.0-2.10.4.tar.gz \
 && rm confluent-1.0-2.10.4.tar.gz \

# Apache Spark
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-1.4.1-bin-fluxcapacitor.tgz \
 && tar xvzf spark-1.4.1-bin-fluxcapacitor.tgz \
 && rm spark-1.4.1-bin-fluxcapacitor.tgz \

# Spark Notebook
 && apt-get install -y screen \
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet.tgz \
 && tar xvzf spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet.tgz \
 && rm spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet.tgz \

# Spark Job Server (1 of 2)
 && wget https://github.com/spark-jobserver/spark-jobserver/archive/v${JOBSERVER_VERSION}.tar.gz \
 && tar xvzf v${JOBSERVER_VERSION}.tar.gz \
 && rm v${JOBSERVER_VERSION}.tar.gz \
 && cd ~/spark-jobserver-${JOBSERVER_VERSION} \
 && mkdir -p logs/spark \ 
 && cd ~

RUN \
# Retrieve Latest Datasets, Configs, and Start Scripts
 git clone https://github.com/bythebay/pipeline.git \
 && chmod a+rx pipeline/*.sh \

# Spark Job Server (2 of 2)
 && cd ~/spark-jobserver-${JOBSERVER_VERSION} \
# && cp ~/pipeline/config/spark-jobserver/* config/ \
 && ln -s ~/pipeline/config/spark-jobserver/pipeline.conf ~/spark-jobserver-0.5.2/config \
 && ln -s ~/pipeline/config/spark-jobserver/pipeline.sh ~/spark-jobserver-0.5.2/config \
 && sbt job-server-tests/package \
 && bin/server_package.sh pipeline \
 && cp /tmp/job-server/* . \
 && rm -rf /tmp/job-server \
 && cd ~ \

# .profile Shell Environment Variables
 && mv ~/.profile ~/.profile.orig \
 && ln -s ~/pipeline/config/bash/.profile ~/.profile \

# Sbt Clean
 && cd pipeline \
 && sbt clean clean-files \

# Sbt Assemble Feeder Producer App
 && sbt feeder/assembly \

# Sbt Package Streaming Consumer App
 && sbt streaming/package 

WORKDIR /root
