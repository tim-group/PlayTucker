package helper

import com.timgroup.tucker.info.Status
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit._
import play.api.mvc.Result
import play.api.test.Helpers.contentAsString
import akka.util.Timeout

object TuckerReader {
  implicit val timeout = Timeout(Duration(5, SECONDS))

  case class Component(label: String, value: String, status: Status)

  def componentFor(result: Future[Result])(id: String): Component = {
      val components = contentAsXML(result) \\ "component"
      val component = components.find { node => node.attribute("id").get.text == id }.get
      val value = (component \ "value").text
      val label = (component.text stripSuffix value).trim()
      val status = Status.valueOf(component.attribute("class").get.text.toUpperCase)
      Component(label, value, status)
  }

  def contentAsXML(result: Future[Result]) = {
    scala.xml.XML.loadString(contentAsString(result).replace("SYSTEM \"status-page.dtd\"", ""))
  }
}
