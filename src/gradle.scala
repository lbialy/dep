package ma.chinespirit.dep

import fastparse.*, SingleLineWhitespace.*

object GradleDependencyParser:
  def configuration[$: P] = P(
    ("testImplementation" | "implementation").!
  ).map {
    case "testImplementation" => Scope.Test
    case "implementation"     => Scope.Main
  }

  def group[$: P] = P(CharsWhile(_ != ':').!)

  def version[$: P] = P(CharsWhile(c => c != '\'' && c != '"').!)

  def dependency[$: P] = P(
    configuration ~ "(".? ~ ("'" | "\"") ~ group ~ ":" ~ ArtifactIdParser.artifactId ~ ":" ~ version ~ ("'" | "\"") ~ ")".?
  ).map { case (scope, group, artifactId, version) =>
    (group, artifactId, version, scope)
  }

  // Parse function
  def parse(input: String): Either[ParsingFailure, Dependency.Gradle] =
    fastparse.parse(input.trim, dependency(using _)) match
      case Parsed.Success((group, artifactId, version, scope), _) => Right(Dependency.Gradle(group, artifactId, version, scope))
      case failure: Parsed.Failure                                => Left(ParsingFailure(failure, input))
