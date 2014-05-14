package com.timgroup.play_akka_tucker

import play.api.{Logger, Application, Plugin}
import com.timgroup.play_tucker.PlayTuckerPlugin
import play.api.libs.concurrent.{Promise, Akka}
import akka.dispatch.{MessageDispatcherConfigurator, ExecutionContext, ExecutorServiceDelegate, Dispatcher}
import akka.jsr166y.ForkJoinPool
import com.timgroup.tucker.info.Component
import akka.actor.ActorSystem
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import play.core.Invoker

case class ExecutionContextIdentifier(val path: String, actorSystem: ActorSystem, actorSystemName: String) {
  def name: String = {
    actorSystemName + "." + path.replace(".", "_")
  }

  def lookup: ExecutionContext = {
    actorSystem.dispatchers.lookup(path)
  }
}

class PlayAkkaTuckerPlugin(application: Application) extends Plugin {

  override def onStart() {
    import play.api.Play.current
    import com.typesafe.plugin.use

    val tucker = use[PlayTuckerPlugin]

    Promise.pure("Force Promise system to be initialized")

    val executionContexts = getExecutionContextsFor(Akka.system, "Akka") ++
                            getExecutionContextsFor(Invoker.system, "Invoker") ++
                            getExecutionContextsFor(promiseSystem(), "Promise")
    executionContexts
      .flatMap(createComponent)
      .foreach(tucker.addComponent)

    Logger.info("PlayAkkaTuckerPlugin started")
  }

  private def promiseSystem() = {
    val system = Promise.getClass.getDeclaredField("system")
    system.setAccessible(true)
    system.get(Promise).asInstanceOf[ActorSystem]
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

  private def getExecutionContextsFor(actorSystem: ActorSystem, actorSystemName: String): Seq[ExecutionContextIdentifier] = {
    val field = actorSystem.dispatchers.getClass.getDeclaredField("dispatcherConfigurators")
    field.setAccessible(true)
    val dispatcherMap = field.get(actorSystem.dispatchers).asInstanceOf[ConcurrentHashMap[String, MessageDispatcherConfigurator]]

    dispatcherMap.keySet().toSeq.map(path => ExecutionContextIdentifier(path, actorSystem, actorSystemName))
  }

  override def onStop() {
    Logger.info("PlayAkkaTuckerPlugin stopped")
  }

  def components = {
    Nil
  }
}
