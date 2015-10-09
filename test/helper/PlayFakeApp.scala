package helper

import org.specs2.mutable.BeforeAfter
import play.api.Play
import play.api.test.FakeApplication

trait PlayFakeApp extends BeforeAfter {
  val application = FakeApplication()
  override def before = Play.start(application)
  override def after = Play.stop(application)
}


