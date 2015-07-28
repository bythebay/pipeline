import sbtassembly.Plugin.AssemblyKeys._

name := "RaitingDataAnalyzer"

version := "0.2.0"

scalaVersion := "2.10.5"

val Spark = "1.4.0"
val SparkCassandra = "1.4.0-M1"
val Kafka = "0.8.1.1"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  ("org.apache.spark" %% "spark-core" % Spark % "provided").
    exclude("com.google.guava", "guava"),
  ("org.apache.spark" %% "spark-streaming" % Spark % "provided").
    exclude("com.google.guava", "guava"),
  ("com.datastax.spark" %% "spark-cassandra-connector" % SparkCassandra withSources() withJavadoc()).
    exclude("com.esotericsoftware.minlog", "minlog").
    exclude("commons-beanutils", "commons-beanutils").
    exclude("org.apache.spark", "spark-core"),
  ("com.datastax.spark" %% "spark-cassandra-connector-java" % SparkCassandra withSources() withJavadoc()).
    exclude("org.apache.spark", "spark-core"),
  ("org.apache.kafka" %% "kafka" % Kafka).
    exclude("org.slf4j", "slf4j-simple").
    exclude("com.sun.jmx", "jmxri").
    exclude("com.sun.jdmk", "jmxtools").
    exclude("javax.jms", "jms"),
  "org.apache.spark" %% "spark-streaming-kafka" % Spark
)

//We do this so that Spark Dependencies will not be bundled with our fat jar but will still be included on the classpath
//When we do a sbt/run
run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run))

assemblySettings

mergeStrategy in assembly := {
  case PathList("META-INF", "ECLIPSEF.RSA", xs@_*) => MergeStrategy.discard
  case PathList("META-INF", "mailcap", xs@_*) => MergeStrategy.discard
  case PathList("org", "apache", "commons", "collections", xs@_*) => MergeStrategy.first
  case PathList("org", "apache", "commons", "logging", xs@_*) => MergeStrategy.first
  case PathList("org", "apache", "commons", "logging", xs@_*) => MergeStrategy.first
  case PathList(ps@_*) if ps.last == "Driver.properties" => MergeStrategy.first
  case PathList(ps@_*) if ps.last == "plugin.properties" => MergeStrategy.discard
  case PathList(ps@_*) if ps.last == "log4j.properties" => MergeStrategy.first
  case x =>
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("datingDemo.KafkaConsumer")
