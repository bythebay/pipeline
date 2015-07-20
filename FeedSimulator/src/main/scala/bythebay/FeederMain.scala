package bythebay

import akka.actor.ActorSystem

object FeederMain extends App {
  val system = ActorSystem("MyActorSystem")
  val feederActor = system.actorOf(FeederActor.props, "feederActor")

  println(s"messaging ${feederActor}")
  feederActor ! FeederActor.Initialize

  system.awaitTermination()

}
