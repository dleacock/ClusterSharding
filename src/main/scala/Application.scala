import akka.actor.AddressFromURIString
import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import thermostat.IotThermostat.Command

import scala.collection.JavaConverters._
import scala.collection.mutable

object Application {

  def main(args: Array[String]): Unit = {
    val seedNodePorts: mutable.Seq[Int] = ConfigFactory
      .load()
      .getStringList("akka.cluster.seed-nodes")
      .asScala
      .flatMap { case AddressFromURIString(s) =>
        s.port
      }

    val ports: collection.Seq[Int] = args.headOption match {
      case Some(port) => Seq(port.toInt)
      case None       => seedNodePorts ++ Seq(0)
    }

    ports foreach { port =>
      val httpPort = if (port > 0) port + 10000 else 0
      val config = ConfigFactory
        .parseString(s"""
       akka.remote.artery.canonical.port = $port
        """)
        .withFallback(ConfigFactory.load())
      ActorSystem[Command](IotThermostatService(httpPort), "IotThermostatService", config)
    }
  }
}