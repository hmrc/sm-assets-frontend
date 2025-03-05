import play.sbt.PlayImport.PlayKeys.playDefaultPort
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion  := 0
ThisBuild / scalaVersion  := "3.3.5"
ThisBuild / scalacOptions += "-Wconf:msg=Flag.*repeatedly:s"

lazy val microservice = Project("sm-assets-frontend", file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions       += "-Wconf:msg=unused import&src=html/.*:s",
    scalacOptions       += "-Wconf:src=routes/.*:s"
  )
  .settings(playDefaultPort := 9032)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings(forkJvmPerTest = true))
  .settings(libraryDependencies ++= AppDependencies.it)
