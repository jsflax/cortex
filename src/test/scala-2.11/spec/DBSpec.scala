package spec

import java.io.File

import cortex.db.SqlDB
import org.scalatest.matchers.{MatchResult, Matcher}

import scalikejdbc._

/**
  */
class DBSpec extends BaseSpec {

  import cortex.db.SqlDB._

  case class Person(id: Long,
                    firstName: String,
                    lastName: String,
                    email: String,
                    hash: String)

  object Person extends SQLSyntaxSupport[Person] {
    override val tableName = "persons"

    def apply(rs: WrappedResultSet): Person = new Person(
      rs.long("id"),
      rs.string("first_name"),
      rs.string("last_name"),
      rs.string("email"),
      rs.string("hash")
    )
  }

  override def configFile = Option(new File("src/test/config.txt"))

  override def dbConnection = DBConnection(
    "localhost:3306",
    "conf",
    config.user,
    config.pass
  )


  if (SqlDB.isInitialized) {
    it should "validate test information in the database" in {
      sql"""
        INSERT INTO persons(first_name, last_name, email, hash)
        VALUES("test", "test", "test@test.com", "test")
      """.update().apply()

      val person: Option[Person] =
        sql"""
          SELECT * FROM persons
          WHERE email='test@test.com'
        """.map(rs => Person(rs)).single().apply()

      sql"""
        DELETE FROM persons
        WHERE email='test@test.com'
      """.update().apply()

      val bePerson =
        new Matcher[Person] {
          def apply(left: Person) = {
            MatchResult(
              left.email.equals("test@test.com"),
              s"$left did not match test case",
              s"$left matched test case"
            )
          }
        }

      person.get should bePerson
    }
  }
}
