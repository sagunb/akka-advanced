/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.typesafe.training.akkacollect

import akka.actor.{ActorRef, ActorSystem, Address, Props}
import akka.pattern.ask

import scala.annotation.tailrec
import scala.io.StdIn

object GameEngineApp extends BaseApp with Terminal {

  override protected val parser: CommandParser.Parser[Command] =
    CommandParser.register | CommandParser.shutdown // REMOVE `registerCommand` is only needed initially

  override def createTop(system: ActorSystem, settings: Settings): ActorRef = {
    import settings.gameEngine._
    val playerRegistry = system.actorOf(PlayerRegistry.props, PlayerRegistry.name)
    system.actorOf(GameEngine.props(tournamentInterval), GameEngine.name)
    playerRegistry // REMOVE This return value to make the player registry handle commands instead of the game engine is only needed initially
  }

  @tailrec
  override protected def commandLoop(system: ActorSystem, settings: Settings, top: ActorRef): Unit = {
    Command(StdIn.readLine()) match {
      case Command.Shutdown =>
        system.terminate()
      case Command.Unknown(command, message) =>
        system.log.warning("Unknown command {} ({})!", command, message)
        commandLoop(system, settings, top)
      case other =>
        system.log.warning("Not responsible for command {}!", other)
        commandLoop(system, settings, top)
    }
  }
}
