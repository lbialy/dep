package ma.chinespirit.dep

import fastparse.*, SingleLineWhitespace.*

object ArtifactIdParser:
  // Helper parsers
  def identifier[$: P] = P(CharsWhile(c => c != '_' && c != '"' && c != '\'' && c != ':' && c != '%').!)
  def version[$: P]    = P(CharIn("0-9.").rep(1).!)

  // Parser for Scala version (e.g., 2.13, 3)
  def scalaVersion[$: P] = P(version.map(ScalaVersion.apply))

  // Parser for cross-platform version (e.g., native0.4, sjs1)
  def crossPlatform[$: P] = P(
    ("native" ~/ version).map(v => CrossPlatform.Native(v)) |
      ("sjs" ~/ version).map(v => CrossPlatform.ScalaJS(v))
  )

  // Main parser for artifact ID
  def artifactId[$: P] = P(
    identifier ~
      ("_" ~ crossPlatform).? ~
      ("_" ~ scalaVersion).?
  ).map { case (baseArtifact, crossPlatform, scalaVersion) =>
    ArtifactId(baseArtifact, crossPlatform, scalaVersion)
  }

  // Parse function
  def parse(input: String): Either[ParsingFailure, ArtifactId] =
    fastparse.parse(input, artifactId(using _)) match
      case Parsed.Success(result, _) => Right(result)
      case fail: Parsed.Failure      => Left(ParsingFailure(fail, input))
