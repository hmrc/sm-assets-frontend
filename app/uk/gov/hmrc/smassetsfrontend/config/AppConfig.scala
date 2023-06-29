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

package uk.gov.hmrc.smassetsfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration

import java.nio.file.{Path, Paths}

@Singleton
class AppConfig @Inject()(config: Configuration) {

  val artifactoryUrl: String  = config.get[String]("artifactory.url")
  val artifactoryPath: String = config.get[String]("artifactory.path")
  val workDir: String         = config.get[String]("workdir")

  val cacheDir: Path = Paths.get(workDir, "assets-cache")
  if(!cacheDir.toFile.exists()) {
    cacheDir.toFile.mkdir()
  }

}
