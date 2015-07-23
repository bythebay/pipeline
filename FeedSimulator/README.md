Feed Simulator Setup - Local Server
====================================

1. Kafka needs to be running before this runs. See the setup in the parent readme.

2. This assumes the data is in a directory called data.  You need to create this directory first.  Then copy ratings.dat.bz2 to the data directory and unzip it.  If you need it in a different place change the FeederActor.

3. Run it with `sbt run`
