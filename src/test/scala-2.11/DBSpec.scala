import cortex.io.Cortex
import cortex.util.test
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{Matchers, FlatSpec}
import scalikejdbc._

/**
 */
class DBSpec extends FlatSpec with Matchers {
  import cortex.db.DB._
  case class Person(id: Long, firstName: String, lastName: String, email: String)
  object Person extends SQLSyntaxSupport[Person] {
    override val tableName = "persons"
    def apply(rs: WrappedResultSet): Person = new Person(
      rs.long("id"), rs.string("first_name"), rs.string("last_name"), rs.string("email")
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
    val person: Option[Person] = sql"""
      SELECT * FROM persons
      WHERE email='jsflax@gmail.com'
    """.map(rs => Person(rs)).single().apply()

    val bePerson =
      new Matcher[Person] {
        def apply(left: Person) = {
          MatchResult(
            left.email.equals("jsflax@gmail.com") && left.id == 1,
            s"$left did not match test case",
            s"$left matched test case"
          )
        }
      }

    person.get should bePerson
  }
}
