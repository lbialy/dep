package ma.chinespirit.dep

import scala.collection.SortedMap

case class RenderContext(assumeScala3: String | Boolean = true)

case class ScalaVersion(version: String):
  def isPrecise: Boolean = version.count(_ == '.') > 1

  def toSuffix: Option[ScalaVersionSuffix] =
    if isPrecise then ScalaVersionSuffix.of(version.split('.').take(2).mkString("."))
    else ScalaVersionSuffix.of(version)

enum ScalaVersionSuffix:
  case `3`, `2.13`, `2.12`, `2.11`, `2.10`
object ScalaVersionSuffix:
  def of(s: String): Option[ScalaVersionSuffix] = values.find(_.toString().toLowerCase == s.toLowerCase)

enum CrossPlatform:
  case Native(version: String)
  case ScalaJS(version: String)

  def render: String = this match
    case Native(version)  => s"native$version"
    case ScalaJS(version) => s"sjs$version"

case class ArtifactId(
  baseArtifact: String,
  crossPlatform: Option[CrossPlatform],
  scalaVersion: Option[ScalaVersion]
):
  def render(full: Boolean)(using ctx: RenderContext): String =
    def suffixes =
      val assumedScala = ctx.assumeScala3 match
        case true  => s"_3"
        case false => s""
        case str   => s"_$str"

      val renderedPlatform = crossPlatform.map(_.render).map("_" + _).getOrElse("")

      val renderedScalaVersion = scalaVersion.map(_.version).map("_" + _).getOrElse(assumedScala)

      s"$renderedPlatform$renderedScalaVersion"

    if full then s"$baseArtifact$suffixes" else baseArtifact

  def preciseScalaVersion: Boolean = scalaVersion match
    case Some(scalaVersion) if scalaVersion.isPrecise => true
    case _                                            => false

object ArtifactId:
  def parse(input: String): Either[Throwable, ArtifactId] =
    ArtifactIdParser.parse(input)

enum DependencyType:
  case Java
  case Scala(preciseScalaVersion: Boolean)
  case ScalaCrossPlatform(preciseScalaVersion: Boolean)

enum Scope:
  case Main, Test

enum Format:
  case sbt, scalaCli, mill, gradle, maven, repl

object Format:
  val mapped: Map[String, Format] = Map(
    "sbt" -> sbt,
    "scala-cli" -> scalaCli,
    "scala" -> scalaCli,
    "mill" -> mill,
    "amm" -> mill,
    "ammonite" -> mill,
    "gradle" -> gradle,
    "maven" -> maven,
    "mvn" -> maven,
    "m2" -> maven,
    "pom" -> maven,
    "repl" -> repl
  )

  inline def of(str: String): Option[Format] = mapped.get(str)

  val byKind: Map[Format, Vector[String]] =
    mapped.groupBy { case (_, v) => v }.view.mapValues(_.keys.toVector.sortBy(-_.size)).toMap

  val validIdentifiers: Vector[String] = mapped.keys.toVector.sorted

  def validFormats: String =
    // reverse lexicographical order because scala-cli, sbt, repl, mill, maven, gradle
    // is also the order of popularity of the formats in scala community
    // (and I'm shilling for scala-cli like a mad man so I'm putting it at the top)
    val reverseOrdering = summon[Ordering[String]].reverse
    val keys            = values.map(_.productPrefix).sorted(using reverseOrdering)

    val message = keys.foldLeft(Vector("available formats (with identifiers to use) are:")) { case (lines, key) =>
      val validEntries = byKind.get(Format.valueOf(key)).getOrElse(Vector.empty)
      val keyName      = key.padTo(keys.maxBy(_.length()).length() + 1, ' ').capitalize

      lines :+ s"  ${keyName}-> ${validEntries.mkString("", ", ", "")}"
    }

    message.mkString(System.lineSeparator())

extension (scalaCli: Dependency.ScalaCli)
  def renderRepl(using RenderContext): String =
    val renderedDirective = scalaCli.render
    val depPosition       = renderedDirective.indexOf("dep")
    renderedDirective.substring(depPosition, renderedDirective.size).prependedAll("--")

