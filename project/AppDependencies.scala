import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    caffeine,
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-28" % "5.24.0",
    "uk.gov.hmrc"             %% "play-frontend-hmrc"         % "3.15.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.24.0"             % "test, it",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"            % "test, it"
  )
}
