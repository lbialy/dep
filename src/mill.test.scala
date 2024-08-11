package ma.chinespirit.dep

import munit.*

class MillDependencyParserTest extends FunSuite:
  test("parse all") {
    val dependencies = Map(
      // scala dep type
      """ivy"com.indoorvivants::decline-derive:0.2.0"""" -> Dependency.Mill(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        DependencyType.Scala(false)
      ),
      // scala dep type, specific scala version
      """ivy"com.indoorvivants:::decline-derive:0.2.0"""" -> Dependency.Mill(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        DependencyType.Scala(true)
      ),
      // scala cross-platform dep type
      """ivy"com.indoorvivants::decline-derive::0.2.0"""" -> Dependency.Mill(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        DependencyType.ScalaCrossPlatform(false)
      ),
      // scala cross-platform dep type, specific scala version
      """ivy"com.indoorvivants:::decline-derive::0.2.0"""" -> Dependency.Mill(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, None),
        "0.2.0",
        DependencyType.ScalaCrossPlatform(true)
      ),
      // java cross-platform dep type, specific scala version
      """ivy"com.indoorvivants:decline-derive_3::0.2.0"""" -> Dependency.Mill(
        "com.indoorvivants",
        ArtifactId("decline-derive", None, Some(ScalaVersion("3"))),
        "0.2.0",
        DependencyType.ScalaCrossPlatform(false)
      )
    )

    for (dep <- dependencies.keys) do
      MillDependencyParser.parse(dep) match
        case Right(parsedDep) => assertEquals(parsedDep, dependencies(dep), dep)
        case Left(err)        => fail(s"Failed to parse '${err.input}':\n$err")
  }
end MillDependencyParserTest
