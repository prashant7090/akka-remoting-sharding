package instance2

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory


object Instance2MainActor  {

  def main(args: Array[String]): Unit = {
    val localSystem = ActorSystem("LocalSystem", ConfigFactory.load("instance2/instance2ActorConf"))

    val instance2Actors: List[ActorRef] = Range(5,8).toList.map { i =>
      localSystem.actorOf(Props[MySimpleInstance2Actor], s"insta${i}")
    }

    instance2Actors.foreach{
      actorRef =>
        actorRef ! "Hello from instance2 actor"
    }
    instance2Actors.head ! 1
  }

}
