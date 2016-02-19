name := "cortex"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

organization      := "com.github.jsflax"

publishMavenStyle := true

libraryDependencies ++= Seq(
  "io.spray" %%  "spray-json" % "1.3.2",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "org.scalaj" %% "scalaj-http" % "1.1.4",
  "mysql" % "mysql-connector-java" % "5.1.6",
  "org.scalikejdbc" %% "scalikejdbc"       % "2.3.5",
  "com.h2database"  %  "h2"                % "1.4.191",
  "ch.qos.logback"  %  "logback-classic"   % "1.1.3"
)

parallelExecution in Test := false