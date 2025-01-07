package ma.chinespirit.dep

import caseapp.*
import caseapp.core.argparser.*
import caseapp.core.Error
import caseapp.core.app.PlatformUtil
import ma.chinespirit.dep.Parser
import scala.io.Source.stdin
import ma.chinespirit.dep.search.Platform

object Cli:

  def readFromStdin: String =
    println("Paste your dependency here and press enter twice:")
    stdin.getLines().takeWhile(_ != "").mkString("\n")

  @ArgsName("dependency to convert")
  case class ConvertOptions(
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
    @Name("tsv")
    @HelpMessage("Scala version of the target format, overrides assumed scala version")
    targetScalaVersion: Option[String] = None
  )

  @ArgsName("search string")
  case class SearchOptions(
    @Name("t")
    @HelpMessage("Target format of the dependency")
    @ValueDescription("Valid formats are: " + Format.validIdentifiers.mkString(", "))
    target: Option[Format] = None,
    @Name("p")
    @HelpMessage(s"Platform of the dependency")
    @ValueDescription("Available platforms are: " + Platform.values.mkString(", ").toLowerCase)
    platform: Platform = Platform.JVM,
    @Name("s")
    @HelpMessage(s"Scala version of dependency to search for")
    @ValueDescription("Available scala versions are: " + ScalaVersionSuffix.values.mkString(", ").toLowerCase)
    scalaVersion: ScalaVersionSuffix = ScalaVersionSuffix.`3`,
    @Name("i")
    @HelpMessage("Interactive mode")
    interactive: Boolean = false
  )

  given ArgParser[Format] = SimpleArgParser.from("format") { input =>
    Format.of(input).toRight(Error.Other(s"Unknown format '$input' - ${Format.validFormats}"))
  }

  given ArgParser[Platform] = SimpleArgParser.from("platform") { input =>
    Platform.of(input).toRight(Error.Other(s"Unknown platform '$input' - ${Platform.values.mkString(", ").toLowerCase}"))
  }

  given ArgParser[ScalaVersionSuffix] = SimpleArgParser.from("scalaVersion") { input =>
    ScalaVersionSuffix
      .of(input)
      .toRight(Error.Other(s"Unknown scalaVersion '$input' - ${ScalaVersionSuffix.values.mkString(", ").toLowerCase}"))
  }

  object Convert extends Command[ConvertOptions]:
    override def names: List[List[String]] = List(
      List("convert"),
      List("c")
    )
    def run(flagsAndOptions: ConvertOptions, remaining: RemainingArgs): Unit =
      val bareWords  = remaining.all
      val parseInput = if bareWords.isEmpty then readFromStdin else bareWords.mkString(" ")

      val parseResult =
        flagsAndOptions.source match
          case Some(sourceFormat) => // user provided explicit source format
            Parser.parse(parseInput, sourceFormat)
          case None => // user did not provide a source format, try to infer one
            Parser.inferFormatAndParse(parseInput)

      parseResult match
        case Right(parsedDep) =>
          given RenderContext = RenderContext(
            flagsAndOptions.targetScalaVersion.getOrElse(flagsAndOptions.assumeScala3)
          )

          val result = parsedDep.renderTo(flagsAndOptions.target)

          printLine(result)
          Clipboard.put(result)

          printLine("Dependency copied to clipboard!")
          exit(0)
        case Left(err) =>
          printLine(err.render)
          exit(1)

  object Search extends Command[SearchOptions]:

    override def names: List[List[String]] = List(
      List("search"),
      List("s")
    )
    def run(options: SearchOptions, args: RemainingArgs): Unit =
      val searchApi = search.Search(search.ScalaDexBaseUrl)
      val query     = args.all.mkString(" ")
      searchApi.search(query) match
        case Left(err) => printLine(err.getMessage())
        case Right(dep) =>
          given RenderContext = RenderContext(assumeScala3 = true)

          val result = dep.renderTo(Format.scalaCli)

          printLine(result)
          Clipboard.put(result)

          printLine("Dependency copied to clipboard!")
          exit(0)

  object CliApp extends DefaultAwareCommandsEntryPoint:
    override def progName: String = "dep"

    override def description: String =
      s"""Dependency helper for Scala, companion to scala command.
         |Converts between dependency formats, searches for dependencies to use.
         |
         |Convert is the default command - pass a dependency string as argument and -t for the target format.
         |Example:
         |
         |$$ $progName -t repl '"com.company" %% "awesome-lib" % "1.2.3"'
         |--dep com.company::awesome-lib:1.2.3
         |""".stripMargin

    override def defaultCommand: Option[Command[?]] = Some(Convert)

    def commands = Seq(
      Convert,
      Search
    )

  def handleArgs(args: Seq[String]): Unit = CliApp.main(args.toArray)

end Cli
