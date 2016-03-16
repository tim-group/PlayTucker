import com.timgroup.tucker.info.Status
import helper.TuckerReader
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import helper.PlayFakeApp

class IntegrationSpec extends Specification with PlayFakeApp {
  "Status page" should {
    "contains component for the app version" in {
      val result = route(FakeRequest("GET", "/info/status")).get

      val component = TuckerReader.componentFor(result)("version")

      component.status must be (Status.INFO)
    }

    "contains the application name" in {
      val result = route(FakeRequest("GET", "/info/status")).get

      val content = TuckerReader.contentAsXML(result)

      content.attribute("id").get(0).text must_== "local-app"
    }

    "contains a component for the version of the JVM" in {
      val result = route(FakeRequest("GET", "/info/status")).get

      val component = TuckerReader.componentFor(result)("jvmversion")

      component.status must be (Status.INFO)
    }

    "responds to callback" in {
      val result = route(FakeRequest("GET", "/info/status?callback=myCallback")).get

      import play.api.test.Helpers.contentAsString

      val content = contentAsString(result)

      content must startWith ("myCallback")
    }

    "responds to callback with jvmversion field" in {
      val result = route(FakeRequest("GET", "/info/status?callback=myCallback")).get

      import play.api.test.Helpers.contentAsString

      val content = contentAsString(result)

      content must contain (""""id":"jvmversion"""")
    }
  }
}
