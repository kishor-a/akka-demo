package com.akishor.actor

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App{

  //part 1 actor system
  val actorSystem = ActorSystem("aKishorActorSystem")

  println(actorSystem.name)

  //part 2 create actor

  //word count actor
  class WordCountActor extends Actor
  {
    //internal data
    var totalWords=0

    //behaviour
    override def receive: PartialFunction[Any, Unit] = {
      case message: String => {
        println(s"Actor received message: $message")
        totalWords = message.split(" ").length
        println(s"word count of messsage is $totalWords")
      }
      case unknownMessage => println(s"I cannot understand this messsage $unknownMessage")
    }
  }

  //part 3 instantiate actor
  val wordCountActor = actorSystem.actorOf(Props[WordCountActor],"wordCounter");

  //part 4 communicate

  wordCountActor ! "I am learning akka"
  wordCountActor ! "this is another message to actor word count" //tell method
}
