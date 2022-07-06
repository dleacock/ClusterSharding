import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, PostStop}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey

object IotThermostat {

  // TODO move this elsewhere and make this class not need to know about cluster sharding
  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("IotThermostat")

  sealed trait Command extends CborSerializable
  final case class RecordTemperature(
    temperature: Temperature,
    replyTo: ActorRef[TemperatureRecorded])
      extends Command
  final case class TemperatureRecorded(deviceId: DeviceId)

  final case class QueryTemperature(replyTo: ActorRef[TemperatureQueryResult])
      extends Command
  final case class TemperatureQueryResult(temperature: Temperature)
      extends CborSerializable

  final case class Temperature(value: String)
  final case class DeviceId(value: String)

  private def run(
    context: ActorContext[Command],
    deviceId: DeviceId,
    temperature: Temperature
  ): Behavior[Command] = {
    val log = context.log
    Behaviors
      .receiveMessage[Command] {
        case RecordTemperature(temperature, replyTo) => {
          log.info(s"IoTTemperature Device $deviceId at $temperature degrees")
          replyTo ! TemperatureRecorded(deviceId)
          run(context, deviceId, temperature)
        }
        case QueryTemperature(replyTo) =>
          replyTo ! TemperatureQueryResult(temperature)
          run(context, deviceId, temperature)
      }
      .receiveSignal { case (_, PostStop) =>
        log.info(s"Stopping, losing temperature for device $deviceId")
        Behaviors.same
      }
  }

  def apply(
    deviceId: DeviceId,
    initialTemperature: Temperature
  ): Behavior[Command] = Behaviors.setup { context =>
    context
      .log
      .info(
        s"Starting IotThermostat $deviceId - currently at $initialTemperature"
      )
    run(context, deviceId, initialTemperature)
  }
}
