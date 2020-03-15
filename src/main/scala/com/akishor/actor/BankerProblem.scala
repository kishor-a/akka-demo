package com.akishor.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object BankerProblem extends App {

  val system = ActorSystem("exercise")

  // 2.
  //Actor 1
  class BankAccount() extends Actor {
    import BankAccount._
    var balance = 90
    override def receive: Receive = {
      case Withdraw(amount) => {

        if(balance<amount){
          sender() ! Failure
        }else{
          balance-=amount
          sender() ! Success
        }
      }
      case Deposit(amount) => {
        balance+=amount
        sender() ! Success
      }
      case Statement => println(s"Balance is $balance")

    }
  }
  object BankAccount {

    case class Withdraw(amount: Int)
    case class Deposit(amount: Int)
    case object Statement

    case object Success
    case object Failure
  }

  //Actor 2
  class Banker extends Actor{
    import Banker._
    import BankAccount._
    override def receive: Receive = {

      case TransactFor(account) => {
        account ! Withdraw(100)
        account ! Deposit(20)
        account ! Withdraw(1000)
        account ! Statement
      }
      case message => println(message)
    }

  }

  object Banker {
    case class TransactFor(ref: ActorRef)
  }


  val account1 = system.actorOf(Props[BankAccount],"account1")
  val banker = system.actorOf(Props[Banker], "banker1")

  import Banker.TransactFor

  banker ! TransactFor(account1)


}
