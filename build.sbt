name := "cortex"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "io.spray" %%  "spray-json" % "1.3.2",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "org.scalaj" %% "scalaj-http" % "1.1.4",
  "mysql" % "mysql-connector-java" % "5.1.6",
  "org.scalikejdbc" %% "scalikejdbc"        % "2.2.+",
  "com.h2database"  %  "h2"                 % "1.4.+",
  "ch.qos.logback"  %  "logback-classic"    % "1.1.+"
)

parallelExecution in Test := false