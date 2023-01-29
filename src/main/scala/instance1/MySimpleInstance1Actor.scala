package instance1

import akka.actor.{Actor, ActorLogging}

class MySimpleInstance1Actor extends Actor with ActorLogging {
  override def receive: Receive = {
    case m => log.info(s"Received $m from ${sender()}")
  }
}
