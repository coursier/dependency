package dependency

import scala.collection.IterableOps
import scala.collection.IterableFactoryDefaults
import scala.collection.IterableFactory
import scala.collection.mutable.Builder

class CovariantSet[+A] private (private val elements: List[A])
  extends Iterable[A]
  with IterableOps[A, CovariantSet, CovariantSet[A]]
  with IterableFactoryDefaults[A, CovariantSet] {

  protected[this] override def className: String = "CovariantSet"

  def +=[B >: A](elem: B): CovariantSet[B] =
    if (elements.contains(elem))
      this
    else
      new CovariantSet[B](elem :: elements)
  def iterator: Iterator[A] =
    elements.iterator

  override def iterableFactory: IterableFactory[CovariantSet] =
    CovariantSet

  override def equals(obj: Any): Boolean =
    if (obj == null) false
    else if (this eq obj.asInstanceOf[AnyRef]) true
    else if (obj.isInstanceOf[CovariantSet[_]]) {
      val other = obj.asInstanceOf[CovariantSet[_]]
      elements.toSet == other.elements.toSet
    }
    else super.equals(obj)

}

object CovariantSet extends IterableFactory[CovariantSet] {
  def from[A](source: IterableOnce[A]): CovariantSet[A] =
    new CovariantSet[A](source.toList.distinct)
  def empty[A]: CovariantSet[A] =
    new CovariantSet[A](Nil)
  def newBuilder[A]: Builder[A, CovariantSet[A]] =
    new Builder[A, CovariantSet[A]] {
      val underlying = collection.mutable.Set.newBuilder[A]
      def addOne(elem: A): this.type = {
        underlying.addOne(elem)
        this
      }
      def clear(): Unit = underlying.clear()
      def result(): CovariantSet[A] = new CovariantSet[A](underlying.result().toList)
    }
}