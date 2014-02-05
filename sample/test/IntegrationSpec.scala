package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class IntegrationSpec extends Specification {
  "Application" should {
    "returns hello world" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")
        browser.pageSource must contain("Hello World!")
      }
    }
  }
}