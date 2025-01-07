package ma.chinespirit.dep

@main def run(args: String*): Unit =
  Clipboard.setupClipboard()
  Cli.handleArgs(args)

end run
