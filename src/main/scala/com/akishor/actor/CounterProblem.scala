package com.akishor.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object CounterProblem extends App{

  val system = ActorSystem("exercise")

  // 1.
  class CounterActor extends Actor {
    var counter = 0
    override def receive: Receive = {
      case "INCREMENT" => println("incrementing"); counter+=1
      case "DECREMENT" => println("decrementing"); counter-=1
      case "PRINT" => println(s"Counter nos is $counter")
    }
  }

  val counterActor = system.actorOf(Props[CounterActor], "counterActor")

  counterActor ! "INCREMENT" //or case class Increment
  counterActor ! "DECREMENT"
  counterActor ! "INCREMENT"
  counterActor ! "INCREMENT"
  counterActor ! "PRINT"

  // 2.

  class BankAccount() extends Actor {
    var balance = 90
    override def receive: Receive = {
      case Withdraw(amount, ref) => {

        if(balance<amount){
          ref ! "Failure"
        }else{
          balance-=amount
          ref ! "Success"
        }
      }
      case Deposit(amount) => balance+=amount
      case "Statement" => println(s"Balance is $balance")
      case "Success" => sender ! "Success"
    }
  }
  class Banker extends Actor{
    override def receive: Receive = {
      case "Success" => println("[Banker] Success received")
      case "Failure" => println("Operation failed")
    }
  }

  case class Withdraw(amount: Int, ref: ActorRef)
  case class Deposit(amount: Int)

  val account1 = system.actorOf(Props[BankAccount],"account1")
  val banker = system.actorOf(Props[Banker], "banker1")

  account1 ! Withdraw(100, banker)
  account1 ! Deposit(100)
  account1 ! "Statement"

}
