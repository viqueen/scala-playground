package com.viqueen.playground

import akka.actor._

object ThreeNplusOne extends App {
  val system = ActorSystem("threeNPlusOne")
  val evenHandler  = system.actorOf(Props[EvenHandler])
  val oddHandler = system.actorOf(Props[OddHandler])

  val master = system.actorOf(Props(new Master(evenHandler, oddHandler)))

  master ! 9
}

class Master(even : ActorRef, odd : ActorRef) extends Actor {

  var cycle = -1

  override def receive: Actor.Receive = {
    case number : Int if number == 1 =>
      println(s"cycle: $cycle")
      context.system.terminate()

    case number : Int if number % 2 == 0 =>
      cycle += 1
      even ! number

    case number : Int if number % 2 != 0 =>
      cycle += 1
      odd ! number
  }
}

class EvenHandler extends Actor {
  override def receive: Actor.Receive = {
    case even : Int => sender ! even / 2 
  }
}

class OddHandler extends Actor {
  override def receive: Actor.Receive = {
    case odd : Int => sender ! (odd * 3) + 1
  }
}