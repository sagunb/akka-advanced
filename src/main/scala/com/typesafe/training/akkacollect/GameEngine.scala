/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.typesafe.training.akkacollect

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, Address, FSM, Props, RootActorPath, Terminated}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{ClusterDomainEvent, MemberRemoved, MemberUp}

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

  def props(tournamentInterval: FiniteDuration, scoresRepository: ActorRef): Props =
    Props(new GameEngine(tournamentInterval, scoresRepository))
}

class GameEngine(tournamentInterval: FiniteDuration, scoresRepository: ActorRef)
    extends Actor with FSM[GameEngine.State, GameEngine.Data] with SettingsActor with ActorLogging {

  import GameEngine._

  startWith(State.Waiting, Data())

  override def preStart(): Unit = {
    Cluster(context.system).subscribe(self, classOf[ClusterDomainEvent])
  }

  override def postStop(): Unit = {
    Cluster(context.system).unsubscribe(self)
  }

  when(State.Waiting) {
    case Event(MemberUp(member), data) =>
      if (member.roles.contains("player-registry")) {
        goto(State.Pausing)
      } else {
        stay
      }
  }

  when(State.Pausing, tournamentInterval) {
    case Event(StateTimeout, data) =>
      if (getNumPlayerRegistryMembers() >= 1) {
        val tournament = startTournament()
        goto(State.Running) using Data(Some(tournament))
      } else {
        stay
      }

    case Event(MemberRemoved(member, memberStatus), data) =>
      if (getNumPlayerRegistryMembers() == 0) {
        goto(State.Waiting)
      } else {
        stay
      }

    case Event(MemberUp(member), data) =>
      stay
  }

  when(State.Running) {
    case Event(Terminated(_), data) =>
      if (getNumPlayerRegistryMembers() >= 1) {
        goto(State.Pausing) using Data()
      } else {
        goto(State.Waiting) using Data()
      }
  }

  onTransition {
    case _ -> State.Pausing => log.debug("Transitioning into pausing state")
    case _ -> State.Running => log.debug("Transitioning into running state")
  }

  initialize()

  def getNumPlayerRegistryMembers(): Int = {
    Cluster(context.system).state.members.count(member => member.roles.contains("player-registry"))
  }

  private def startTournament(): ActorRef = {
    log.info("Starting tournament")
    context.watch(createTournament())
  }

  private def createPlayerRegistry(): ActorSelection = {
    // TODO : fix this s@@@
    val playerRegistry = Cluster(context.system).state.members.find(member => member.roles.contains("player-registry")).get
    val path = PlayerRegistry.pathFor(playerRegistry.address)
    context actorSelection path
  }

  protected def createTournament(): ActorRef = {
    import settings.tournament._
    context.actorOf(Tournament.props(createPlayerRegistry(), scoresRepository, maxPlayerCountPerGame, askTimeout))
  }
}
