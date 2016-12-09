package com.typesafe.training.akkacollect

import akka.actor.{Props, Address}
import akka.testkit.ImplicitSender

class PlayerRegistrySpec extends BaseAkkaSpec with ImplicitSender {

  "The player registry companion" should {

    "create an an actor path given an address" in {
      val address = Address("akka.tcp", "system", "example.com", 1899)
      val path = PlayerRegistry.pathFor(address)
      path.address shouldEqual address
      path.name shouldEqual PlayerRegistry.name
      path.parent.name shouldEqual "user"
    }

  }

}