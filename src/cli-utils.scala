package ma.chinespirit.dep

import caseapp.*
import caseapp.core.app.PlatformUtil
import caseapp.core.commandparser.RuntimeCommandParser

trait DefaultAwareCommandsEntryPoint extends CommandsEntryPoint:

  private def commandProgName(commandName: List[String]): String =
    (progName +: commandName).mkString(" ")

  private val helpFlags = Set("-h", "--help", "-help", "--help-full")

  override def main(args: Array[String]): Unit =
    val actualArgs = PlatformUtil.arguments(args)

    if enableCompleteCommand && actualArgs.startsWith(completeCommandName.toArray[String]) then
      completeMain(actualArgs.drop(completeCommandName.length))
    else
      val completionAliasOpt =
        if enableCompletionsCommand then completionsCommandAliases.find(actualArgs.startsWith(_))
        else None

      completionAliasOpt match
        case Some(completionAlias) =>
          completionsMain(actualArgs.drop(completionAlias.length))
        case None =>
          defaultCommand match
            case None =>
              RuntimeCommandParser.parse(commands, actualArgs.toList) match {
                case None =>
                  printUsage()
                case Some((commandName, command, commandArgs)) =>
                  command.main(commandProgName(commandName), commandArgs.toArray)
              }
            case Some(defaultCommand0) =>
              val (commandName, command, commandArgs) =
                RuntimeCommandParser.parse(defaultCommand0, commands, actualArgs.toList)

              if commandName.isEmpty && helpFlags.exists(commandArgs.contains) then printUsage()
              else command.main(commandProgName(commandName), commandArgs.toArray)
