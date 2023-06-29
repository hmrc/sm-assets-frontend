import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {
  val bootstrapPlayVersion = "7.19.0"

  val compile = Seq(
    caffeine,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % "7.14.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28"     % bootstrapPlayVersion % "test, it"
  )
}
