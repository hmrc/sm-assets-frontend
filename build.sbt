import play.sbt.PlayImport.PlayKeys.playDefaultPort
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

lazy val microservice = Project("sm-assets-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    majorVersion        := 0,
    scalaVersion        := "2.13.8",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions       += "-Wconf:cat=unused-imports&src=html/.*:s",
    scalacOptions       += "-Wconf:src=routes/.*:s"
  )
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(playDefaultPort := 9032)
