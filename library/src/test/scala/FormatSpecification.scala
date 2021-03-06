package giter8

import org.scalacheck._
import sbt.io._, syntax._
import java.nio.charset.Charset

object FormatSpecification extends Properties("Format") {
  import Prop.forAll

  property("plainConversion") =
    forAll(nonDollar) { x =>
      conversion(x, Map.empty[String, String]) == x
    }
  property("formatUppercase") =
    forAll(asciiString, asciiString, nonDollar) { (x, y, z) =>
      conversion(s"""$$$x;format="upper"$$$z""", Map(x -> y)) == y.toUpperCase + z
    }
  property("formatUppercase") =
    forAll(asciiString, asciiString, nonDollar) { (x, y, z) =>
      conversion(s"""$$$x;format="lower"$$$z""", Map(x -> y)) == y.toLowerCase + z
    }

  lazy val hiragana = (0x3041 to 0x3094).toList
  lazy val nonDollarChar: Gen[Char] = Gen.oneOf(((0x20 to 0xff).toList ::: hiragana).filter( x =>
    Character.isDefined(x) && x != 0x24 && x != 0x5c).map(_.toChar))
  lazy val nonDollar: Gen[String] = Gen.sized { size =>
    Gen.listOfN(size, nonDollarChar).map(_.mkString)
  } filter {_.nonEmpty}
  lazy val asciiChar: Gen[Char] = Gen.oneOf( ((0x41 to 0x5a).toList ::: (0x61 to 0x7a).toList).filter( x =>
    Character.isDefined(x)).map(_.toChar))
  lazy val asciiString: Gen[String] = Gen.sized { size =>
    Gen.listOfN(size, asciiChar).map(_.mkString)
  } filter {_.nonEmpty}
  def conversion(inContent: String, ps: Map[String,String]): String = {
    IO.withTemporaryDirectory { tempDir =>
      val in = tempDir / "in.txt"
      val out = tempDir / "out.txt"
      IO.write(in, inContent, IO.utf8)
      G8(in, out, ps)
      val outContent = IO.read(out, IO.utf8)
      // println(outContent)
      outContent
    }
  }
}
