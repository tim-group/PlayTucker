package helper

import play.api.test.FakeApplication
import org.specs2.specification._
import play.api.Play

trait PlayFakeApp extends BaseSpecification {
  val application = FakeApplication()
  override def map(fs: => Fragments) = Step(Play.start(application)) ^ super.map(fs) ^ Step(Play.stop())
}

