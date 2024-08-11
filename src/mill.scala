package ma.chinespirit.dep

import fastparse.*, NoWhitespace.*

trait MillDependencyFormat:

  def organization[$: P] = P(CharsWhile(_ != ':').!)
  def version[$: P]      = P(CharsWhile(_ != '"').!)

  def javaArtifactId[$: P]  = P(ArtifactIdParser.artifactId)
  def scalaArtifactId[$: P] = P(CharsWhile(_ != ':').!)

  def javaDep[$: P] = P(
    organization ~ ":" ~ javaArtifactId ~ ":" ~ version
  ).map { case (org, artifact, version) =>
    (org, artifact, version, DependencyType.Java)
  }

  def javaCrossDep[$: P] = P(
    organization ~ ":" ~ javaArtifactId ~ "::" ~ version
  ).map { case (org, artifact, version) =>
    (org, artifact, version, DependencyType.ScalaCrossPlatform(artifact.preciseScalaVersion))
  }

  def scalaDep[$: P] = P(
    organization ~ "::" ~~ ":".!.?.map(_.isDefined) ~ scalaArtifactId ~ ":" ~ version
  ).map { case (org, preciseScalaVersion, artifact, version) =>
    (org, ArtifactId(artifact, None, None), version, DependencyType.Scala(preciseScalaVersion))
  }

  def scalaCrossDep[$: P] = P(
    organization ~ "::" ~~ ":".!.?.map(_.isDefined) ~ scalaArtifactId ~ "::" ~ version
  ).map { case (org, preciseScalaVersion, artifact, version) =>
    (org, ArtifactId(artifact, None, None), version, DependencyType.ScalaCrossPlatform(preciseScalaVersion))
  }

  def millDepString[$: P] = P(
    (javaCrossDep | javaDep | scalaCrossDep | scalaDep)
  )

object MillDependencyParser extends MillDependencyFormat:

  def ivyPrefix[$: P] = P("ivy".? ~ "\"".?)
  def ivySuffix[$: P] = P("\"".?)

  def dependency[$: P] = P( // order is important here to avoid parsing first colon as part of the version
    ivyPrefix ~ millDepString ~ ivySuffix
  )

  // Parse function
  def parse(input: String): Either[ParsingFailure, Dependency.Mill] =
    fastparse.parse(input.trim, dependency(using _)) match
      case Parsed.Success((org, artifact, version, depType), _) => Right(Dependency.Mill(org, artifact, version, depType))
      case failure: Parsed.Failure                              => Left(ParsingFailure(failure, input))
end MillDependencyParser
