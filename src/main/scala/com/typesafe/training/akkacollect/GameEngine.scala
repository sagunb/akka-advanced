/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.typesafe.training.akkacollect

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Address, FSM, Props, RootActorPath, Terminated}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import akka.routing.FromConfig

import scala.concurrent.duration.FiniteDuration

object GameEngine {

  sealed trait State

  object State {

    case object Pausing extends State

    case object Running extends State

    case object Waiting extends State
  }

  case class Data(tournament: Option[ActorRef] = None)

  val name: String =
    "game-engine"

  def props(tournamentInterval: FiniteDuration): Props =
    Props(new GameEngine(tournamentInterval))
}

class GameEngine(tournamentInterval: FiniteDuration)
    extends Actor with FSM[GameEngine.State, GameEngine.Data] with SettingsActor with ActorLogging {

  import GameEngine._

  val scoresRepository = context.actorOf(FromConfig.props(Props[ScoresRepository]), "scores-repository-router")

  val playerRegistryProxy = context.system.actorOf(
    ClusterSingletonProxy.props(
      s"/user/player-registry",
      ClusterSingletonProxySettings(context.system).withRole("player-registry")),
    "player-registry-proxy"
  )

  startWith(State.Pausing, Data())

  when(State.Pausing, tournamentInterval) {
    case Event(StateTimeout, data) =>
        val tournament = startTournament()
        goto(State.Running) using Data(Some(tournament))
  }

  when(State.Running) {
    case Event(Terminated(_), data) => goto(State.Pausing) using Data()
  }

  onTransition {
    case _ -> State.Pausing => log.debug("Transitioning into pausing state")
    case _ -> State.Running => log.debug("Transitioning into running state")
  }

  initialize()

  protected def startTournament(): ActorRef = {
    log.info("Starting tournament")
    context.watch(createTournament())
  }

  protected def createTournament(): ActorRef = {
    import settings.tournament._
    context.actorOf(Tournament.props(playerRegistryProxy, scoresRepository, maxPlayerCountPerGame, askTimeout))
  }
}
