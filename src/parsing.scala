package ma.chinespirit.dep

import fastparse.Parsed

sealed trait InferenceFailure:
  def render: String

case class CouldNotInferFormat(input: String) extends Exception(s"Could not infer the format of '$input'") with InferenceFailure:
  def render: String = getMessage()

case class ParsingFailure(parsed: Parsed.Failure, input: String) extends Exception(parsed.trace(true).longMsg) with InferenceFailure:
  def render: String = s"Failed to parse '$input':\n${getMessage}"

extension (input: String)
  def stripDoubleQuotes: String = input.stripPrefix("\"").stripSuffix("\"")
  def stripSingleQuotes: String = input.stripPrefix("'").stripSuffix("'")

object Parser:
  def couldNotInfer(input: String): Either[InferenceFailure, Dependency] =
    Left(CouldNotInferFormat(input))

  def inferFormatAndParse(input: String): Either[InferenceFailure, Dependency] =
    if input.replace(" ", "").contains("\"%") then SbtDependencyParser.parse(input)
    else if input.startsWith("<") then MavenDependencyParser.parse(input)
    else if input.contains("//>") || input.matches(".*(using )?(test\\.)?(dep|lib) .+") then ScalaCliDependencyParser.parse(input)
    else if input.startsWith("implementation") || input.startsWith("testImplementation") then GradleDependencyParser.parse(input)
    else if input.contains("ivy\"") then MillDependencyParser.parse(input)
    else if input.contains(":") then
      MillDependencyParser
        .parse(input)
        .orElse(GradleDependencyParser.parse(input))
        .orElse(ScalaCliDependencyParser.parse(input))
    else couldNotInfer(input)

  def parse(input: String, format: Format): Either[ParsingFailure, Dependency] =
    format match
      case Format.sbt      => SbtDependencyParser.parse(input)
      case Format.scalaCli => ScalaCliDependencyParser.parse(input)
      case Format.mill     => MillDependencyParser.parse(input)
      case Format.gradle   => GradleDependencyParser.parse(input)
      case Format.maven    => MavenDependencyParser.parse(input)
      case Format.repl     => MillDependencyParser.parse(input)
end Parser
