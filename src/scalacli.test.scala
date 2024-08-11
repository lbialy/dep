package ma.chinespirit.dep

import munit.FunSuite

class ScalaCliDependencyParserTest extends FunSuite:

  test("parse all") {
    val dependencies = Map(
      // scala dep type
      """//> using dep "com.indoorvivants::decline-derive:0.2.0"""" -> Dependency.ScalaCli(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        DependencyType.Scala(false),
        Scope.Main
      ),
      // scala dep type, no double quotes
      """//> using dep com.indoorvivants::decline-derive:0.2.0""" -> Dependency.ScalaCli(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        DependencyType.Scala(false),
        Scope.Main
      ),
      // scala dep type, specific scala version
      """//> using dep "com.indoorvivants:::decline-derive:0.2.0"""" -> Dependency.ScalaCli(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        DependencyType.Scala(true),
        Scope.Main
      ),
      // scala dep type, specific scala version, no double quotes
      """//> using dep com.indoorvivants:::decline-derive:0.2.0""" -> Dependency.ScalaCli(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        DependencyType.Scala(true),
        Scope.Main
      ),
      // scala cross-platform dep type
      """//> using dep "com.indoorvivants::decline-derive::0.2.0"""" -> Dependency.ScalaCli(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        DependencyType.ScalaCrossPlatform(false),
        Scope.Main
      ),
      // scala cross-platform dep type, no double quotes
      """//> using test.dep com.indoorvivants::decline-derive::0.2.0""" -> Dependency.ScalaCli(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        DependencyType.ScalaCrossPlatform(false),
        Scope.Test
      ),
      // scala cross-platform dep type, specific scala version
      """//> using dep "com.indoorvivants:::decline-derive::0.2.0"""" -> Dependency.ScalaCli(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        DependencyType.ScalaCrossPlatform(true),
        Scope.Main
      ),
      // scala cross-platform dep type, specific scala version, no double quotes
      """//> using test.dep com.indoorvivants:::decline-derive::0.2.0""" -> Dependency.ScalaCli(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        DependencyType.ScalaCrossPlatform(true),
        Scope.Test
      ),
      // java cross-platform dep type, specific scala version
      """//> using dep "com.indoorvivants:decline-derive_3::0.2.0"""" -> Dependency.ScalaCli(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, Some(ScalaVersion("3"))),
        "0.2.0",
        DependencyType.ScalaCrossPlatform(false),
        Scope.Main
      ),
      // java cross-platform dep type, specific scala version, no double quotes
      """//> using test.dep com.indoorvivants:decline-derive_3::0.2.0""" -> Dependency.ScalaCli(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, Some(ScalaVersion("3"))),
        "0.2.0",
        DependencyType.ScalaCrossPlatform(false),
        Scope.Test
      ),
      // without //>
      """using dep "org.scalamacros:::paradise:2.1.1"""" -> Dependency.ScalaCli(
        "org.scalamacros",
        ArtifactId("paradise", None, None),
        "2.1.1",
        DependencyType.Scala(true),
        Scope.Main
      )
    )

    for (dep <- dependencies.keys) do
      ScalaCliDependencyParser.parse(dep) match
        case Right(parsedDep) => assertEquals(parsedDep, dependencies(dep), dep)
        case Left(err)        => fail(s"Failed to parse '${err.input}':\n$err")
  }

end ScalaCliDependencyParserTest
