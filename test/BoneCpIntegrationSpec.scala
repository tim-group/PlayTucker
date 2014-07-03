import com.timgroup.tucker.info.Status
import helper.TuckerReader
import java.sql.Connection
import org.specs2.mutable._

import play.api.db.DB
import play.api.test._
import play.api.test.Helpers._
import helper.PlayFakeApp

class BoneCpIntegrationSpec extends Specification with PlayFakeApp {
  "Status page" should {
    "contains component for the app version" in {
      val result = route(FakeRequest("GET", "/info/status")).get

      val component = TuckerReader.componentFor(result)("version")

      component.status must be (Status.INFO)
    }

    "contains pool usage component for each datasource" in {
      val result = route(FakeRequest("GET", "/info/status")).get

      val database_1 = TuckerReader.componentFor(result)("BoneCp-database_1")
      val database_2 = TuckerReader.componentFor(result)("BoneCp-database_2")

      database_1.status must be (Status.OK)
      database_2.status must be (Status.OK)
    }

    "contains connectivity component for each datasource" in {
      val result = route(FakeRequest("GET", "/info/status")).get

      val database_1 = TuckerReader.componentFor(result)("Connectivity-database_1")
      val database_2 = TuckerReader.componentFor(result)("Connectivity-database_2")

      database_1.status must be (Status.OK)
      database_2.status must be (Status.OK)
    }

    "component shows the number of connections used/crated/max" in {
      val used = useConnections("database_1", 4)

      val result = route(FakeRequest("GET", "/info/status")).get

      val database_1 = TuckerReader.componentFor(result)("BoneCp-database_1")

      used.foreach(_.close())

      database_1.value must equalTo ("4 in use of 20 (max 50)")
    }

    "be critical when less than 10 connections are available" in {
      val used = useConnections("database_1", 41)

      val result = route(FakeRequest("GET", "/info/status")).get

      val database_1 = TuckerReader.componentFor(result)("BoneCp-database_1")

      used.foreach(_.close())

      database_1.status must be(Status.CRITICAL)
    }
  }

  private def useConnections(datasourceName: String, leaveUsed: Int): List[Connection] = {
    import play.api.Play.current
    val source = DB.getDataSource(datasourceName)

    (1 to leaveUsed).map(x => {
      source.getConnection
    }).toList
  }
}
