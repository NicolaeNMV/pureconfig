package pureconfig

import java.io.File
import java.math.{ BigInteger, BigDecimal => JavaBigDecimal }
import java.net.{ URI, URL }
import java.nio.file.Path
import java.time._
import java.time.{ Duration => JavaDuration }
import java.util.UUID
import java.util.regex.Pattern

import com.typesafe.config._
import pureconfig.arbitrary._
import pureconfig.data.Percentage
import pureconfig.equality._
import pureconfig.error.{ CannotConvert, EmptyStringFound, WrongSizeString }
import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.duration.{ Duration, FiniteDuration, _ }
import scala.util.matching.Regex

class BasicConvertersSuite extends BaseSuite {
  implicit override val generatorDrivenConfig = PropertyCheckConfiguration(minSuccessful = 100)

  behavior of "ConfigConvert"

  checkArbitrary[Duration]
  checkFailure[Duration, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailure[Duration, CannotConvert](ConfigValueFactory.fromIterable(List(1).asJava))

  checkArbitrary[JavaDuration]
  checkFailure[JavaDuration, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailure[JavaDuration, CannotConvert](ConfigValueFactory.fromIterable(List(1).asJava))

  checkArbitrary[FiniteDuration]
  checkFailure[FiniteDuration, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailure[FiniteDuration, CannotConvert](
    ConfigValueFactory.fromIterable(List(1).asJava),
    ConfigConvert[Duration].to(Duration.MinusInf),
    ConfigConvert[Duration].to(Duration.Inf))

  checkReadString[FiniteDuration](
    "5 seconds" -> 5.seconds,
    "28 ms" -> 28.millis,
    "28ms" -> 28.millis,
    "28" -> 28.millis,
    "28 milliseconds" -> 28.millis,
    "1d" -> 1.day)

  checkRead[FiniteDuration](
    ConfigValueFactory.fromAnyRef(28) -> 28.millis)

  checkArbitrary[Instant]

  checkArbitrary[ZoneOffset]

  checkArbitrary[ZoneId]

  checkArbitrary[Period]

  checkReadString[Period](
    "1d" -> Period.ofDays(1),
    "42" -> Period.ofDays(42),
    "4 weeks" -> Period.ofWeeks(4),
    "13 months" -> Period.ofMonths(13),
    "2y" -> Period.ofYears(2))

  checkRead[Period](
    ConfigValueFactory.fromAnyRef(42) -> Period.ofDays(42))

  checkFailure[Period, CannotConvert](
    ConfigValueFactory.fromAnyRef("4kb"),
    ConfigValueFactory.fromAnyRef("x weeks"))

  checkArbitrary[Year]

  checkArbitrary[String]

  checkArbitrary[Char]
  checkFailure[Char, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailure[Char, WrongSizeString](ConfigValueFactory.fromAnyRef("not a char"))

  checkArbitrary[Boolean]
  checkRead[Boolean](
    ConfigValueFactory.fromAnyRef("yes") -> true,
    ConfigValueFactory.fromAnyRef("on") -> true,
    ConfigValueFactory.fromAnyRef("no") -> false,
    ConfigValueFactory.fromAnyRef("off") -> false)

  checkArbitrary[Double]
  checkArbitrary2[Double, Percentage](_.toDoubleFraction)
  checkFailure[Double, EmptyStringFound](ConfigValueFactory.fromAnyRef(""))
  checkFailure[Double, CannotConvert](ConfigValueFactory.fromIterable(List(1, 2, 3, 4).asJava))

  checkArbitrary[Float]
  checkArbitrary2[Float, Percentage](_.toFloatFraction)

  checkArbitrary[Int]

  checkArbitrary[Long]

  checkArbitrary[Short]

  checkArbitrary[BigInt]
  checkArbitrary[BigDecimal]
  checkArbitrary[BigInteger]
  checkArbitrary[JavaBigDecimal]

  checkArbitrary[UUID]

  checkArbitrary[Path]

  checkArbitrary[File]

  checkReadWriteString[DayOfWeek]("MONDAY" -> DayOfWeek.MONDAY)
  checkReadWriteString[Month]("JULY" -> Month.JULY)
  checkFailure[DayOfWeek, CannotConvert](
    ConfigValueFactory.fromAnyRef("thursday"), // lowercase string vs upper case enum
    ConfigValueFactory.fromAnyRef("this is not a day")) // no such value

  checkArbitrary[immutable.HashSet[String]]

  checkArbitrary[immutable.List[Float]]
  checkRead[immutable.List[Int]](
    // order of keys maintained
    ConfigValueFactory.fromMap(Map("2" -> 1, "0" -> 2, "1" -> 3).asJava) -> List(2, 3, 1),
    ConfigValueFactory.fromMap(Map("3" -> 2, "1" -> 4).asJava) -> List(4, 2))
  checkFailure[immutable.List[Int], CannotConvert](
    ConfigValueFactory.fromMap(Map("1" -> 1, "a" -> 2).asJava))

  checkArbitrary[immutable.ListSet[Int]]

  checkArbitrary[immutable.Map[String, Int]]
  checkFailure[immutable.Map[String, Int], CannotConvert](
    ConfigFactory.parseString("conf.a=1").root(), // nested map should fail
    ConfigFactory.parseString("{ a=b }").root()) // wrong value type should fail

  checkArbitrary[immutable.Queue[Boolean]]

  checkArbitrary[immutable.Set[Double]]
  checkRead[immutable.Set[Int]](
    ConfigValueFactory.fromMap(Map("1" -> 4, "2" -> 5, "3" -> 6).asJava) -> Set(4, 5, 6))

  checkArbitrary[immutable.Stream[String]]

  checkArbitrary[immutable.TreeSet[Int]]

  checkArbitrary[immutable.Vector[Short]]

  checkArbitrary[Option[Int]]

  checkReadWriteString[Pattern]("(a|b)" -> Pattern.compile("(a|b)"))
  checkFailure[Pattern, CannotConvert](ConfigValueFactory.fromAnyRef("(a|b")) // missing closing ')'

  checkReadWriteString[Regex]("(a|b)" -> new Regex("(a|b)"))
  checkFailure[Regex, CannotConvert](ConfigValueFactory.fromAnyRef("(a|b")) // missing closing ')'

  checkReadWriteString[URL](
    "http://host/path?with=query&param" -> new URL("http://host/path?with=query&param"))

  checkReadWriteString[URI](
    "http://host/path?with=query&param" -> new URI("http://host/path?with=query&param"))

  checkReadWrite[ConfigList](
    ConfigValueFactory.fromIterable(List().asJava) -> ConfigValueFactory.fromIterable(List().asJava),
    ConfigValueFactory.fromIterable(List(1, 2, 3).asJava) -> ConfigValueFactory.fromIterable(List(1, 2, 3).asJava))

  checkReadWrite[ConfigValue](
    ConfigValueFactory.fromAnyRef(4) -> ConfigValueFactory.fromAnyRef(4),
    ConfigValueFactory.fromAnyRef("str") -> ConfigValueFactory.fromAnyRef("str"),
    ConfigValueFactory.fromAnyRef(List(1, 2, 3).asJava) -> ConfigValueFactory.fromAnyRef(List(1, 2, 3).asJava))

  {
    val conf = ConfigFactory.parseString("""{ v1 = 3, v2 = 4 }""".stripMargin)

    checkReadWrite[ConfigObject](
      conf.root() -> ConfigValueFactory.fromMap(Map("v1" -> 3, "v2" -> 4).asJava))

    checkReadWrite[Config](
      conf.root() -> conf)
  }
}
