package ma.chinespirit.dep

import scala.io.Source.stdin

def readFromStdin: String =
  println("Paste your dependency here and press enter twice:")
  stdin.getLines().takeWhile(_ != "").mkString("\n")

@main def run(args: String*): Unit =
  Clipboard.setupClipboard()

  val (flagsAndOptions, remaining) = Cli.parseFlagsAndOptions(args)

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

      println(result)
      Clipboard.put(result)

      println("Dependency copied to clipboard!")
      sys.exit(0)
    case Left(err) =>
      println(err.render)
      sys.exit(1)

end run
