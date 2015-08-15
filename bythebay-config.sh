# SSH
echo ...Configuring SSH Part 1 of 2...
service ssh start
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
mkdir -p ~/.ssh
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
chmod 600 ~/.ssh/id_rsa

# Datasets
echo ...Decompressing Datasets...
bzip2 -d -k datasets/dating/gender.json.bz2
bzip2 -d -k datasets/dating/gender.csv.bz2
bzip2 -d -k datasets/dating/ratings.json.bz2
bzip2 -d -k datasets/dating/ratings.csv.bz2
tar xf datasets/ml-latest-small.tar.bz2 -C datasets

# MySQL (Required by HiveQL Exercises)
echo ...Configurating MySQL...
service mysql start
mysqladmin -u root password "password"
nohup service mysql stop
echo ...Ignore the ERROR 2002 above...

# Cassandra
echo ...Configuring Cassandra...
mv $CASSANDRA_HOME/conf/cassandra-env.sh $CASSANDRA_HOME/conf/cassandra-env.sh.orig
mv $CASSANDRA_HOME/conf/cassandra.yaml $CASSANDRA_HOME/conf/cassandra.yaml.orig
ln -s $PIPELINE_HOME/config/cassandra/cassandra-env.sh $CASSANDRA_HOME/conf
ln -s $PIPELINE_HOME/config/cassandra/cassandra.yaml $CASSANDRA_HOME/conf

# Spark
echo ...Configuring Spark...
ln -s $PIPELINE_HOME/config/spark/spark-defaults.conf $SPARK_HOME/conf
ln -s $PIPELINE_HOME/config/spark/spark-env.sh $SPARK_HOME/conf
ln -s $PIPELINE_HOME/config/spark/metrics.properties $SPARK_HOME/conf
ln -s $PIPELINE_HOME/config/hadoop/hive-site.xml $SPARK_HOME/conf
ln -s $MYSQL_CONNECTOR_JAR $SPARK_HOME/lib

# Kafka
echo ...Configuring Kafka...

# ZooKeeper
echo ...Configuring ZooKeeper...

# SBT
echo ...Configuring SBT...

# Spark-Notebook
echo ...Configuring Spark-Notebook...
ln -s $PIPELINE_HOME/notebooks/spark-notebook/pipeline $SPARK_NOTEBOOK_HOME/notebooks

# Spark-JobServer
echo ...Configuring Spark Job Server...
cp $PIPELINE_HOME/config/spark-jobserver/pipeline.conf $SPARK_JOBSERVER_HOME/

# SSH (Part 2/2)
echo ...Configuring SSH Part 2 of 2
# We need to keep the SSH service running for other services to be configured above
service ssh stop
