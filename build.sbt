lazy val akkaHttpVersion = "10.2.8"
lazy val akkaVersion = "2.6.19"
lazy val circeVersion = "0.14.1"
lazy val AkkaManagementVersion = "1.1.3"

name := "ClusterSharding"

version := "0.1"

scalaVersion := "2.13.8"

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-unused:imports",
  "-encoding", "UTF-8"
)

lazy val commonJavacOptions = Seq(
  "-Xlint:unchecked",
  "-Xlint:deprecation"
)

lazy val commonSettings = Seq(
  Compile / scalacOptions ++= commonScalacOptions,
  Compile / javacOptions ++= commonJavacOptions,
  run / javaOptions ++= Seq("-Xms128m", "-Xmx1024m"),
  run / fork := true,
  Global / cancelable := false,
  licenses := Seq(
    ("CC0", url("http://creativecommons.org/publicdomain/zero/1.0"))
  )
)

lazy val application = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    mainClass in (Compile, run) := Some("Application"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.2",
      //  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % AkkaManagementVersion,
      "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
      "com.datastax.oss" % "java-driver-core" % "4.13.0", // See https://github.com/akka/alpakka/issues/2556
      "com.typesafe.akka" %% "akka-persistence-cassandra" % "1.0.5",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
      "ch.qos.logback" % "logback-classic" % "1.2.10",
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-persistence-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.9" % Test,
      "io.aeron" % "aeron-driver" % "1.37.0",
      "io.aeron" % "aeron-client" % "1.37.0"
    )
  )