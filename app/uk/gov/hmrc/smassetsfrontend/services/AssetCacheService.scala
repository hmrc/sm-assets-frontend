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
import java.nio.file.{Files, StandardCopyOption}
import java.util.zip.ZipFile
import javax.inject.Inject
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}


class AssetCacheService @Inject()(client: WSClient, config: AppConfig, cache: AsyncCacheApi)(implicit ec: ExecutionContext, mat: Materializer) {
  private val logger = Logger(this.getClass)
  private val failedDownloads = mutable.Map[String, String]()

  def getAsset(version:String): Future[Option[ZipFile]] = cache.getOrElseUpdate(version)(downloadIfMissing(version))

  private def downloadIfMissing(version: String): Future[Option[ZipFile]] = {
    val assetFile = config.cacheDir.resolve(s"assets-frontend-$version.zip").toFile

    if(assetFile.exists()) {
        Future.successful(Some(new ZipFile(assetFile)))
    } else {
      logger.info(s"version $version not found locally, downloading")
      val tmpFile = File.createTempFile("assets-frontend", "zip")

      for {
        downloadedFile <- download(version, tmpFile).recover { case _:Exception => None }
        outFile         = downloadedFile.map(file => Files.move(file.toPath, assetFile.toPath, StandardCopyOption.REPLACE_EXISTING).toFile)
        zipFile         = outFile.map(f => new ZipFile(f))
      } yield zipFile

    }
  }

  private def download(version: String, outputFile: File): Future[Option[File]] = {

    val url = s"https://${config.artifactoryUrl}${config.artifactoryPath}$version/assets-frontend-$version.zip"

    // abort early if we've already tried the url and it didnt work
    if(failedDownloads.contains(url)) return Future.successful(None)

    for {
      resp   <- client.url(url).withMethod("GET").stream()
      _       = logger.info(s"downloading $url")
      result <- if(resp.status == 200)
                  resp
                    .bodyAsSource
                    .runWith(FileIO.toPath(outputFile.toPath))
                    // only return file if sha1 is valid
                    .map(_ => resp.header("X-Checksum-Sha1")
                                  .filter(sha1 => Hashing.validateFileSha1(sha1, outputFile))
                                  .map(_       => outputFile))
                else {
                  logger.info(s"failed to download $url - ${resp.status}")
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
      .filter(_.isFile)

  def uninstall(): Future[Unit] = {
    cache.removeAll().map(_ =>
    listAvailable()
      .foreach(file => {
        logger.info(s"Deleting ${file.getPath}")
        file.delete()
      })
    )
  }

}

