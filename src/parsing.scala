package ma.chinespirit.dep

import fastparse.Parsed

case class ParsingFailure(parsed: Parsed.Failure, input: String) extends Exception(parsed.trace(true).longMsg):
  def render: String = s"Failed to parse '$input':\n${parsed.trace(true).longMsg}"

extension (input: String)
  def stripDoubleQuotes: String = input.stripPrefix("\"").stripSuffix("\"")
  def stripSingleQuotes: String = input.stripPrefix("'").stripSuffix("'")

object Parser:
  def fail(input: String): Either[ParsingFailure, Dependency] =
    Left(ParsingFailure(Parsed.Failure("Unknown format", 0, Parsed.Extra(input, 0, 0, _ => ???, Nil)), input))

  def inferFormatAndParse(input: String): Either[ParsingFailure, Dependency] =
    if input.replace(" ", "").contains("\"%") then SbtDependencyParser.parse(input)
    else if input.startsWith("<") then MavenDependencyParser.parse(input)
    else if input.contains("//>") || input.matches(".*(using)? ?(dep|lib).*") then ScalaCliDependencyParser.parse(input)
    else if input.startsWith("implementation") || input.startsWith("testImplementation") then GradleDependencyParser.parse(input)
    else if input.contains("ivy\"") then MillDependencyParser.parse(input)
    else if input.contains(":") then
      MillDependencyParser.parse(input).orElse(GradleDependencyParser.parse(input)).orElse(ScalaCliDependencyParser.parse(input))
    else fail(input)

  def parse(input: String): Either[ParsingFailure, Dependency] = inferFormatAndParse(input)
