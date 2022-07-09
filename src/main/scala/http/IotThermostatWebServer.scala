package http

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.{actor => classic}

import scala.util.{Failure, Success}

object IotThermostatWebServer {

  def start(routes: Route, port: Int, system: ActorSystem[_]): Unit = {
    import akka.actor.typed.scaladsl.adapter._
    implicit val classicSystem: classic.ActorSystem = system.toClassic
    import system.executionContext

    Http()
      .newServerAt("localhost", port)
      .bind(routes)
      .onComplete {
        case Success(binding) =>
          system
            .log
            .info(
              s"\u001B[34m **** IotThermostat WebServer online at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort} **** \u001B[0m"
            )
        case Failure(exception) =>
          system.log.error(s"Failed to bind HTTP server $exception")
          system.terminate()
      }
  }
}
