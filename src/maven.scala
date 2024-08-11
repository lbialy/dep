package ma.chinespirit.dep

import scala.xml.*
import scala.util.Try
import fastparse.Parsed

object MavenDependencyParser:
  def parse(xmlString: String): Either[ParsingFailure, Dependency] =
    Try {
      val xml           = XML.loadString(xmlString)
      val groupId       = (xml \ "groupId").text
      val artifactIdStr = (xml \ "artifactId").text
      val version       = (xml \ "version").text
      val scope = (xml \ "scope").headOption.map(_.text) match
        case None         => Scope.Main
        case Some("test") => Scope.Test
        case Some(_)      => Scope.Main

      val artifactId = ArtifactIdParser.parse(artifactIdStr) match
        case Right(parsed) => parsed
        case Left(failure) => throw failure

      Dependency.Maven(groupId, artifactId, version, scope)
    }.toEither.left.map {
      case ex: ParsingFailure => ex
      case ex                 => ParsingFailure(Parsed.Failure(ex.getMessage, 0, Parsed.Extra(xmlString, 0, 0, identity, Nil)), xmlString)
    }
