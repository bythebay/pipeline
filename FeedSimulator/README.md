Feed Simulator Setup - Local Server
====================================

copy ratings.dat.bz2 to the data directory and unzip it.  If you need it in a different place change the FeederActor.

sbt run -Dspark.cassandra.connection.host=52.8.63.225 -Dzookeeper.host=localhost:2181
