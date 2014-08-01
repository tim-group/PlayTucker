package helper

import play.api.mvc.{AsyncResult, Result}
import com.timgroup.tucker.info.Status

object TuckerReader {
  case class Component(label: String, value: String, status: Status)

  def contentAsXML(result: Result) = {
    scala.xml.XML.loadString(contentAsString(result).replace("SYSTEM \"status-page.dtd\"", ""))
  }

  def componentFor(result: Result)(id: String): Component = {
      val components = contentAsXML(result) \\ "component"
      val component = components.find { node => node.attribute("id").get.text == id }.get
      val value = (component \ "value").text
      val label = (component.text stripSuffix value).trim()
      val status = Status.valueOf(component.attribute("class").get.text.toUpperCase)
      Component(label, value, status)
  }

  private def contentAsString(r: Result): String = play.api.test.Helpers.contentAsString(waitForAsyncResult(r))

  private def waitForAsyncResult(r: Result): Result = {
    r match {
      case AsyncResult(pr) => pr.await.get
      case _ => r
    }
  }
}
