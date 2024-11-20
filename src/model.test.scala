package ma.chinespirit.dep

import munit.FunSuite

class ModelTest extends FunSuite:

  given RenderContext = RenderContext()

  test("render sbt dependency") {
    val sbtDep = Dependency.Sbt(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      DependencyType.Scala(false),
      Scope.Main
    )

    assertEquals(
      sbtDep.render,
      """"org.scalatest" %% "scalatest" % "3.2.9""""
    )
  }

  test("render scala-cli dependency") {
    val scalaCliDep = Dependency.ScalaCli(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      DependencyType.Scala(false),
      Scope.Main
    )

    assertEquals(
      scalaCliDep.render,
      """//> using dep org.scalatest::scalatest:3.2.9"""
    )
  }

  test("render mill dependency") {
    val millDep = Dependency.Mill(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      DependencyType.Scala(false)
    )

    assertEquals(
      millDep.render,
      """ivy"org.scalatest::scalatest:3.2.9""""
    )
  }

  test("render gradle dependency") {
    val gradleDep = Dependency.Gradle(
      "org.scalatest",
      ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
      "3.2.9",
      Scope.Main
    )

    assertEquals(
      gradleDep.render,
      """implementation 'org.scalatest:scalatest_sjs1_3:3.2.9'"""
    )
  }

  test("render gradle dependency with no scala version with assumeScala") {
    given RenderContext = RenderContext(assumeScala = true)
    val gradleDep = Dependency.Gradle(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      Scope.Main
    )

    assertEquals(
      gradleDep.render,
      """implementation 'org.scalatest:scalatest_3:3.2.9'"""
    )
  }

  test("render gradle dependency with no scala version without assumeScala") {
    given RenderContext = RenderContext(assumeScala = false)
    val gradleDep = Dependency.Gradle(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      Scope.Main
    )

    assertEquals(
      gradleDep.render,
      """implementation 'org.scalatest:scalatest:3.2.9'"""
    )
  }

  test("render maven dependency") {
    val mavenDep = Dependency.Maven(
      "org.scalatest",
      ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
      "3.2.9",
      Scope.Test
    )

    assertEquals(
      mavenDep.render,
      """<dependency>
        |  <groupId>org.scalatest</groupId>
        |  <artifactId>scalatest_sjs1_3</artifactId>
        |  <version>3.2.9</version>
        |  <scope>test</scope>
        |</dependency>""".stripMargin
    )
  }

  test("render maven dependency with no scala version with assumeScala") {
    given RenderContext = RenderContext(assumeScala = true)

    val mavenDep = Dependency.Maven(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      Scope.Test
    )

    assertEquals(
      mavenDep.render,
      """<dependency>
        |  <groupId>org.scalatest</groupId>
        |  <artifactId>scalatest_3</artifactId>
        |  <version>3.2.9</version>
        |  <scope>test</scope>
        |</dependency>""".stripMargin
    )
  }

  test("render maven dependency with no scala version without assumeScala") {
    given RenderContext = RenderContext(assumeScala = false)

    val mavenDep = Dependency.Maven(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      Scope.Test
    )

    assertEquals(
      mavenDep.render,
      """<dependency>
          |  <groupId>org.scalatest</groupId>
          |  <artifactId>scalatest</artifactId>
          |  <version>3.2.9</version>
          |  <scope>test</scope>
          |</dependency>""".stripMargin
    )
  }

  test("translations from others to sbt") {
    val millDep = Dependency.Mill(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      DependencyType.ScalaCrossPlatform(true)
    )

    assertEquals(
      millDep.toSbt,
      Dependency.Sbt(
        "org.scalatest",
        ArtifactId("scalatest", None, None),
        "3.2.9",
        DependencyType.ScalaCrossPlatform(true),
        Scope.Main
      )
    )

    val scalaCliDep = Dependency.ScalaCli(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      DependencyType.ScalaCrossPlatform(true),
      Scope.Test
    )

    assertEquals(
      scalaCliDep.toSbt,
      Dependency.Sbt(
        "org.scalatest",
        ArtifactId("scalatest", None, None),
        "3.2.9",
        DependencyType.ScalaCrossPlatform(true),
        Scope.Test
      )
    )

    val gradleDep = Dependency.Gradle(
      "org.scalatest",
      ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
      "3.2.9",
      Scope.Test
    )

    assertEquals(
      gradleDep.toSbt,
      Dependency.Sbt(
        "org.scalatest",
        ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
        "3.2.9",
        DependencyType.ScalaCrossPlatform(false),
        Scope.Test
      )
    )
  }

  test("translations from others to scala-cli") {
    val millDep = Dependency.Mill(
      "org.scalatest",
      ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
      "3.2.9",
      DependencyType.ScalaCrossPlatform(true)
    )

    assertEquals(
      millDep.toScalaCli,
      Dependency.ScalaCli(
        "org.scalatest",
        ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
        "3.2.9",
        DependencyType.ScalaCrossPlatform(true),
        Scope.Main
      )
    )

    val sbtDep = Dependency.Sbt(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      DependencyType.Scala(false),
      Scope.Test
    )

    assertEquals(
      sbtDep.toScalaCli,
      Dependency.ScalaCli(
        "org.scalatest",
        ArtifactId("scalatest", None, None),
        "3.2.9",
        DependencyType.Scala(false),
        Scope.Test
      )
    )

    val gradleDep = Dependency.Gradle(
      "org.scalatest",
      ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
      "3.2.9",
      Scope.Test
    )

    assertEquals(
      gradleDep.toScalaCli,
      Dependency.ScalaCli(
        "org.scalatest",
        ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
        "3.2.9",
        DependencyType.ScalaCrossPlatform(false),
        Scope.Test
      )
    )
  }

  test("translations from others to mill") {
    val sbtDep = Dependency.Sbt(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      DependencyType.Scala(false),
      Scope.Test
    )

    assertEquals(
      sbtDep.toMill,
      Dependency.Mill(
        "org.scalatest",
        ArtifactId("scalatest", None, None),
        "3.2.9",
        DependencyType.Scala(false)
      )
    )

    val scalaCliDep = Dependency.ScalaCli(
      "org.scalatest",
      ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
      "3.2.9",
      DependencyType.Java,
      Scope.Test
    )

    assertEquals(
      scalaCliDep.toMill,
      Dependency.Mill(
        "org.scalatest",
        ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
        "3.2.9",
        DependencyType.Java
      )
    )

    val gradleDep = Dependency.Gradle(
      "org.scalatest",
      ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
      "3.2.9",
      Scope.Test
    )

    assertEquals(
      gradleDep.toMill,
      Dependency.Mill(
        "org.scalatest",
        ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
        "3.2.9",
        DependencyType.ScalaCrossPlatform(false)
      )
    )
  }

  test("translations from others to gradle") {
    val sbtDep = Dependency.Sbt(
      "org.scalatest",
      ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
      "3.2.9",
      DependencyType.Java,
      Scope.Test
    )

    assertEquals(
      sbtDep.toGradle,
      Dependency.Gradle(
        "org.scalatest",
        ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
        "3.2.9",
        Scope.Test
      )
    )

    val scalaCliDep = Dependency.ScalaCli(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      DependencyType.Scala(false),
      Scope.Test
    )

    assertEquals(
      scalaCliDep.toGradle,
      Dependency.Gradle(
        "org.scalatest",
        ArtifactId("scalatest", None, None),
        "3.2.9",
        Scope.Test
      )
    )

    val millDep = Dependency.Mill(
      "org.scalatest",
      ArtifactId("scalatest", None, None),
      "3.2.9",
      DependencyType.Scala(false)
    )

    assertEquals(
      millDep.toGradle,
      Dependency.Gradle(
        "org.scalatest",
        ArtifactId("scalatest", None, None),
        "3.2.9",
        Scope.Main
      )
    )
  }

  test("translations from others to maven") {
    val sbtDep = Dependency.Sbt(
      "org.scalatest",
      ArtifactId("scalatest", Some(CrossPlatform.Native("0.4")), Some(ScalaVersion("3"))),
      "3.2.9",
      DependencyType.Scala(false),
      Scope.Test
    )

    assertEquals(
      sbtDep.toMaven,
      Dependency.Maven(
        "org.scalatest",
        ArtifactId("scalatest", Some(CrossPlatform.Native("0.4")), Some(ScalaVersion("3"))),
        "3.2.9",
        Scope.Test
      )
    )

    val scalaCliDep = Dependency.ScalaCli(
      "org.scalatest",
      ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
      "3.2.9",
      DependencyType.Scala(false),
      Scope.Test
    )

    assertEquals(
      scalaCliDep.toMaven,
      Dependency.Maven(
        "org.scalatest",
        ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
        "3.2.9",
        Scope.Test
      )
    )

    val millDep = Dependency.Mill(
      "org.scalatest",
      ArtifactId("scalatest", Some(CrossPlatform.Native("0.4")), Some(ScalaVersion("3"))),
      "3.2.9",
      DependencyType.Scala(false)
    )

    assertEquals(
      millDep.toMaven,
      Dependency.Maven(
        "org.scalatest",
        ArtifactId("scalatest", Some(CrossPlatform.Native("0.4")), Some(ScalaVersion("3"))),
        "3.2.9",
        Scope.Main
      )
    )

    val gradleDep = Dependency.Gradle(
      "org.scalatest",
      ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
      "3.2.9",
      Scope.Test
    )

    assertEquals(
      gradleDep.toMaven,
      Dependency.Maven(
        "org.scalatest",
        ArtifactId("scalatest", Some(CrossPlatform.ScalaJS("1")), Some(ScalaVersion("3"))),
        "3.2.9",
        Scope.Test
      )
    )
  }

end ModelTest
