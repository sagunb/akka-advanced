/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.typesafe.training.akkacollect

import akka.actor.{ Actor, ActorLogging, ActorRef, FSM, Props, Terminated }
import scala.concurrent.duration.FiniteDuration

object GameEngine {

  sealed trait State

  object State {

    case object Pausing extends State

    case object Running extends State
  }

  case class Data(tournament: Option[ActorRef] = None)

  val name: String =
    "game-engine"

  def props(tournamentInterval: FiniteDuration, playerRegistry: ActorRef, scoresRepository: ActorRef): Props =
    Props(new GameEngine(tournamentInterval, playerRegistry, scoresRepository))
}

class GameEngine(tournamentInterval: FiniteDuration, playerRegistry: ActorRef, scoresRepository: ActorRef)
    extends Actor with FSM[GameEngine.State, GameEngine.Data] with SettingsActor with ActorLogging {

  import GameEngine._

  startWith(State.Pausing, Data())

  when(State.Pausing, tournamentInterval) {
    case Event(StateTimeout, data) =>
      val tournament = startTournament()
      goto(State.Running) using Data(Some(tournament))
  }

  when(State.Running) {
    case Event(Terminated(_), data) =>
      goto(State.Pausing) using data.copy(tournament = None)
  }

  onTransition {
    case _ -> State.Pausing => log.debug("Transitioning into pausing state")
    case _ -> State.Running => log.debug("Transitioning into running state")
  }

  initialize()

  private def startTournament(): ActorRef = {
    log.info("Starting tournament")
    context.watch(createTournament())
  }

  protected def createTournament(): ActorRef = {
    import settings.tournament._
    context.actorOf(Tournament.props(playerRegistry, scoresRepository, maxPlayerCountPerGame))
  }
}
