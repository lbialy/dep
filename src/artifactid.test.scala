package ma.chinespirit.dep

import munit.FunSuite

class ArtifactIdParserTest extends FunSuite:

  test("parse all") {
    val artifactIds = Map(
      "scalatest" -> ArtifactId("scalatest", None, None),
      "scalatest_2.13" -> ArtifactId("scalatest", None, Some(ScalaVersion("2.13"))),
      "utest_native0.4_3" -> ArtifactId("utest", Some(CrossPlatform.Native("0.4")), Some(ScalaVersion("3"))),
      "cats-effect_sjs1_2.13" -> ArtifactId("cats-effect", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("2.13"))),
      "zio_3" -> ArtifactId("zio", None, Some(ScalaVersion("3")))
    )

    for (id <- artifactIds.keys) do
      ArtifactIdParser.parse(id) match
        case Right(parsedId) => assertEquals(parsedId, artifactIds(id))
        case Left(err)       => fail(s"Failed to parse:\n$err")
  }
