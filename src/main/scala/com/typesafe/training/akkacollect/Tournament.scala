/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.typesafe.training.akkacollect

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, Terminated }

object Tournament {

  def props(playerRegistry: ActorRef, scoresRepository: ActorRef, maxPlayerCountPerGame: Int): Props =
    Props(new Tournament(playerRegistry, scoresRepository, maxPlayerCountPerGame))

  def partitionPlayers(players: Set[ActorRef], maxPlayerCountPerGame: Int): Iterator[Set[ActorRef]] = {
    val remainder = players.size % maxPlayerCountPerGame
    val partitions =
      if (remainder == 0)
        players.sliding(maxPlayerCountPerGame, maxPlayerCountPerGame)
      else {
        val count = players.size / maxPlayerCountPerGame + 1
        val normalSize = players.size / count
        val largeCount = players.size - (count * normalSize)
        val largePartitions = players.sliding(normalSize + 1, normalSize + 1) take largeCount
        val normalPartitions = (players drop ((normalSize + 1) * largeCount)).sliding(normalSize, normalSize)
        largePartitions ++ normalPartitions
      }
    partitions map (_.toSet)
  }
}

class Tournament(playerRegistry: ActorRef, scoresRepository: ActorRef, maxPlayerCountPerGame: Int)
    extends Actor with SettingsActor with ActorLogging {

  import Tournament._

  private var games = Set.empty[ActorRef]

  private var scores = Map.empty[String, Long]

  override def preStart(): Unit =
    playerRegistry ! PlayerRegistry.GetPlayers

  override def receive: Receive =
    waiting

  private def waiting: Receive = {
    case PlayerRegistry.Players(players) => onPlayers(players)
  }

  private def becomeRunning(players: Set[ActorRef]): Unit = {
    log.info("Starting games")
    val g = for (players <- partitionPlayers(players, maxPlayerCountPerGame)) yield createGame(players)
    for (actorRef <- g) context.watch(actorRef)
    games = games ++ g.toSet
    context become running
  }

  private def running: Receive = {
    case Game.GameOver(gameScores) => scores ++= gameScores
    case Terminated(game)          => onGameTerminated(game)
  }

  private def onPlayers(players: Set[ActorRef]): Unit =
    if (players.isEmpty) {
      log.info("No players, no games")
      context.stop(self)
    } else
      becomeRunning(players)

  private def onGameTerminated(game: ActorRef): Unit = {
    games -= game
    if (games.isEmpty) {
      log.info("Tournament over with scores: {}", scores mkString ", ")
      scoresRepository ! ScoresRepository.UpdateScores(scores)
      context.stop(self)
    }
  }

  protected def createGame(players: Set[ActorRef]): ActorRef = {
    import settings.game._
    context.actorOf(Game.props(players, moveCount, moveTimeout, sparseness))
  }
}