enum Dependency:
  case Sbt(organization: String, artifactId: ArtifactId, version: String, dependencyType: DependencyType, scope: Scope)
  case ScalaCli(organization: String, artifactId: ArtifactId, version: String, dependencyType: DependencyType, scope: Scope)
  case Mill(organization: String, artifactId: ArtifactId, version: String, dependencyType: DependencyType)
  case Gradle(group: String, artifactId: ArtifactId, version: String, scope: Scope)
  case Maven(groupId: String, artifactId: ArtifactId, version: String, scope: Scope)

  def toSbt: Dependency.Sbt = this match
    case sbt: Sbt => sbt
    case ScalaCli(org, artifact, version, depType, scope) =>
      Dependency.Sbt(org, artifact, version, depType, scope)
    case Mill(org, artifact, version, depType) =>
      Dependency.Sbt(org, artifact, version, depType, Scope.Main)
    case Gradle(group, artifact, version, scope) =>
      artifact match
        case ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)) =>
          Dependency.Sbt(
            group,
            ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)),
            version,
            DependencyType.ScalaCrossPlatform(artifact.preciseScalaVersion),
            scope
          )

        case ArtifactId(baseArtifact, None, Some(scalaVersion)) =>
          Dependency.Sbt(
            group,
            ArtifactId(baseArtifact, None, Some(scalaVersion)),
            version,
            DependencyType.Scala(artifact.preciseScalaVersion),
            scope
          )

        case ArtifactId(baseArtifact, Some(crossVersion), None) =>
          Dependency.Sbt(
            group,
            ArtifactId(baseArtifact, Some(crossVersion), None),
            version,
            DependencyType.ScalaCrossPlatform(artifact.preciseScalaVersion),
            scope
          )

        case ArtifactId(baseArtifact, None, None) =>
          Dependency.Sbt(group, ArtifactId(baseArtifact, None, None), version, DependencyType.Java, scope)

    case Maven(groupId, artifactId, version, scope) =>
      artifactId match
        case ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)) =>
          Dependency.Sbt(
            groupId,
            ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)),
            version,
            DependencyType.ScalaCrossPlatform(artifactId.preciseScalaVersion),
            scope
          )
        case ArtifactId(baseArtifact, None, Some(scalaVersion)) =>
          Dependency.Sbt(
            groupId,
            ArtifactId(baseArtifact, None, Some(scalaVersion)),
            version,
            DependencyType.Scala(artifactId.preciseScalaVersion),
            scope
          )
        case ArtifactId(baseArtifact, Some(crossVersion), None) =>
          Dependency.Sbt(
            groupId,
            ArtifactId(baseArtifact, Some(crossVersion), None),
            version,
            DependencyType.ScalaCrossPlatform(artifactId.preciseScalaVersion),
            scope
          )
        case ArtifactId(baseArtifact, None, None) =>
          Dependency.Sbt(groupId, ArtifactId(baseArtifact, None, None), version, DependencyType.Java, scope)

  def toScalaCli: Dependency.ScalaCli = this match
    case scalaCli: ScalaCli => scalaCli
    case Sbt(org, artifact, version, depType, scope) =>
      Dependency.ScalaCli(org, artifact, version, depType, scope)
    case Mill(org, artifact, version, depType) =>
      Dependency.ScalaCli(org, artifact, version, depType, Scope.Main)
    case Gradle(group, artifact, version, scope) =>
      artifact match
        case ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)) =>
          Dependency.ScalaCli(
            group,
            ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)),
            version,
            DependencyType.ScalaCrossPlatform(artifact.preciseScalaVersion),
            scope
          )

        case ArtifactId(baseArtifact, None, Some(scalaVersion)) =>
          Dependency.ScalaCli(
            group,
            ArtifactId(baseArtifact, None, Some(scalaVersion)),
            version,
            DependencyType.Scala(artifact.preciseScalaVersion),
            scope
          )

        case ArtifactId(baseArtifact, Some(crossVersion), None) =>
          Dependency.ScalaCli(
            group,
            ArtifactId(baseArtifact, Some(crossVersion), None),
            version,
            DependencyType.ScalaCrossPlatform(artifact.preciseScalaVersion),
            scope
          )

        case ArtifactId(baseArtifact, None, None) =>
          Dependency.ScalaCli(group, ArtifactId(baseArtifact, None, None), version, DependencyType.Java, scope)

    case Maven(groupId, artifactId, version, scope) =>
      artifactId match
        case ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)) =>
          Dependency.ScalaCli(
            groupId,
            ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)),
            version,
            DependencyType.ScalaCrossPlatform(artifactId.preciseScalaVersion),
            scope
          )
        case ArtifactId(baseArtifact, None, Some(scalaVersion)) =>
          Dependency.ScalaCli(
            groupId,
            ArtifactId(baseArtifact, None, Some(scalaVersion)),
            version,
            DependencyType.Scala(artifactId.preciseScalaVersion),
            scope
          )
        case ArtifactId(baseArtifact, Some(crossVersion), None) =>
          Dependency.ScalaCli(
            groupId,
            ArtifactId(baseArtifact, Some(crossVersion), None),
            version,
            DependencyType.ScalaCrossPlatform(artifactId.preciseScalaVersion),
            scope
          )
        case ArtifactId(baseArtifact, None, None) =>
          Dependency.ScalaCli(groupId, ArtifactId(baseArtifact, None, None), version, DependencyType.Java, scope)

  def toMill: Dependency.Mill = this match
    case mill: Mill => mill
    case Sbt(org, artifact, version, depType, scope) =>
      Dependency.Mill(org, artifact, version, depType)
    case ScalaCli(org, artifact, version, depType, scope) =>
      Dependency.Mill(org, artifact, version, depType)
    case Gradle(group, artifact, version, scope) =>
      artifact match
        case ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)) =>
          Dependency.Mill(
            group,
            ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)),
            version,
            DependencyType.ScalaCrossPlatform(artifact.preciseScalaVersion)
          )

        case ArtifactId(baseArtifact, None, Some(scalaVersion)) =>
          Dependency.Mill(
            group,
            ArtifactId(baseArtifact, None, Some(scalaVersion)),
            version,
            DependencyType.Scala(artifact.preciseScalaVersion)
          )

        case ArtifactId(baseArtifact, Some(crossVersion), None) =>
          Dependency.Mill(
            group,
            ArtifactId(baseArtifact, Some(crossVersion), None),
            version,
            DependencyType.ScalaCrossPlatform(artifact.preciseScalaVersion)
          )

        case ArtifactId(baseArtifact, None, None) =>
          Dependency.Mill(group, ArtifactId(baseArtifact, None, None), version, DependencyType.Java)

    case Maven(groupId, artifactId, version, scope) =>
      artifactId match
        case ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)) =>
          Dependency.Mill(
            groupId,
            ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)),
            version,
            DependencyType.ScalaCrossPlatform(artifactId.preciseScalaVersion)
          )
        case ArtifactId(baseArtifact, None, Some(scalaVersion)) =>
          Dependency.Mill(
            groupId,
            ArtifactId(baseArtifact, None, Some(scalaVersion)),
            version,
            DependencyType.Scala(artifactId.preciseScalaVersion)
          )
        case ArtifactId(baseArtifact, Some(crossVersion), None) =>
          Dependency.Mill(
            groupId,
            ArtifactId(baseArtifact, Some(crossVersion), None),
            version,
            DependencyType.ScalaCrossPlatform(artifactId.preciseScalaVersion)
          )
        case ArtifactId(baseArtifact, None, None) =>
          Dependency.Mill(groupId, ArtifactId(baseArtifact, None, None), version, DependencyType.Java)

  def toGradle: Dependency.Gradle = this match
    case gradle: Gradle => gradle
    case Sbt(org, artifact, version, depType, scope) =>
      Dependency.Gradle(org, artifact, version, scope)
    case ScalaCli(org, artifact, version, depType, scope) =>
      Dependency.Gradle(org, artifact, version, scope)
    case Mill(org, artifact, version, depType) =>
      Dependency.Gradle(org, artifact, version, Scope.Main)
    case Maven(groupId, artifactId, version, scope) =>
      Dependency.Gradle(groupId, artifactId, version, scope)

  def toMaven: Dependency.Maven = this match
    case maven: Maven => maven
    case Sbt(org, artifact, version, depType, scope) =>
      Dependency.Maven(org, artifact, version, scope)
    case ScalaCli(org, artifact, version, depType, scope) =>
      Dependency.Maven(org, artifact, version, scope)
    case Mill(org, artifact, version, depType) =>
      Dependency.Maven(org, artifact, version, Scope.Main)
    case Gradle(groupId, artifactId, version, scope) =>
      artifactId match
        case ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)) =>
          Dependency.Maven(
            groupId,
            ArtifactId(baseArtifact, Some(crossVersion), Some(scalaVersion)),
            version,
            scope
          )
        case ArtifactId(baseArtifact, None, Some(scalaVersion)) =>
          Dependency.Maven(
            groupId,
            ArtifactId(baseArtifact, None, Some(scalaVersion)),
            version,
            scope
          )
        case ArtifactId(baseArtifact, Some(crossVersion), None) =>
          Dependency.Maven(
            groupId,
            ArtifactId(baseArtifact, Some(crossVersion), None),
            version,
            scope
          )
        case ArtifactId(baseArtifact, None, None) =>
          Dependency.Maven(groupId, ArtifactId(baseArtifact, None, None), version, scope)

  def render(using ctx: RenderContext): String = this match
    case Sbt(org, artifact, version, depType, scope) => // render sbt dependency string
      val percents = depType match
        case DependencyType.Java                  => "%"
        case DependencyType.Scala(_)              => "%%"
        case DependencyType.ScalaCrossPlatform(_) => "%%%"

      val shouldRenderFullArtifact = depType match
        case DependencyType.Java => true
        case _                   => false

      s""""$org" $percents "${artifact.render(shouldRenderFullArtifact)}" % "$version""""

    case ScalaCli(org, artifact, version, depType, scope) => // render scala-cli dependency string
      val scopeDirective = scope match
        case Scope.Main => ""
        case Scope.Test => "test."

      val firstColons = depType match
        case DependencyType.Java                        => ":"
        case DependencyType.Scala(precise)              => if precise then ":::" else "::"
        case DependencyType.ScalaCrossPlatform(precise) => if precise then ":::" else "::"

      val secondColons = depType match
        case DependencyType.Scala(_) | DependencyType.Java => ":"
        case DependencyType.ScalaCrossPlatform(_)          => "::"

      val shouldRenderFullArtifact = depType match
        case DependencyType.Java => true
        case _                   => false

      s"//> using ${scopeDirective}dep $org$firstColons${artifact.render(shouldRenderFullArtifact)}$secondColons$version"

    case Mill(org, artifact, version, depType) => // render mill dependency string
      val firstColons = depType match
        case DependencyType.Java                        => ":"
        case DependencyType.Scala(precise)              => if precise then ":::" else "::"
        case DependencyType.ScalaCrossPlatform(precise) => if precise then ":::" else "::"

      val secondColons = depType match
        case DependencyType.Scala(_) | DependencyType.Java => ":"
        case DependencyType.ScalaCrossPlatform(_)          => "::"

      val shouldRenderFullArtifact = depType match
        case DependencyType.Java => true
        case _                   => false

      s"""ivy"$org$firstColons${artifact.render(shouldRenderFullArtifact)}$secondColons$version""""

    case Gradle(group, artifact, version, scope) => // render gradle dependency string
      val scopeDirective = scope match
        case Scope.Main => "implementation"
        case Scope.Test => "testImplementation"

      s"""$scopeDirective '$group:${artifact.render(true)}:$version'"""

    case Maven(groupId, artifactId, version, scope) => // render maven dependency string
      import scala.xml.*

      val scopeDirective = scope match
        case Scope.Main => ""
        case Scope.Test => "test"

      val pomXml =
        <dependency>
          <groupId>{groupId}</groupId>
          <artifactId>{artifactId.render(true)}</artifactId>
          <version>{version}</version>
          {
          scope match
            case Scope.Main => <scope>compile</scope>
            case Scope.Test => <scope>test</scope>
        }
        </dependency>

      scala.xml.PrettyPrinter(120, 2).format(pomXml)

  def renderTo(format: Format)(using RenderContext): String =
    format match
      case Format.sbt      => toSbt.render
      case Format.scalaCli => toScalaCli.render
      case Format.mill     => toMill.render
      case Format.gradle   => toGradle.render
      case Format.maven    => toMaven.render
      case Format.repl     => toScalaCli.renderRepl

end Dependency
