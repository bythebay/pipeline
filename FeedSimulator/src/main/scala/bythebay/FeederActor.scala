package bythebay

import akka.actor.{Props, Actor, ActorLogging}
import bythebay.FeederActor.Initialize
import bythebay.FeederActor.SendNextLine
import bythebay.FeederActor.ShutDown
import kafka.producer.KeyedMessage

import scala.concurrent.duration.Duration
import scala.concurrent.duration._

/**
 * This keeps the file handle open and just reads on line at fixed time ticks.
 * Not the most efficient implementation, but it is the easiest.
 */
class FeederActor extends Actor with ActorLogging with FeederExtensionActor{

  var counter = 0

  implicit val executionContext = context.system.dispatcher

  val feederTick = context.system.scheduler.schedule(Duration.Zero, 100.millis, self, SendNextLine)

  var ratingsDataSet:Iterator[String] = Iterator.empty

  def receive = {
  	case Initialize =>
	    println("In FeederActor - starting file feeder")
      initRatingsFile

    case SendNextLine =>
      if (ratingsDataSet.isEmpty)
        initRatingsFile

      val nxtRating = ratingsDataSet.next()
      println(nxtRating)
      feederExtension.producer.send(new KeyedMessage[String, String](feederExtension.kafkaTopic, nxtRating))

  }

  def initRatingsFile() {
    val source = scala.io.Source.fromFile(feederExtension.dataDirectory + "/ratings.dat")
    ratingsDataSet = source.getLines
  }
}

object FeederActor {
	val props = Props[FeederActor]
	case object Initialize
	case object ShutDown
	case object SendNextLine

}
