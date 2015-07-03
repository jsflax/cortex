package spec

import cortex.io.Cortex
import cortex.util.test
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{FlatSpec, Matchers}
import scalikejdbc._

/**
 */
class DBSpec extends FlatSpec with Matchers {
  import cortex.db.DB._
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

  @test object app extends Cortex {
    override def port = 9997

    override def controllers = Seq()
    override def views = Seq()
    override def dbConnection = DBConnection(
      "localhost:3306/conf",
      "root",
      "root"
    )
  }

  app.hashCode()

  it should "validate test information in the database" in {
    sql"""
      INSERT INTO persons(first_name, last_name, email, hash)
      VALUES("test", "test", "test@test.com", "test")
    """.update().apply()

    val person: Option[Person] = sql"""
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
