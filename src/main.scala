package ma.chinespirit.dep

@main def run(args: String*): Unit =
  System.setProperty("testfx.robot", "glass")
  System.setProperty("testfx.headless", "true")
  System.setProperty("prism.order", "sw")
  System.setProperty("prism.text", "t2k")

  if args.isEmpty then // this should handle target format flags
    println("Paste your dependency here and press enter twice:")
    val input = scala.io.Source.stdin.getLines().takeWhile(_ != "").mkString("\n")

    Parser.parse(input) match
      case Right(parsedDep) =>
        val result = parsedDep.toScalaCli.render
        println(result)
        Clipboard.put(result)
        println("Dependency copied to clipboard")
        sys.exit(0)
      case Left(err) => // stdin is not a valid dep
        println(err.render)
        sys.exit(1)
  else
    Dep.parse(args*) match
      case Right(dep) =>
        dep match
          case Dep.Web => println("starting web interface")
      case Left(help) =>
        Parser.parse(args.mkString(" ")) match
          case Right(parsedDep) =>
            println(parsedDep.toScalaCli.render)
          case Left(err) =>
            println(help.toString)
            sys.exit(1)
