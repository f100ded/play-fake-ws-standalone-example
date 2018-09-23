name := "play-fake-ws-standalone-example"

organization := "org.f100ded.play"

scalaVersion := Versions.scala

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ahc-ws-standalone" % Versions.playWsStandalone,
  "com.typesafe.play" %% "play-ws-standalone-json" % Versions.playWsStandalone,
  "org.f100ded.play" %% "play-fake-ws-standalone" % Versions.playWsFakeStandalone % Test
)
