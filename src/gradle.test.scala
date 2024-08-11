package ma.chinespirit.dep

import munit.*

class GradleDependencyParserTest extends FunSuite:

  test("parse all") {
    val dependencies = Map(
      // gradle dep type, groovy single quotes
      """implementation 'com.indoorvivants:decline-derive_3:0.2.0'""" -> Dependency.Gradle(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, Some(ScalaVersion("3"))),
        "0.2.0",
        Scope.Main
      ),
      // gradle dep type, groovy double quotes
      """testImplementation "com.indoorvivants:decline-derive_3:0.2.0"""" -> Dependency.Gradle(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, Some(ScalaVersion("3"))),
        "0.2.0",
        Scope.Test
      ),
      // gradle dep type, groovy single quotes, no scala version
      """implementation 'com.indoorvivants:decline-derive:0.2.0'""" -> Dependency.Gradle(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        Scope.Main
      ),
      // gradle dep type, groovy double quotes, scala cross-platform + version
      """testImplementation "com.indoorvivants:decline-derive_sjs1_3:0.2.0"""" -> Dependency.Gradle(
        "com.indoorvivants",
        ArtifactId("decline-derive", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
        "0.2.0",
        Scope.Test
      ),
      // gradle dep type, kotlin function call, no scala version
      """implementation("com.indoorvivants:decline-derive:0.2.0")""" -> Dependency.Gradle(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        Scope.Main
      ),
      // gradle dep type, kotlin function call, scala cross-platform + version
      """testImplementation("com.indoorvivants:decline-derive_sjs1_3:0.2.0")""" -> Dependency.Gradle(
        "com.indoorvivants",
        ArtifactId("decline-derive", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
        "0.2.0",
        Scope.Test
      )
    )

    for (dep <- dependencies.keys) do
      GradleDependencyParser.parse(dep) match
        case Right(parsedDep) => assertEquals(parsedDep, dependencies(dep), dep)
        case Left(err)        => fail(s"Failed to parse '${err.input}':\n$err")
  }
end GradleDependencyParserTest
