package sbinary;
import org.scalacheck._;
import org.scalacheck.Test._;
import Gen._;
import Arbitrary._;
import Prop._;

import scala.collection._;

import Operations._;
import Instances._;
import TupleInstances._;

object BinaryTests extends Application{
  def test[T](prop : T => Boolean)(implicit arb : Arb[T] => Arbitrary[T]) = {
    check(property(prop)).result match {
      case GenException(e) => e.printStackTrace();
      case _ => ();
    }
  }

  def testBinaryTypePreservesEquality[T](name : String)(implicit bin : Binary[T], arb : Arb[T] => Arbitrary[T]) = {
    println(name);
    test((x : T) => x == fromByteArray[T](toByteArray(x)))
  }

  def testBinaryTypePreservesArrayEquality[T](name : String)(implicit bin : Binary[T], arb : Arb[T] => Arbitrary[T]) = {
    println("Array[" + name + "]");
    test((x : Array[T]) => x.deepEquals(fromByteArray[Array[T]](toByteArray(x))))
  }

  def testBinaryProperties[T](name : String)(implicit bin : Binary[T], arb : Arb[T] => Arbitrary[T]) = {
    testBinaryTypePreservesEquality[T](name);
    testLength[T];
//    testBinaryTypePreservesArrayEquality[T]("Array[" + name + "]");
//    testBinaryTypePreservesArrayEquality[Array[T]]("Array[Array[" + name + "]]");
  }

  def arrayBinaryProperties[T](name : String)(implicit bin : Binary[T], arb : Arb[T] => Arbitrary[T]) = {
    testBinaryTypePreservesArrayEquality[T](name);
    testLength[Array[T]];
//    testBinaryTypePreservesArrayEquality[T]("Array[" + name + "]");
  }

  def testLength[T](implicit bin : Binary[T], arb : Arb[T] => Arbitrary[T]) = {
    test((x : T) => {
      val stream = new java.io.ByteArrayOutputStream;
      write[T](x)(stream) == stream.toByteArray.length     
    })    
  }

  implicit def arbitraryArray[T](x: Arb[Array[T]])(implicit arb : Arb[T] => Arbitrary[T]): Arbitrary[Array[T]] =
    new Arbitrary[Array[T]] {
      def getArbitrary = arbitrary[List[T]] map ((_.toArray))
    }

  implicit def arbitraryMap[K, V](arb : Arb[immutable.Map[K, V]])(implicit arbK : Arb[K] => Arbitrary[K], arbV : Arb[V] => Arbitrary[V]) : Arbitrary[immutable.Map[K, V]]=
    new Arbitrary[immutable.Map[K, V]]{
    def getArbitrary = arbitrary[List[(K, V)]].map(x => immutable.Map.empty ++ x);
  }

  println("Primitives");
  testBinaryProperties[Byte]("Byte");
  testBinaryProperties[Char]("Char");
  testBinaryProperties[Int]("Int");
  testBinaryProperties[Double]("Double");

  // No Arbitrary instances for these. Write some.
  // testBinaryProperties[Long]("Long");
  // testBinaryProperties[Short]("Short");
  // testBinaryProperties[Float]("Float");

  println
  testBinaryProperties[String]("String")

  println ("Tuples")
  testBinaryProperties[(Int, Int, Int)]("(Int, Int, Int)");
  testBinaryProperties[(String, Int, String)]("(String, Int, String)")
  testBinaryProperties[((Int, (String, Int), Int))]("((Int, (String, Int), Byte, Byte, Int))]");

  println
  println("Options");
  testBinaryProperties[Option[String]]("Option[String]");
  testBinaryProperties[(Option[String], String)]("(Option[String], String)");
  testBinaryProperties[Option[Option[Int]]]("Option[Option[Int]]");
  
  println
  println("Lists");
  testBinaryProperties[List[String]]("List[String]");
  testBinaryProperties[List[(String, Int)]]("List[(String, Int)]");
  testBinaryProperties[List[Option[Int]]]("List[Option[Int]]");

  println
  println("Arrays");
  arrayBinaryProperties[String]("String");
  arrayBinaryProperties[Array[String]]("Array[String]");
  arrayBinaryProperties[List[Int]]("List[Int]");
  arrayBinaryProperties[Option[Byte]]("Option[Byte]");
  arrayBinaryProperties[Byte]("Byte");
  arrayBinaryProperties[(Int, Int)]("(Int, Int)");

  println
  println("Maps");
  testBinaryProperties[immutable.Map[Int, Int]]("immutable.Map[Int, Int]");
  testBinaryProperties[immutable.Map[Int, Int]]("immutable.Map[Option[String], Int]");
  testBinaryProperties[immutable.Map[Int, Int]]("immutable.Map[List[Int], String]");
}
