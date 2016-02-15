package cortex.db

import java.sql.{SQLException, DriverManager}

import cortex.util.log
import scalikejdbc._
import scalikejdbc.interpolation.Implicits

/**
 */
object SqlDB extends Implicits {
  // initialize JDBC driver & connection pool
  Class.forName("org.h2.Driver")
  implicit val session = AutoSession

  var isInitialized = false

  def initialize(dbAddressUsernameAndPassword: (String, String, String)) = {
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
        s"jdbc:mysql://${dbAddressUsernameAndPassword._1}",
        dbAddressUsernameAndPassword._2,
        dbAddressUsernameAndPassword._3
      )

    try {
      DriverManager.getConnection(
        s"jdbc:mysql://${dbAddressUsernameAndPassword._1}"
      )
      isInitialized = true
    } catch {
      case e: SQLException => log e "Failed to initialize SqlDB"
    }
  }
}
