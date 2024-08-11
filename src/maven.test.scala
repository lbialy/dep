package ma.chinespirit.dep

import munit.*

class MavenDependencyParserTest extends FunSuite:

  test("parse maven dependency") {
    val input =
      """<dependency>
        |  <groupId>org.scala-lang</groupId>
        |  <artifactId>scala-library</artifactId>
        |  <version>2.13.8</version>
        |  <scope>compile</scope>
        |</dependency>""".stripMargin

    val expected = Dependency.Maven(
      "org.scala-lang",
      ArtifactId("scala-library", None, None),
      "2.13.8",
      Scope.Main
    )

    MavenDependencyParser.parse(input) match
      case Left(error)       => fail("unexpected error", error)
      case Right(dependency) => assertEquals(dependency, expected)
  }

  test("all") {
    val dependencies = Map(
      """<dependency>
        |  <groupId>org.scala-lang</groupId>
        |  <artifactId>scala-library</artifactId>
        |  <version>2.13.8</version>
        |</dependency>""" -> Dependency.Maven(
        "org.scala-lang",
        ArtifactId("scala-library", None, None),
        "2.13.8",
        Scope.Main
      ),
      """<dependency>
        |  <groupId>org.scala-lang</groupId>
        |  <artifactId>scala-library</artifactId>
        |  <version>2.13.8</version>
        |  <scope>test</scope>
        |</dependency>""" -> Dependency.Maven(
        "org.scala-lang",
        ArtifactId("scala-library", None, None),
        "2.13.8",
        Scope.Test
      ),
      """<dependency>
        |  <groupId>com.indoorvivants</groupId>
        |  <artifactId>decline-derive_3</artifactId>
        |  <version>0.2.0</version>
        |</dependency>""" -> Dependency.Maven(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, Some(ScalaVersion("3"))),
        "0.2.0",
        Scope.Main
      ),
      """<dependency>
        |  <groupId>com.indoorvivants</groupId>
        |  <artifactId>decline-derive_native0.4_3</artifactId>
        |  <version>0.2.0</version>
        |  <scope>test</scope>
        |</dependency>""" -> Dependency.Maven(
        "com.indoorvivants",
        ArtifactId("decline-derive", Some(CrossPlatform.Native("0.4")), Some(ScalaVersion("3"))),
        "0.2.0",
        Scope.Test
      ),
      """<dependency>
        |  <groupId>com.indoorvivants</groupId>
        |  <artifactId>decline-derive_sjs1_3</artifactId>
        |  <version>0.2.0</version>
        |</dependency>""" -> Dependency.Maven(
        "com.indoorvivants",
        ArtifactId("decline-derive", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
        "0.2.0",
        Scope.Main
      )
    )

    for (dep <- dependencies.keys) do
      MavenDependencyParser.parse(dep) match
        case Left(error)       => fail("unexpected error", error)
        case Right(dependency) => assertEquals(dependency, dependencies(dep), dep)
  }
end MavenDependencyParserTest
