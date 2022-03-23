package dependency

import scala.collection.immutable.SortedSet
import scala.collection.mutable.Builder
import scala.collection.IterableLike
import scala.collection.generic.CanBuildFrom
import scala.collection.AbstractIterable
import scala.collection.generic.GenericTraversableTemplate
import scala.collection.generic.GenericCompanion

class CovariantSet[+A] private (private val elements: List[A])
  extends AbstractIterable[A]
  with GenericTraversableTemplate[A, CovariantSet]
  with IterableLike[A, CovariantSet[A]] {

  override def companion: GenericCompanion[CovariantSet] = CovariantSet

  def +=[B >: A](elem: B): CovariantSet[B] =
    if (elements.contains(elem))
      this
    else
      new CovariantSet[B](elem :: elements)
  def iterator: Iterator[A] =
    elements.iterator

  // override def newBuilder[A]: Builder[A, CovariantSet[A]] = ???

  override def equals(obj: Any): Boolean =
    if (obj == null) false
    else if (this eq obj.asInstanceOf[AnyRef]) true
    else if (obj.isInstanceOf[CovariantSet[_]]) {
      val other = obj.asInstanceOf[CovariantSet[_]]
      elements.toSet == other.elements.toSet
    }
    else super.equals(obj)

}

object CovariantSet extends GenericCompanion[CovariantSet] {

  implicit def cbf[A, B]: CanBuildFrom[CovariantSet[A], B, CovariantSet[B]] =
    new CanBuildFrom[CovariantSet[A], B, CovariantSet[B]] {
      def apply(): scala.collection.mutable.Builder[B,dependency.CovariantSet[B]] =
        newBuilder[B]
      def apply(from: dependency.CovariantSet[A]): scala.collection.mutable.Builder[B,dependency.CovariantSet[B]] =
        newBuilder[B]
    }

  def newBuilder[A]: Builder[A, CovariantSet[A]] =
    new Builder[A, CovariantSet[A]] {
      val underlying = collection.mutable.Set.newBuilder[A]
      def +=(elem: A): this.type = {
        underlying += elem
        this
      }
      def clear(): Unit = underlying.clear()
      def result(): CovariantSet[A] = new CovariantSet[A](underlying.result().toList)
    }
}