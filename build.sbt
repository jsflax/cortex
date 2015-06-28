name := "cortex"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.2"
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "1.1.4"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.6"
libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"        % "2.2.+",
  "com.h2database"  %  "h2"                 % "1.4.+",
  "ch.qos.logback"  %  "logback-classic"    % "1.1.+"
)