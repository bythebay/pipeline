package bythebay

import akka.actor.{Props, Actor, ActorLogging}
import kafka.producer.KeyedMessage

import scala.concurrent.duration.Duration
import scala.concurrent.duration._

/**
 * This keeps the file handle open and just reads on line at fixed time ticks.
 * Not the most efficient implementation, but it is the easiest.
 */
class FeederActor extends Actor with ActorLogging with FeederExtensionActor {

  import bythebay.FeederActor.SendNextLine

  var counter = 0

  implicit val executionContext = context.system.dispatcher

  val feederTick = context.system.scheduler.schedule(Duration.Zero, 100.millis, self, SendNextLine)

  var ratingsDataSet:Iterator[String] = initRatingsFile()

  def receive = {
    case SendNextLine if ratingsDataSet.hasNext =>
      val nxtRating = ratingsDataSet.next()
      log.info(s"Sending next rating: $nxtRating")
      feederExtension.producer.send(new KeyedMessage[String, String](feederExtension.kafkaTopic, nxtRating))

  }

  def initRatingsFile() = {
    val source = scala.io.Source.fromFile(feederExtension.dataDirectory + "/ratings.dat")
    source.getLines()
  }
}

object FeederActor {
	val props = Props[FeederActor]
	case object ShutDown
	case object SendNextLine

}
