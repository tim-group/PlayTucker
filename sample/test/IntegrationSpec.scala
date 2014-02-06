import com.timgroup.tucker.info.Status
import helper.TuckerReader
import org.specs2.mutable._

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

    "contains component for each datasource" in {
      val result = routeAndCall(FakeRequest("GET", "/info/status")).get

      val database_1 = TuckerReader.componentFor(result)("BoneCp-database_1")
      val database_2 = TuckerReader.componentFor(result)("BoneCp-database_2")

      database_1.status must be (Status.OK)
      database_2.status must be (Status.OK)
    }
  }
}