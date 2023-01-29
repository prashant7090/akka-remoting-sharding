package sharding

import java.util.{Date, UUID}

import scala.util.Random
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, ReceiveTimeout}
import akka.cluster.sharding.ShardRegion.Passivate
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import com.typesafe.config.ConfigFactory


import scala.concurrent.duration._
import scala.collection.immutable

//Domain Classes
case class Message(id: Int, text: String)
case class MessageReceiver(msg: Message, date: Date)

object MessageSender {
  def props(sender: ActorRef) = Props(new MessageSender(sender))
}

class MessageSender(actor: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case receivedMessage: Message => {
      println("************")
      actor ! MessageReceiver(receivedMessage, new Date)
    }
    case _ => println("Received unknown message")
  }
}

class LBUsingSharding extends Actor with ActorLogging  {
  override def preStart(): Unit = {
    super.preStart()
    log.info("Starting  sharding main actor")
    context.setReceiveTimeout(10 seconds)
  }
  override def receive: Receive = {
    case MessageReceiver(msg @ Message(id, text), _) =>
      log.info(s"Received message $msg")
  }
}

object MessageSenderSettings {

  val numberOfShards = 10
  val numberOfEntities = 100

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case attempt @ MessageReceiver(Message(id, _), _) =>
      val entityId = id.hashCode.abs % numberOfEntities
      (entityId.toString, attempt)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case MessageReceiver(Message(cardId, _), _) =>
      val shardId = cardId.hashCode.abs % numberOfShards
      shardId.toString
    case ShardRegion.StartEntity(entityId) =>
      (entityId.toLong % numberOfShards).toString
  }
}

class Node(port: Int, totalNodes: Int) extends App {
  val config = ConfigFactory.parseString(
    s"""
       |akka.remote.artery.canonical.port = $port
     """.stripMargin)
    .withFallback(ConfigFactory.load("instance1/clusterSharding.conf"))

  val system = ActorSystem("MyCluster", config)

  // Setting up Cluster Sharding
  val messageSenderShardRegionRef: ActorRef = ClusterSharding(system).start(
    typeName = "LBUsingSharding",
    entityProps = Props[LBUsingSharding],
    settings = ClusterShardingSettings(system).withRememberEntities(true),
    extractEntityId = MessageSenderSettings.extractEntityId,
    extractShardId = MessageSenderSettings.extractShardId
  )

  val messageSender: immutable.Seq[ActorRef] = (1 to totalNodes).map(_ => system.actorOf(MessageSender.props(messageSenderShardRegionRef)))

  Thread.sleep(10000)

  scala.io.Source.stdin.getLines().foreach { line =>
    val accountNo = line.toInt
    val randomMessageSenderIndex = Random.nextInt(totalNodes)
    val randomMessageSender = messageSender(randomMessageSenderIndex)
    println(s"Sending message to $randomMessageSender")
    randomMessageSender ! Message(accountNo, s"messageFrom${accountNo}")
  }

}


object node11 extends Node(2551, 5)
object node21 extends Node(2561, 5)
