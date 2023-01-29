package instance1

import akka.actor.{ ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory


object Instance1MainActor  {

  def main(args: Array[String]): Unit = {
    val localSystem = ActorSystem("LocalSystem", ConfigFactory.load("instance1/instance1ActorConf"))

    val instance1Actors: List[ActorRef] = Range(1,3).toList.map { i =>
      localSystem.actorOf(Props[MySimpleInstance1Actor], s"insta${i}")
    }

    instance1Actors.foreach{
      actorRef =>
        actorRef ! "Hello from instance1 actor"
    }

  }

}
