/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.typesafe.training.akkacollect

import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, Address, Props, RootActorPath}

object PlayerRegistry {

  case class RegisterPlayer(name: String, props: Props)

  case class PlayerNameTaken(name: String)

  case class PlayerRegistered(name: String)

  case object GetPlayers

  case class Players(players: Set[String])

  val name: String =
    "player-registry"

  def props: Props =
    Props(new PlayerRegistry)

  def pathFor(address: Address): ActorPath = {
    RootActorPath(address) / "user" / name
  }
}

class PlayerRegistry extends Actor with ActorLogging {

  import PlayerRegistry._

  var players: collection.mutable.Set[String] = collection.mutable.Set()
  val playerSharding = PlayerSharding(context.system)

  override def receive: Receive = {
    case RegisterPlayer(name, _) if isNameTaken(name) => playerNameTaken(name: String)
    case RegisterPlayer(name, props)                  => registerPlayer(name, props)
    case GetPlayers                                   => sender() ! Players(players.toSet)
  }

  private def playerNameTaken(name: String): Unit = {
    log.warning("Player name {} taken", name)
    sender() ! PlayerNameTaken(name)
  }

  private def registerPlayer(name: String, props: Props): Unit = {
    log.info("Registering player {}", name)
    createPlayer(name, props)
    sender() ! PlayerRegistered(name)
  }

  protected def createPlayer(name: String, props: Props): Unit  = {
    players.add(name)
    playerSharding.tellPlayer(name, PlayerSharding.Player.Initialize(props))
  }

  private def isNameTaken(name: String): Boolean =
    players.contains(name)

}
