package ma.chinespirit.dep

case class ScalaVersion(version: String):
  def isPrecise: Boolean = version.count(_ == '.') > 1

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
  def render(full: Boolean): String =
    val suffixes = s"${crossPlatform.map(_.render).map("_" + _).getOrElse("")}${scalaVersion.map(_.version).map("_" + _).getOrElse("")}"
    if full then s"${baseArtifact}$suffixes" else baseArtifact

  def preciseScalaVersion: Boolean = scalaVersion match
    case Some(scalaVersion) if scalaVersion.isPrecise => true
    case _                                            => false

enum DependencyType:
  case Java
  case Scala(preciseScalaVersion: Boolean)
  case ScalaCrossPlatform(preciseScalaVersion: Boolean)

enum Scope:
  case Main, Test

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

  def render: String = this match
    case Sbt(org, artifact, version, depType, scope) => // render sbt dependency string
      val percents = depType match
        case DependencyType.Java                  => "%"
        case DependencyType.Scala(_)              => "%%"
        case DependencyType.ScalaCrossPlatform(_) => "%%%"

      val shouldRenderFullArtifact = depType match
        case DependencyType.Scala(precise)              => precise
        case DependencyType.ScalaCrossPlatform(precise) => precise
        case _                                          => false

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
        case DependencyType.Scala(precise)              => precise
        case DependencyType.ScalaCrossPlatform(precise) => precise
        case _                                          => false

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
        case DependencyType.Scala(precise)              => precise
        case DependencyType.ScalaCrossPlatform(precise) => precise
        case _                                          => false

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

end Dependency
