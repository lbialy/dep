package ma.chinespirit.dep

import munit.*

class SbtDependencyParserTest extends FunSuite:

  test("parse all") {
    val dependencies = Map(
      """"com.softwaremill.sttp.openai" %% "core" % "0.2.0"""" -> Dependency.Sbt(
        "com.softwaremill.sttp.openai",
        ArtifactId("core", None, None),
        "0.2.0",
        DependencyType.Scala(false),
        Scope.Main
      ),
      """"com.softwaremill.sttp.openai" % "core" % "0.2.0"""" -> Dependency.Sbt(
        "com.softwaremill.sttp.openai",
        ArtifactId("core", None, None),
        "0.2.0",
        DependencyType.Java,
        Scope.Main
      ),
      """"com.softwaremill.sttp.openai" %%% "core" % "0.2.0" % "test"""" -> Dependency.Sbt(
        "com.softwaremill.sttp.openai",
        ArtifactId("core", None, None),
        "0.2.0",
        DependencyType.ScalaCrossPlatform(false),
        Scope.Test
      ),
      """"org.scalatest" %% "scalatest" % "3.2.9" % Test""" -> Dependency.Sbt(
        "org.scalatest",
        ArtifactId("scalatest", None, None),
        "3.2.9",
        DependencyType.Scala(false),
        Scope.Test
      ),
      """"org.scalatest" %% "scalatest" % "3.2.9" % "test"""" -> Dependency.Sbt(
        "org.scalatest",
        ArtifactId("scalatest", None, None),
        "3.2.9",
        DependencyType.Scala(false),
        Scope.Test
      ),
      """"org.scalatest"%"scalatest"%"3.2.9"%"test"""" -> Dependency.Sbt(
        "org.scalatest",
        ArtifactId("scalatest", None, None),
        "3.2.9",
        DependencyType.Java,
        Scope.Test
      ),
      """"org.scalatest" % "scalatest_3" % "3.2.9" % "test" classifier "sources"""" -> Dependency.Sbt(
        "org.scalatest",
        ArtifactId("scalatest", None, Some(ScalaVersion("3"))),
        "3.2.9",
        DependencyType.Java,
        Scope.Test
      ),
      """"org.scalatest" % "scalatest_native0.4_3" % "3.2.9" % "test" classifier "sources"""" -> Dependency.Sbt(
        "org.scalatest",
        ArtifactId("scalatest", Some(CrossPlatform.Native("0.4")), Some(ScalaVersion("3"))),
        "3.2.9",
        DependencyType.Java,
        Scope.Test
      )
    )

    for (dep <- dependencies.keys) do
      SbtDependencyParser.parse(dep) match
        case Right(parsedDep) => assertEquals(parsedDep, dependencies(dep), dep)
        case Left(err)        => fail(s"Failed to parse '${err.input}':\n$err")
  }
end SbtDependencyParserTest
