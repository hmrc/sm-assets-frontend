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

package uk.gov.hmrc.smassetsfrontend.controllers

import org.apache.pekko.stream.scaladsl.StreamConverters
import play.api.http.HttpEntity
import play.api.mvc._
import play.mvc.StaticFileMimeTypes
import uk.gov.hmrc.smassetsfrontend.services.AssetCacheService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AssetsController @Inject()(
  cacheService: AssetCacheService,
  cc          : ControllerComponents
)(using
  ec          : ExecutionContext
) extends AbstractController(cc):

  private val mimeTypes = StaticFileMimeTypes.fileMimeTypes().asScala

  def assets(version: String, path: String): Action[AnyContent] =
    Action.async:
      for
        zf     <- cacheService.getAsset(version)
        result =  zf.fold(NotFound(s"Invalid version $version")): zipFile =>
                    Option(zipFile.getEntry(path))
                      .fold(NotFound(s"Invalid file $path")): zipEntry =>
                        val content  = StreamConverters.fromInputStream(() => zipFile.getInputStream(zipEntry))
                        val mineType = Some(mimeTypes.forFileName(path).getOrElse("application/octet-stream"))
                        Result(
                          header = ResponseHeader(200, Map("Cache-Control" -> "max-age=680000, immutable")),
                          body   = HttpEntity.Streamed(content, None, mineType)
                        )
      yield result
