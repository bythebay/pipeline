import AssemblyKeys._

name := "RaitingDataAnalyzer"

version := "0.2.0"

scalaVersion := "2.10.4"

val Spark = "1.1.0"
val SparkCassandra = "1.1.0"

val Json4s         = "3.2.10"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.10" % Spark % "provided",
  ("com.datastax.spark" %% "spark-cassandra-connector" % SparkCassandra withSources() withJavadoc()).
    exclude("com.esotericsoftware.minlog", "minlog").
    exclude("commons-beanutils","commons-beanutils").
    exclude("org.apache.spark","spark-core"),
  ("com.datastax.spark" %% "spark-cassandra-connector-java" % SparkCassandra withSources() withJavadoc()).
    exclude("org.apache.spark","spark-core"),
  "net.jpountz.lz4" % "lz4" % "1.2.0",
  ("org.apache.kafka" % "kafka_2.10" % "0.8.0").
    exclude("org.slf4j","slf4j-simple"),
  "org.apache.spark" %% "spark-streaming-kafka" % "1.1.0",
  "org.json4s"          %% "json4s-core" % Json4s,
  "org.json4s"          %% "json4s-jackson" % Json4s,
  "org.json4s"          %% "json4s-native" % Json4s
)

//We do this so that Spark Dependencies will not be bundled with our fat jar but will still be included on the classpath
//When we do a sbt/run
run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))

assemblySettings

mergeStrategy in assembly := {
  case PathList("META-INF", "ECLIPSEF.RSA", xs @ _*)         => MergeStrategy.discard
  case PathList("META-INF", "mailcap", xs @ _*)         => MergeStrategy.discard
  case PathList("org", "apache","commons","collections", xs @ _*) => MergeStrategy.first
  case PathList("org", "apache","commons","logging", xs @ _*) => MergeStrategy.first
  case PathList(ps @ _*) if ps.last == "Driver.properties" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last == "plugin.properties" => MergeStrategy.discard
  case PathList(ps @ _*) if ps.last == "log4j.properties" => MergeStrategy.first
  case x =>
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("sensorDemo.KafkaConsumer")
