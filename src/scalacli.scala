package ma.chinespirit.dep

import fastparse.*, SingleLineWhitespace.*

object ScalaCliDependencyParser extends MillDependencyFormat:
  // do not capture //> and using
  def directive[$: P] = P("//>".? ~/ "using".? ~/ ("test.".? ~/ ("dep" | "lib")).!).map {
    case "dep"      => Scope.Main
    case "test.dep" => Scope.Test
  }

  def scalaCliDep[$: P] = P(
    directive.? ~ "\"".? ~ millDepString ~ "\"".?
  ).map {
    case (Some(scope), dep) => (dep, scope)
    case (None, dep)        => (dep, Scope.Main) // default to Main scope if not specified
  }

  // Parse function
  def parse(input: String): Either[ParsingFailure, Dependency.ScalaCli] =
    fastparse.parse(input.trim, scalaCliDep(using _)) match
      case Parsed.Success(((org, artifact, version, depType), scope), _) =>
        Right(Dependency.ScalaCli(org, artifact, version, depType, scope))
      case fail: Parsed.Failure => Left(ParsingFailure(fail, input))
