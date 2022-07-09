import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.server.Route
import http.{IotThermostatWebServer, Routes}
import thermostat.IotThermostat
import thermostat.IotThermostat.Command

object IotThermostatService {

  def apply(httpPort: Int): Behavior[Command] = Behaviors.setup { context =>
    val system: ActorSystem[Nothing] = context.system
    val routes: Route = new Routes(system).routes

    IotThermostat.initSharding(system)
    IotThermostatWebServer.start(routes, httpPort, system)

    Behaviors.empty
  }
}
