package ma.chinespirit.dep

import sttp.client4.quick.*
import sttp.client4.jsoniter.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import just.semver.SemVer
import ox.either, either.*
import cue4s.*

object search:

  class Search(baseUrl: String):
    val scaladex = new ScaladexApi(baseUrl)

    def search(
      query: String,
      platform: Platform = Platform.JVM,
      scalaVersion: ScalaVersionSuffix = ScalaVersionSuffix.`3`
    ): Either[Throwable, Dependency] = either:
      Prompts.sync.use { prompts =>
        val projects = scaladex.search(query, platform, scalaVersion.toString()).ok()

        if projects.isEmpty then Exception(s"No projects matching query '$query' found!").fail()

        val options = projects.map(p => s"${p.organization}/${p.repository}").toList

        prompts
          .singleChoice("Select the project you're interested in:", options)
          .toOption
          .match
            case None => Exception("No project selected!").fail()
            case Some(s"$org/$repo") =>
              val artifacts = scaladex.fetchProject(org, repo).ok()
              val artifactsByArtifactId = artifacts
                .groupBy(_.artifactId)
                .map { case (artifactId, artifacts) =>
                  ArtifactId.parse(artifactId).ok() -> artifacts.sortBy(_.version).reverse // has to be at least one -> groupBy
                }
                .view
                .filterKeys {
                  case ArtifactId(_, _, Some(artifactsScalaVersion)) =>
                    artifactsScalaVersion.toSuffix == Some(scalaVersion)
                  case _ => false
                }
                .toMap

              val artifactNames = artifactsByArtifactId.keys.iterator.map(_.baseArtifact).distinct.toList.sortBy(_.size)
              val artifactIdsByName = artifactsByArtifactId.map { case (artifactId, _) =>
                artifactId.baseArtifact -> artifactId
              }

              // should be impossible, search must have returned at least one artifact for this version?
              if artifactNames.isEmpty then Exception("No artifacts found for the given scala version!").fail()

              val artifactName =
                prompts
                  .singleChoice(s"Select the artifact of $org/$repo project:", artifactNames)
                  .toEither
                  .ok()

              val artifactId       = artifactIdsByName(artifactName)
              val artifactVersions = artifactsByArtifactId(artifactId)

              // should be impossible, search must have returned at least one artifact for this version?
              if artifactVersions.isEmpty then Exception("No artifacts found for the given scala version!").fail()

              val version = prompts
                .singleChoice(s"Select the version of $artifactName:", artifactVersions.map(_.version.render).toList)
                .toEither
                .ok()

              val artifact = artifactVersions.find(_.version.render == version).get

              Dependency.ScalaCli(
                artifact.groupId,
                artifactId,
                artifact.version.render,
                DependencyType.Scala(false),
                Scope.Main
              )
            case Some(what) =>
              Exception(s"Unknown project selected: $what. Exiting.").fail()
        end match
      }
  end Search

  enum Platform:
    case JVM, JS, NATIVE, SBT
  object Platform:
    def of(s: String): Option[Platform] = values.find(_.toString().toLowerCase == s.toLowerCase)

  case class FoundProject(
    organization: String,
    repository: String,
    logo: String,
    artifacts: Vector[String],
    deprecatedArtifacts: Vector[String]
  )

  case class Artifact(
    groupId: String,
    artifactId: String,
    version: SemVer
  ):
    override def toString(): String = s"$groupId:$artifactId:${version.render}"

  given JsonValueCodec[SemVer] = new JsonValueCodec[SemVer]:
    def nullValue: SemVer                                    = null
    def decodeValue(in: JsonReader, default: SemVer): SemVer = SemVer.parseUnsafe(in.readString(null))
    def encodeValue(x: SemVer, out: JsonWriter): Unit        = out.writeVal(x.render)

  given vecFoundProjectCodec: JsonValueCodec[Vector[FoundProject]] = JsonCodecMaker.make
  given vecArtifactCodec: JsonValueCodec[Vector[Artifact]]         = JsonCodecMaker.make

  val ScalaDexBaseUrl = "https://index.scala-lang.org"

  class ScaladexApi(baseUrl: String):
    def search(query: String, platform: Platform, scalaVersion: String): Either[Throwable, Vector[FoundProject]] =
      either.catching:
        val url =
          uri"$baseUrl/api/search"
            .addParam("q", query)
            .addParam("target", platform.toString)
            .addParam("scalaVersion", scalaVersion)

        basicRequest
          .get(url)
          .contentType("application/json")
          .response(asJson[Vector[FoundProject]])
          .send()
          .body
          .ok()

    def fetchProject(
      organization: String,
      repository: String,
      binaryVersion: Option[String] = None,
      artifactName: Option[String] = None,
      stableOnly: Option[Boolean] = None
    ): Either[Throwable, Vector[Artifact]] =
      either.catching:
        val url =
          uri"$baseUrl/api/v1/projects/${organization}/${repository}/artifacts"
            .addParam("binary-version", binaryVersion)
            .addParam("artifact-name", artifactName)
            .addParam("stable-only", stableOnly.map(_.toString()))

        basicRequest
          .get(url)
          .contentType("application/json")
          .response(asJson[Vector[Artifact]])
          .send()
          .body
          .ok()

end search
