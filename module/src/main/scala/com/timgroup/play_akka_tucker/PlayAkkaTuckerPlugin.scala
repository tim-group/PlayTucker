package com.timgroup.play_akka_tucker

import play.api.{Logger, Application, Plugin}
import com.timgroup.play_tucker.PlayTuckerPlugin
import play.api.libs.concurrent.Akka
import akka.dispatch.{ExecutorServiceDelegate, Dispatcher}
import akka.jsr166y.ForkJoinPool
import com.timgroup.tucker.info.Component

class PlayAkkaTuckerPlugin(application: Application) extends Plugin {

  override def onStart() {
    import play.api.Play.current
    import com.typesafe.plugin.use

    val tucker = use[PlayTuckerPlugin]

    getDispatcherNamesFromConfig
      .flatMap(createComponent)
      .foreach(tucker.addComponent)

    Logger.info("PlayAkkaTuckerPlugin started")
  }

  private def createComponent(executionContextName: String): Option[Component] = {
    getPool(executionContextName).map {
      pool => new ForkJoinPoolStatusComponent(executionContextName, pool)
    }
  }

  private def getPool(executionContextName: String): Option[ForkJoinPool] = {
    import play.api.Play.current

    try {
      val executionContext = Akka.system.dispatchers.lookup(executionContextName)
      val dispatcher = executionContext.asInstanceOf[Dispatcher]
      val executorServiceField = dispatcher.getClass.getDeclaredField("executorService")
      executorServiceField.setAccessible(true)
      val atomicReference = executorServiceField.get(dispatcher)
      val executorServiceDelegate = atomicReference.asInstanceOf[java.util.concurrent.atomic.AtomicReference[Object]].get.asInstanceOf[ExecutorServiceDelegate]
      val forkJoinPool = executorServiceDelegate.executor.asInstanceOf[ForkJoinPool]
      Some(forkJoinPool)
    } catch {
      case e: Exception => {
        Logger.error(e.getMessage)
        e.printStackTrace()
        None
      }
    }
  }

  private def getDispatcherNamesFromConfig(): Seq[String] = {
    application
      .configuration
      .getConfig("play.akka.actor")
      .map(s => s.subKeys)
      .flatten
      .filter(_.endsWith("dispatcher"))
      .toSeq
  }

  override def onStop() {
    Logger.info("PlayAkkaTuckerPlugin stopped")
  }

  def components = {
    Nil
  }
}
