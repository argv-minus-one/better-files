package better.files

import java.io.{InputStream, BufferedReader, LineNumberReader, Reader, StringReader}

import scala.io.Codec

trait Scanner extends Iterator[String] with AutoCloseable {
  def lineNumber(): Int

  def next[A: Scannable]: A = implicitly[Scannable[A]].apply(this)

  def tillDelimiter(delimiter: String): String

  def tillEndOfLine() = tillDelimiter(Scanner.Config.Delimiters.lines)

  def nonEmptyLines: Iterator[String] = produce(tillEndOfLine()).till(hasNext)
}

/**
 * Faster, safer and more idiomatic Scala replacement for java.util.Scanner
 * See: http://codeforces.com/blog/entry/7018
 */
object Scanner {

  def apply(str: String)(implicit config: Config): Scanner = Scanner(new StringReader(str))(config)

  def apply(reader: Reader)(implicit config: Config): Scanner = Scanner(reader.buffered)(config)

  def apply(reader: BufferedReader)(implicit config: Config): Scanner = Scanner(new LineNumberReader(reader))(config)

  def apply(inputStream: InputStream)(implicit config: Config): Scanner = Scanner(inputStream.reader(config.codec))(config)

  def apply(reader: LineNumberReader)(implicit config: Config): Scanner = new Scanner {
    private[this] val tokenizers = reader.tokenizers(config).buffered
    private[this] def tokenizer() = {
      while(tokenizers.headOption.exists(st => !st.hasMoreTokens)) tokenizers.next()
      tokenizers.headOption
    }
    override def lineNumber() = reader.getLineNumber
    override def tillDelimiter(delimiter: String) = tokenizer().get.nextToken(delimiter)
    override def next() = tokenizer().get.nextToken()
    override def hasNext = tokenizer().nonEmpty
    override def close() = reader.close()
  }

  val stdIn = Scanner(System.in)(Config.default)

  /**
   * Use this to configure your Scanner
   *
   * @param delimiter
   * @param includeDelimiters
   */
  case class Config(delimiter: String, includeDelimiters: Boolean)(implicit val codec: Codec)
  object Config {
    implicit val default = Config(delimiter = Delimiters.whitespaces, includeDelimiters = false)
    object Delimiters {
      val lines = "\n\r"
      val whitespaces = " \t\f" + lines
    }
  }
}

/**
 * Implement this trait to make thing parseable
 */
trait Scannable[A] {
  def apply(scanner: Scanner): A
  def map[B](f: A => B): Scannable[B] = Scannable(apply _ andThen f)
  def +[B](that: Scannable[B]): Scannable[(A, B)] = Scannable(s => this(s) -> that(s))
}

object Scannable {
  def apply[A](f: Scanner => A): Scannable[A] = new Scannable[A] {
    override def apply(scanner: Scanner) = f(scanner)
  }
  implicit val stringScanner      : Scannable[String]     = Scannable(_.next())
  implicit val boolScanner        : Scannable[Boolean]    = stringScanner.map(_.toBoolean)
  implicit val byteScanner        : Scannable[Byte]       = stringScanner.map(_.toByte)  //TODO: https://issues.scala-lang.org/browse/SI-9706
  implicit val shortScanner       : Scannable[Short]      = stringScanner.map(_.toShort)
  implicit val intScanner         : Scannable[Int]        = stringScanner.map(_.toInt)
  implicit val longScanner        : Scannable[Long]       = stringScanner.map(_.toLong)
  implicit val bigIntScanner      : Scannable[BigInt]     = stringScanner.map(BigInt(_))
  implicit val floatScanner       : Scannable[Float]      = stringScanner.map(_.toFloat)
  implicit val doubleScanner      : Scannable[Double]     = stringScanner.map(_.toDouble)
  implicit val bigDecimalScanner  : Scannable[BigDecimal] = stringScanner.map(BigDecimal(_))
  implicit def tuple2Scanner[T1, T2](implicit t1: Scannable[T1], t2: Scannable[T2]): Scannable[(T1, T2)] = t1 + t2
}
