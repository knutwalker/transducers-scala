package scala.transducer.internal

import org.scalatest.FunSpec

class CappedEvictingQueueSpec extends FunSpec {

  describe("Adding to a 5-sized evicting queue") {
    def newQueue = new CappedEvictingQueue[String](5)

    it("should consume 5 elements") {
      val queue = newQueue
      for (i ← 1 to 5)
        assert(queue.add(i.toString) == None)
    }

    it("should evict the oldest elements") {
      val queue = newQueue
      for (i ← 1 to 5) queue.add(i.toString)

      for (i ← 1 to 5) assert(queue.add((i + 10).toString) == Some(i.toString))
    }
  }

  describe("Iterating over a 5-sized evicting queue") {

    it("should iterator all elements from old to young") {
      val queue = new CappedEvictingQueue[String](5)
      for (i ← 1 to 7) queue.add(i.toString)

      val strings = Iterator.from(3).map(_.toString).take(5)
      queue.elements.zip(strings) foreach {
        case (actual, expected) ⇒ assert(actual == expected)
      }
      assert(!strings.hasNext)
    }

    it("should iterate only elements that were added") {
      val queue = new CappedEvictingQueue[String](5)
      for (i ← 1 to 3) queue.add(i.toString)

      val strings = Iterator.from(1).map(_.toString).take(3)
      queue.elements.zip(strings) foreach {
        case (actual, expected) ⇒ assert(actual == expected)
      }
      assert(!strings.hasNext)
    }
  }

}
