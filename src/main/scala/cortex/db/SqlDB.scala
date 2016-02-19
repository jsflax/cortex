package cortex.db

import java.sql.{SQLException, DriverManager}

import cortex.util.log
import scalikejdbc._

/**
  */
object SqlDB {
  // initialize JDBC driver & connection pool
  Class.forName("org.h2.Driver")
  implicit val session = AutoSession

  var isInitialized = false

  def initialize(dbAddressUsernameAndPassword: (String, String, String, String)) = {
    GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
      enabled = false,
      singleLineMode = false,
      printUnprocessedStackTrace = false,
      stackTraceDepth = 15,
      logLevel = 'error,
      warningEnabled = false,
      warningThresholdMillis = 3000L,
      warningLogLevel = 'warn
    )
    ConnectionPool.singleton(
      s"jdbc:mysql://${dbAddressUsernameAndPassword._1}/${dbAddressUsernameAndPassword._2}",
      dbAddressUsernameAndPassword._3,
      dbAddressUsernameAndPassword._4
    )

    try {
      val connection = DriverManager.getConnection(
        s"jdbc:mysql://${dbAddressUsernameAndPassword._1}",
          dbAddressUsernameAndPassword._3,
          dbAddressUsernameAndPassword._4
      )
      connection.prepareStatement(
        s"CREATE DATABASE IF NOT EXISTS ${dbAddressUsernameAndPassword._2}"
      ).executeUpdate()
      connection.prepareStatement(
        s"use ${dbAddressUsernameAndPassword._2}"
      ).execute()

      isInitialized = true
    } catch {
      case e: SQLException => log e s"Failed to initialize SqlDB: ${e.getMessage}"
    }
  }
}
