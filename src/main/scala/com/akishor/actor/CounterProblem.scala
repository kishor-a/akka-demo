package com.akishor.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}


object CounterProblem extends App{

  import com.akishor.actor.CounterProblem.CounterActor._
  val system = ActorSystem("exercise")

  // 1.
  class CounterActor extends Actor {

    var counter = 0
    override def receive: Receive = {
      case Increment => println("incrementing"); counter+=1
      case Decrement => println("decrementing"); counter-=1
      case Print => println(s"Counter nos is $counter")
    }
  }
  object CounterActor{
    case object Increment;
    case object Decrement;
    case object Print;
  }

  val counterActor = system.actorOf(Props[CounterActor], "counterActor")

  counterActor ! Increment //or case class Increment
  counterActor ! Decrement
  counterActor ! Increment
  counterActor ! Increment
  counterActor ! Print


}
