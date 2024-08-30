/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.smassetsfrontend

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.{ScalaFutures, IntegrationPatience}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.libs.ws.WSBodyReadables.readableAsString
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.smassetsfrontend.services.Hashing

class AssetsFrontendIntegrationSpec
  extends AnyWordSpec
     with Matchers
     with ScalaFutures
     with IntegrationPatience
     with GuiceOneServerPerSuite
     with WireMockSupport:

  val cacheDir = java.nio.file.Files.createTempDirectory("assets-frontend-cache-dir")

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(
        "artifactory.url"  -> wireMockUrl,
        "artifactory.path" -> "/path/",
        "workdir"          -> cacheDir.toString
      )
      .build()

  val wsClient = app.injector.instanceOf[WSClient]

  "Requesting asset" should:
    "return asset" in:
      val assetVersion = "1.0.0"
      val assetFile    = s"assets-frontend-$assetVersion.zip"

      wireMockServer.stubFor(
        WireMock.get(urlEqualTo(s"/path/$assetVersion/$assetFile"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBodyFile(assetFile)
              .withHeader("X-Checksum-Sha1", Hashing.sha1(getClass.getResourceAsStream(s"/__files/$assetFile")))
          )
      )

      def testDownload(): Unit =
        val response = wsClient.url(resource(s"assets/$assetVersion/file1.txt")).get().futureValue
        response.status shouldBe 200
        response.body shouldBe "FILE_CONTENT\n"

      testDownload()
      testDownload() // second time should be cached

      verify(1, getRequestedFor(urlEqualTo(s"/path/$assetVersion/$assetFile")))

  def resource(path: String): String =
    s"http://localhost:$port/$path"
