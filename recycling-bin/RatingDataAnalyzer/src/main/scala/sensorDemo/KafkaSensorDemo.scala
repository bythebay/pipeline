package sensorDemo

import java.util.Date
import java.util.concurrent.atomic.AtomicLong

import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.streaming._

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._
import org.apache.spark.streaming.kafka._

import org.apache.spark.streaming.dstream.PairDStreamFunctions

import org.json4s._
import org.json4s.native.JsonParser

object SensorConfig {
  //val sparkMasterHost = "local"
  //val cassandraHost = "52.8.63.225"

  val cassandraKeyspace = "sensors"
  val accelerationTable = "acceleration"

 // val zookeeperHost = "localhost:2181"
 // val kafkaHost = "localhost:9092"
  val kafkaTopic = "acc_data"
  val kafkaConsumerGroup = "sensor_group"

}
object StreamConsumer {

  val counter = new AtomicLong()

  def setup() : (SparkContext, StreamingContext, CassandraConnector) = {
    val sparkConf = new SparkConf(true)
     // .set("spark.cassandra.connection.host", SensorConfig.cassandraHost)//
      .set("spark.cleaner.ttl", "3600")
      .setMaster("local[2]") // Spark Streaming requires local > 1 to work because it writes multiple copies!  It has to have duplicate locations to write to.
      //.setMaster(s"spark://${SensorConfig.sparkMasterHost}:7077")
      .setAppName(getClass.getSimpleName)

    // Connect to the Spark cluster:
    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, Seconds(1))
    ssc.checkpoint()
    val cc = CassandraConnector(sc.getConf)
    createSchema(cc)
    (sc, ssc, cc)
  }

  implicit val formats = DefaultFormats

  def parseDate(str:String) : Date = {
    javax.xml.bind.DatatypeConverter.parseDateTime(str).getTime
  }

  def minuteBucket(d:Date) : Long = {
    d.getTime / (60 * 1000)
  }

  def parseMessage(msg:String) : Acceleration = {
    JsonParser.parse(msg).extract[Acceleration]
  }

  def createSchema(cc:CassandraConnector) = {
    cc.withSessionDo { session =>
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS ${SensorConfig.cassandraKeyspace} WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2 };")
      //session.execute(s"DROP TABLE IF EXISTS ${SensorConfig.cassandraKeyspace}.${SensorConfig.accelerationTable};")

      session.execute("CREATE TABLE IF NOT EXISTS " +
        s"${SensorConfig.cassandraKeyspace}.${SensorConfig.accelerationTable} (userid text, timestamp bigint, x float, y float, z float, notes text, " +
        s"PRIMARY KEY (userid, timestamp) );")
    }
  }

  def process(ssc : StreamingContext, input : DStream[String]) {

    input.print()
    val parsedRecords = input.map(parseMessage)

    if ( (counter.getAndIncrement() % 1000) == 0)
      parsedRecords.print()


    parsedRecords.saveToCassandra(SensorConfig.cassandraKeyspace, SensorConfig.accelerationTable)


    sys.ShutdownHookThread {
      ssc.stop(stopSparkContext = true, stopGracefully = true)
    }

    ssc.start()
    ssc.awaitTermination()
  }
}

object KafkaConsumer {
  def main(args: Array[String]) {

    val zookeeperHost = System.getProperty("zookeeper.host","localhost:2181")


    val (sc, ssc, cc) = StreamConsumer.setup()
    val input = KafkaUtils.createStream(
      ssc,
      zookeeperHost,
      SensorConfig.kafkaConsumerGroup,
      Map(SensorConfig.kafkaTopic -> 1)).map(_._2)
    StreamConsumer.process(ssc, input)
  }
}
