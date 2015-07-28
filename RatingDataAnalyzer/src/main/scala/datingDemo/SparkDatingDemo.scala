package datingDemo

import java.util.concurrent.atomic.AtomicLong

import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.streaming._

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._
import org.apache.spark.streaming.kafka._



object DatingConfig {
  //val sparkMasterHost = "\"localhost\""

  val cassandraHost = "localhost"

  val cassandraKeyspace = "datingdemo"
  val ratingsTable = "ratingstable"

  //val zookeeperHost = "localhost:2181"
  //val kafkaHost = "localhost:9092"

  val kafkaTopic = "ratings"
  val kafkaConsumerGroup = "ratings_group"

}
object StreamConsumer {

  val counter = new AtomicLong()

  def setup() : (SparkContext, StreamingContext, CassandraConnector) = {
    val sparkConf = new SparkConf(true)
      .set("spark.cassandra.connection.host", DatingConfig.cassandraHost)//
      .set("spark.cleaner.ttl", "3600")
      .setMaster("local[2]") // Spark Streaming requires local > 1 to work because it writes multiple copies!  It has to have duplicate locations to write to.
      //.setMaster(s"spark://${SensorConfig.sparkMasterHost}:7077")
      .setAppName(getClass.getSimpleName)

    // Connect to the Spark cluster:
    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, Seconds(1))
  //  ssc.checkpoint()
    val cc = CassandraConnector(sc.getConf)
    createSchema(cc)
    (sc, ssc, cc)
  }

  def parseMessage(msg:String) : Ratings = {
    val msgSplit: Array[String] = msg.split(",")
    Ratings(Integer.parseInt(msgSplit(0)), Integer.parseInt(msgSplit(1)), java.lang.Float.valueOf(msgSplit(2)))
  }

  def createSchema(cc:CassandraConnector) = {
    cc.withSessionDo { session =>
      session.execute(s"CREATE KEYSPACE IF NOT EXISTS ${DatingConfig.cassandraKeyspace} WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };")
      //session.execute(s"DROP TABLE IF EXISTS ${SensorConfig.cassandraKeyspace}.${SensorConfig.accelerationTable};")

      session.execute("CREATE TABLE IF NOT EXISTS " +
        s"${DatingConfig.cassandraKeyspace}.${DatingConfig.ratingsTable} (userid int, profileid int, rating float, PRIMARY KEY (userid, profileid) );")
    }  //UserID:Int,ProfileID:Int,Rating
  }

  def process(ssc : StreamingContext, input : DStream[String]) {

    input.print()
    val parsedRecords = input.map(parseMessage)

    if ( (counter.getAndIncrement() % 100) == 0)
      parsedRecords.print()


    parsedRecords.saveToCassandra(DatingConfig.cassandraKeyspace, DatingConfig.ratingsTable)


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
      DatingConfig.kafkaConsumerGroup,
      Map(DatingConfig.kafkaTopic -> 1)).map(_._2)
    StreamConsumer.process(ssc, input)
  }
}
