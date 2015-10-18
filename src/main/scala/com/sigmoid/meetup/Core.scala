package com.sigmoid.meetup

import akka.actor.{ActorRefFactory, Props, ActorSystem}
import akka.io.IO
import akka.util.Timeout
import com.sigmoid.meetup.services.SparkServiceActor
import spray.can.Http

import scala.concurrent.duration._

/**
 * Created by ajay on 17/10/15.
 */
trait Core {
  /**
   * This is the main actor system that is driving the application
   * @return
   */
  protected implicit def system: ActorSystem
}

trait CoreActors {
  this: Core =>

  /**
   * Creating the actor services to be used
   */
  val sparkManager = system.actorOf(Props[SparkServiceActor], "spark")

}

trait BootedCore extends Core with Api with StaticResources with Configuration {
  implicit def system: ActorSystem = ActorSystem("sigmoid-crm")
  def actorRefFactory: ActorRefFactory = system

  // This is the main handler
  val rootService = system.actorOf(Props(new RoutedHttpService(routes ~ staticResources)))

  implicit val timeout = Timeout((timeoutDelay).seconds)

  // Starting the service
  val boundedFuture = IO(Http) ! Http.Bind(rootService, interface = interface, port = port)

  // Additional tasks to be executed before shutting down the server
  sys.addShutdownHook(system.shutdown())
}

// Defining classes for marshalling the data
case class Station(id: String, name: String, incoming: Option[Int] = None, outgoing: Option[Int] = None,
                   pageRank: Option[Double] = None)
case class Train(id: Int, name: String, source: Station, destination: Station, distance: Int)
case class Stop(id: String, name: String, num: Int, distance: Int)
case class Journey(id: Int, name: String, stops: Seq[Stop])

