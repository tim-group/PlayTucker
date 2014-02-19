package com.timgroup.play_akka_tucker

import play.api.{Logger, Application, Plugin}
import com.timgroup.play_tucker.PlayTuckerPlugin
import play.api.libs.concurrent.Akka
import akka.dispatch.{ExecutionContext, ExecutorServiceDelegate, Dispatcher}
import akka.jsr166y.ForkJoinPool
import com.timgroup.tucker.info.Component

case class ExecutionContextIdentifier(val path: String) {
  def name: String = {
    path.split('.').last
  }

  def lookup: ExecutionContext = {
    import play.api.Play.current
    Akka.system.dispatchers.lookup(path)
  }
}

class PlayAkkaTuckerPlugin(application: Application) extends Plugin {

  override def onStart() {
    import play.api.Play.current
    import com.typesafe.plugin.use

    val tucker = use[PlayTuckerPlugin]

    getExecutionContextsFromConfig
      .flatMap(createComponent)
      .foreach(tucker.addComponent)

    Logger.info("PlayAkkaTuckerPlugin started")
  }

  private def createComponent(executionContextIdentifier: ExecutionContextIdentifier): Option[Component] = {
    getPool(executionContextIdentifier).map {
      pool => new ForkJoinPoolStatusComponent(executionContextIdentifier.name, pool)
    }
  }

  private def getPool(executionContextIdentifier: ExecutionContextIdentifier): Option[ForkJoinPool] = {

    try {
      val executionContext = executionContextIdentifier.lookup
      val dispatcher = executionContext.asInstanceOf[Dispatcher]
      val executorServiceField = dispatcher.getClass.getDeclaredField("executorService")
      executorServiceField.setAccessible(true)
      val atomicReference = executorServiceField.get(dispatcher)
      val executorServiceDelegate = atomicReference.asInstanceOf[java.util.concurrent.atomic.AtomicReference[Object]].get.asInstanceOf[ExecutorServiceDelegate]
      val forkJoinPool = executorServiceDelegate.executor.asInstanceOf[ForkJoinPool]
      Some(forkJoinPool)
    } catch {
      case e: Exception => {
        Logger.error("Error getting ForkJoinPool for EC %s: %s".format(executionContextIdentifier.name, e.getMessage))
        e.printStackTrace()
        None
      }
    }
  }

  private def getExecutionContextsFromConfig(): Seq[ExecutionContextIdentifier] = {
    val prefix = "play.akka.actor"

    application
      .configuration
      .getConfig(prefix)
      .map(s => s.subKeys)
      .flatten
      .filter(_.endsWith("dispatcher"))
      .toSeq
      .map(name => ExecutionContextIdentifier(prefix + "." + name))
  }

  override def onStop() {
    Logger.info("PlayAkkaTuckerPlugin stopped")
  }

  def components = {
    Nil
  }
}
