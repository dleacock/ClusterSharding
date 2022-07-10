package http

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import thermostat.IotThermostat._

import scala.concurrent.Future
import scala.concurrent.duration._

class Routes(system: ActorSystem[_]) {
  private val sharding = ClusterSharding(system)

  private implicit val timeout: Timeout =
    system
      .settings
      .config
      .getDuration("thermostat.routes.ask-timeout")
      .toMillis
      .millis

  final case class RecordTemperatureRequest(
    deviceId: String,
    temperature: String)

  private def recordTemperature(
    deviceId: DeviceId,
    temperature: Temperature
  ): Future[TemperatureRecorded] = {
    val deviceRef = sharding.entityRefFor(TypeKey, deviceId.value)
    deviceRef.ask(replyTo => RecordTemperature(temperature, replyTo))
  }

  private def queryTemperature(
    deviceId: DeviceId
  ): Future[TemperatureQueryResult] = {
    val deviceRef = sharding.entityRefFor(TypeKey, deviceId.value)
    deviceRef.ask(replyTo => QueryTemperature(replyTo))
  }

  import akka.http.scaladsl.server.Directives._

  val routes: Route =
    pathPrefix("thermostat") {
      path(Segment) { id =>
        get {
          onSuccess(queryTemperature(DeviceId(id))) {
            case TemperatureQueryResult(temperature) =>
              complete(temperature.value)
          }
        } ~
          put {
            entity(as[RecordTemperatureRequest]) { request =>
              onSuccess(
                recordTemperature(
                  DeviceId(request.deviceId),
                  Temperature(request.temperature)
                )
              ) { performed =>
                complete(
                  StatusCodes.Accepted -> s"Temperature ${request.temperature} recorded for ${performed.deviceId}"
                )
              }
            }
          }
      }
    }
}
