import NativePackagerKeys._

packageArchetype.java_application

name := """scala-heroku-docker"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-httpx" % "6.26.0",
  "postgresql" % "postgresql" % "9.0-801.jdbc4",
  "com.heroku.sdk" % "heroku-jdbc" % "0.1.1"
)
