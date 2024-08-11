package ma.chinespirit.dep

import fastparse.*, SingleLineWhitespace.*

object SbtDependencyParser:

  // utility, parser for quoted strings
  def quoted[$: P] = P("\"" ~ CharsWhile(_ != '"').rep.! ~ "\"")

  // Parsers for different parts of the dependency
  def organization[$: P]    = P(quoted)
  def plainArtifactId[$: P] = P(quoted.!.map(str => ArtifactId(str.stripDoubleQuotes, None, None)))
  def artifactId[$: P]      = P("\"" ~ ArtifactIdParser.artifactId ~ "\"")

  def version[$: P] = P(quoted)
  def scope[$: P]   = P(("%" ~ ("\"test\"" | "Test")).!.?.map(_.isDefined))

  // Parsers for different dependency types
  def javaDep[$: P]          = P(organization ~ "%" ~ artifactId ~ "%" ~ version)
  def scalaDep[$: P]         = P(organization ~ "%%" ~ plainArtifactId ~ "%" ~ version)
  def crossPlatformDep[$: P] = P(organization ~ "%%%" ~ plainArtifactId ~ "%" ~ version)

  // Main parser
  def dependency[$: P] = P(
    (javaDep.map { case (org, artifact, version) => (org, artifact, version, DependencyType.Java) } |
      scalaDep.map { case (org, artifact, version) => (org, artifact, version, DependencyType.Scala(artifact.preciseScalaVersion)) } |
      crossPlatformDep.map { case (org, artifact, version) =>
        (org, artifact, version, DependencyType.ScalaCrossPlatform(artifact.preciseScalaVersion))
      }) ~
      scope
  )

  // Parse function
  def parse(input: String): Either[ParsingFailure, Dependency.Sbt] =
    fastparse.parse(input, dependency(using _)) match
      case Parsed.Success((org, artifact, version, depType, isTestDep), _) =>
        Right(Dependency.Sbt(org, artifact, version, depType, if (isTestDep) Scope.Test else Scope.Main))
      case fail: Parsed.Failure => Left(ParsingFailure(fail, input))
