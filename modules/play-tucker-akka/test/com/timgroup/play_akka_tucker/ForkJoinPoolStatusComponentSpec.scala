package com.timgroup.play_akka_tucker

import org.scalatest._
import matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import akka.jsr166y.ForkJoinPool
import com.yammer.metrics.Metrics
import scala.collection.JavaConversions._

class ForkJoinPoolStatusComponentSpec extends path.FunSpec with MockitoSugar with MustMatchers {
  describe("the ForkJoinPool status component") {

    it("register metrics for the pool") {
      val pool = mock[ForkJoinPool]

      new ForkJoinPoolStatusComponent("default-dispatcher", pool)

      Metrics.defaultRegistry().allMetrics.map(_._1.getName) must contain ("ActiveThreadCount")
    }

  }
}
