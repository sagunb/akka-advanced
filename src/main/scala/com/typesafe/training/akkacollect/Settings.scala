/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.typesafe.training.akkacollect

import akka.actor.{ Actor, ExtendedActorSystem, Extension, ExtensionKey }
import akka.util.Timeout
import scala.concurrent.duration.{ Duration, FiniteDuration, MILLISECONDS => Millis }

object Settings extends ExtensionKey[Settings]

class Settings(system: ExtendedActorSystem) extends Extension {

  object app {

    implicit val askTimeout: Timeout =
      Duration(akkollect.getDuration("app.ask-timeout", Millis), Millis)
  }

  object game {

    val moveCount: Int =
      akkollect getInt "game.move-count"

    val moveTimeout: FiniteDuration =
      Duration(akkollect.getDuration("game.move-timeout", Millis), Millis)

    val sparseness: Int =
      akkollect getInt "game.sparseness"
  }

  object gameEngine {

    implicit val askTimeout: Timeout = app.askTimeout

    val tournamentInterval: FiniteDuration =
      Duration(akkollect.getDuration("game-engine.tournament-interval", Millis), Millis)

    val hostName = akkollect getString "game-engine.player-registry.hostname"
    val port = akkollect getInt "game-engine.player-registry.port"
    val playerRegistryAddress = s"$hostName:$port"
  }

  object tournament {

    val maxPlayerCountPerGame: Int =
      akkollect getInt "tournament.max-player-count-per-game"
  }

  private val akkollect = system.settings.config getConfig "akkollect"
}

trait SettingsActor {
  this: Actor =>

  val settings: Settings =
    Settings(context.system)
}
