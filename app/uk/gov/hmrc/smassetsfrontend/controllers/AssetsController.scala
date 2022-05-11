/*
 * Copyright 2022 HM Revenue & Customs
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

import akka.stream.scaladsl.StreamConverters
import play.api.Logger
import play.api.http.HttpEntity
import play.api.mvc._
import play.mvc.StaticFileMimeTypes
import uk.gov.hmrc.smassetsfrontend.services.AssetCache

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AssetsController @Inject()(assetCache: AssetCache, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc){

  private val logger = Logger(this.getClass)
  private val mimeTypes = StaticFileMimeTypes.fileMimeTypes().asScala()

  def assets(version: String, path: String): Action[AnyContent] =  Action.async { implicit request =>
    assetCache.downloadIfMissing(version).map(
      _.fold(NotFound(s"Invalid version $version"))(zf =>
        Option(zf.getEntry(path))
          .fold(NotFound(s"Invalid file $path"))(ze => {
            val content = StreamConverters.fromInputStream(() => zf.getInputStream(ze))
            Result(
              header = ResponseHeader(200, Map.empty),
              body   = HttpEntity.Streamed(content, None, Some(mimeTypes.forFileName(path).getOrElse("application/octet-stream")))
            )
          })
      )
    )
  }
}
