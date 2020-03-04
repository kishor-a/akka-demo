package com.akishor.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App{

  class SimpleActor extends Actor{
    override def receive: Receive = {
      case "Hi" => sender ! "Hello There"
      case message: String => println(s"Actor - $self, Message received: $message")
      case number: Int => println(s"Number received $number")
      case SpecialMessage(content) => println(s"Simple message received with content $content")
      case SendMessageToSelf(content) => self ! content
      case SayHiTo(ref) => ref ! "Hi"
      case WirelessMessage(message, ref) => ref forward  message+"s"
    }
  }

  val actorCapabilitiesActorSystem = ActorSystem("actor-capabilities-actor-system");
  val simpleActor = actorCapabilitiesActorSystem.actorOf(Props[SimpleActor])

  simpleActor ! "See actor system capabilities"


  simpleActor ! 21

  case class SpecialMessage(content: String)

  simpleActor ! SpecialMessage("Special message content")

  // 1. message can be of any type
  // Message must be IMMUTABLE
  // message must be SERIALIZABLE
  // in practice use case classes and case objects

  // 2. Actors have information about context and about themselves
  // context.self === `this` in OOP

  case class SendMessageToSelf(message: String)

  simpleActor ! SendMessageToSelf("I am an actor and I am proud of it")

  // 3. Actors can reply to messages

  val alice = actorCapabilitiesActorSystem.actorOf(Props[SimpleActor],"alice")
  val bob = actorCapabilitiesActorSystem.actorOf(Props[SimpleActor],"bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  // 4. Dead letters
  alice ! "Hi"

  // 5. Forward message

  case class WirelessMessage(content: String, ref: ActorRef)

  alice ! WirelessMessage("Hi", bob)
}
