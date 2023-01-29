package instance2

import akka.actor.{Actor, ActorLogging, ActorSystem}
import com.typesafe.config.ConfigFactory

class MySimpleInstance2Actor extends Actor with ActorLogging {
  override def receive: Receive = {
    case message: Int => {
      log.info(s"Received $message from ${sender()} for another JVM")
      val localSystem = ActorSystem("LocalSystem", ConfigFactory.load("instance2/instance2ActorConf"))
      val ref = localSystem.actorSelection("akka://LocalSystem@localhost:2551/user/insta1")
      ref ! message
    }
    case m => log.info(s"Received $m from ${sender()}")
  }
}
