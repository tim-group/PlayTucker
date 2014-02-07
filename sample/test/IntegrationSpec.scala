import collection.immutable.IndexedSeq
import com.timgroup.tucker.info.Status
import helper.TuckerReader
import java.sql.Connection
import javax.sql.DataSource
import org.specs2.mutable._

import play.api.db.DB
import play.api.test._
import play.api.test.Helpers._
import helper.PlayFakeApp

class IntegrationSpec extends Specification with PlayFakeApp {
  "Status page" should {
    "contains component for the app version" in {
      val result = routeAndCall(FakeRequest("GET", "/info/status")).get

      val component = TuckerReader.componentFor(result)("version")

      component.status must be (Status.INFO)
    }
  }
}
