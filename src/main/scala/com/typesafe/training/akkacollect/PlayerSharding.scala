package com.typesafe.training.akkacollect

import akka.actor.{ Actor, ActorLogging, ActorRef, ExtendedActorSystem, Extension, ExtensionKey, Props }
import akka.cluster.sharding.{ClusterShardingSettings, ClusterSharding, ShardRegion}

object PlayerSharding extends ExtensionKey[PlayerSharding] {

  object Player {

    case class Initialize(props: Props)

    case class Envelope(name: String, payload: Any)

    val typeName: String =
      "player"

    val extractEntityId: ShardRegion.ExtractEntityId = {
      case Player.Envelope(name, payload) => (name, payload)
    }

    def extractShardId(shardCount: Int): ShardRegion.ExtractShardId= {
      case Player.Envelope(name, _) => name.hashCode % shardCount toString
    }

    def props: Props =
      Props(new Player)
  }

  class Player extends Actor with ActorLogging {

    import Player._

    override def receive: Receive = {
      case Initialize(props) => becomeInitialized(props)
    }

    private def becomeInitialized(props: Props): Unit = {
      log.info("Initializing player with name {}", self.path.name)
      val player = context.actorOf(props, self.path.name)
      context become PartialFunction(player.forward)
    }
  }
}

class PlayerSharding(system: ExtendedActorSystem) extends Extension {

  import PlayerSharding._

  private val shardCount = Settings(system).playerRegistry.shardCount

  def start(): Unit =
    ClusterSharding(system).start(
      Player.typeName,
      Player.props,
      ClusterShardingSettings(system).withRole("player-registry"),
      Player.extractEntityId,
      Player.extractShardId(shardCount))

  def startProxy(): Unit =
    ClusterSharding(system).startProxy(
      Player.typeName,
      Some("player-registry"), // role
      Player.extractEntityId,
      Player.extractShardId(shardCount)
    )

  def tellPlayer(name: String, message: Any)(implicit sender: ActorRef = ActorRef.noSender): Unit =
    shardRegion ! Player.Envelope(name, message)

  private def shardRegion: ActorRef =
    ClusterSharding(system).shardRegion(Player.typeName)
}
