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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.smassetsfrontend.config.AppConfig
import uk.gov.hmrc.smassetsfrontend.services.AssetCacheService
import uk.gov.hmrc.smassetsfrontend.views.html.Installed

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AdminController @Inject() (
  assetCacheService: AssetCacheService,
  config           : AppConfig,
  mcc              : MessagesControllerComponents
)(implicit
  ec: ExecutionContext
) extends FrontendController(mcc) {

  def installed(): Action[AnyContent] =
    Action {
      val available = assetCacheService.listAvailable()
      val failed    = assetCacheService.listFailed()
      Ok(Installed(config, available, failed))
    }

  def uninstall(): Action[AnyContent] =
    Action.async(
      assetCacheService.uninstall().map(_ => Ok("Cache Cleared"))
    )
}
