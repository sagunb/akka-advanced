/**
  * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
  */

package com.typesafe.training.akkacollect

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Props, Status, Terminated}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.typesafe.training.akkacollect.ScoresRepository.ScoresUpdated

import scala.concurrent.duration._

object Tournament {

  def props(playerRegistry: ActorRef, scoresRepository: ActorRef, maxPlayerCountPerGame: Int, askTimeout: Timeout): Props =
    Props(new Tournament(playerRegistry, scoresRepository, maxPlayerCountPerGame)(askTimeout))

  def partitionPlayers(players: Set[String], maxPlayerCountPerGame: Int): Iterator[Set[String]] = {
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

class Tournament(playerRegistry: ActorRef, scoresRepository: ActorRef, maxPlayerCountPerGame: Int)(implicit askTimeout: Timeout)
  extends Actor with SettingsActor with ActorLogging {

  import Tournament._
  import context.dispatcher
  implicit val timeout: Duration = 5 seconds

  private var games = Set.empty[ActorRef]

  private var scores = Map.empty[String, Long]

  override def preStart(): Unit =
    playerRegistry ? PlayerRegistry.GetPlayers pipeTo self

  override def receive: Receive =
    waiting

  private def waiting: Receive = {
    case PlayerRegistry.Players(players) => onPlayers(players)
    case Status.Failure(_)               => onPlayersAskTimeout()
  }

  private def becomeRunning(players: Set[String]): Unit = {
    log.info("Starting games")
    for (players <- partitionPlayers(players, maxPlayerCountPerGame))
      games += context.watch(createGame(players))
    context become running
  }

  private def running: Receive = {
    case Game.GameOver(gameScores) =>
      scores ++= gameScores
      log.info("Tournament over with scores: {}", scores mkString ", ")
    case Terminated(game)          => onGameTerminated(game)
    case Failure =>
      log.info("No answer form scores repository, scores might have been lost! at error")
      context.stop(self)
    case ScoresUpdated => context.stop(self)
  }

  private def onPlayers(players: Set[String]): Unit =
    if (players.isEmpty) {
      log.info("No players, no games")
      context.stop(self)
    } else
      becomeRunning(players)

  private def onPlayersAskTimeout() = {
    log.error("No answer from player registry, no games!")
    context.stop(self)
  }

  private def onGameTerminated(game: ActorRef): Unit = {
    games -= game
    if (games.isEmpty) {
      val status = scoresRepository ? ScoresRepository.UpdateScores(scores)
      status pipeTo self
    }
  }

  protected def createGame(players: Set[String]): ActorRef = {
    import settings.game._
    context.actorOf(Game.props(players, moveCount, moveTimeout, sparseness))
  }
}
