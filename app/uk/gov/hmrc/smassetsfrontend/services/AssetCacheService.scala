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

package uk.gov.hmrc.smassetsfrontend.services

import akka.stream.Materializer
import akka.stream.scaladsl.FileIO
import play.api.Logger
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.WSClient
import uk.gov.hmrc.smassetsfrontend.config.AppConfig

import java.io.{File, FilenameFilter}
import java.util.zip.ZipFile
import javax.inject.Inject
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}


class AssetCacheService @Inject()(client: WSClient, config: AppConfig, cache: AsyncCacheApi)(implicit ec: ExecutionContext, mat: Materializer) {
  private val logger = Logger(this.getClass)
  private val failedDownloads = mutable.Map[String, String]()

  def getAsset(version:String): Future[Option[ZipFile]] = cache.getOrElseUpdate(version)(downloadIfMissing(version))


  def downloadIfMissing( version: String): Future[Option[ZipFile]] = {
    val maybeFile = config.cacheDir.resolve(s"assets-frontend-$version.zip").toFile
    if(maybeFile.exists()) {
      Future.successful(Option(new ZipFile(maybeFile)))
    } else {
      logger.info(s"version $version not found locally, downloading")
      download(version, maybeFile).recover { case _:Exception => None }
    }
  }

  private def download(version: String,  outputFile: File): Future[Option[ZipFile]] = {

    val url = s"https://${config.artifactoryUrl}${config.artifactoryPath}$version/assets-frontend-$version.zip"

    // abort early if we've already tried the url and it didnt work
    if(failedDownloads.contains(url)) {
      return Future.successful(None)
    }
    for {
      resp   <- client.url(url).withMethod("GET").stream()
      _       = logger.info(s"downloading $url")
      result <- if(resp.status == 200)
                  resp.bodyAsSource.runWith(FileIO.toPath(outputFile.toPath)).map(_ => Some(new ZipFile(outputFile)))
                else {
                  logger.info(s"failed to download $url - ${resp.status}")
                  failedDownloads.put(url, s"${resp.status} ${resp.statusText}")
                  Future.successful(None)
                }
    } yield result
  }

  def listFailed(): Map[String, String] = failedDownloads.toMap

  def listAvailable(): Seq[File] =
    config
      .cacheDir
      .toFile
      .listFiles(new FilenameFilter {override def accept(file: File, name: String): Boolean = name.startsWith("assets-frontend-") && name.endsWith(".zip")}).toSeq
      .filter(_.isDirectory==false)

  def uninstall(): Unit = {
    listAvailable()
      .foreach(file => {
        logger.info(s"Deleting ${file.getPath}")
        file.delete()
      })
  }
}
