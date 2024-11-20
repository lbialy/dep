package ma.chinespirit.dep

import scala.util.control.NonFatal
import caseapp.*
import caseapp.core.argparser.*
import caseapp.core.Error

object Cli:

  @AppName("Scala Dep")
  @ProgName("dep")
  @AppVersion("0.1.0")
  @ArgsName("dependency to convert")
  case class FlagsAndOptions(
    @Name("s")
    @HelpMessage("Source format of the dependency")
    @ValueDescription("Valid formats are: " + Format.validIdentifiers.mkString(", "))
    source: Option[Format] = None,
    @Name("t")
    @HelpMessage("Target format of the dependency")
    @ValueDescription("Valid formats are: " + Format.validIdentifiers.mkString(", "))
    target: Format = Format.scalaCli,
    @HelpMessage("Assume scala 3 by default for dependencies coming from source format without language version?")
    assumeScala3: Boolean = true,
    @HelpMessage("Scala version of the target format, overrides assumed scala version")
    targetScalaVersion: Option[String] = None
  )

  given ArgParser[Format] = SimpleArgParser.from("format") { input =>
    Format.of(input).toRight(Error.Other(s"Unknown format '$input' - ${Format.validFormats}"))
  }

  def parseFlagsAndOptions(opts: Seq[String]): (FlagsAndOptions, RemainingArgs) =
    CaseApp.process[FlagsAndOptions](opts)

end Cli
