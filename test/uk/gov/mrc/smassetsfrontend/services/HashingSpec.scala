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

package uk.gov.hmrc.smassetsfrontend.services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{File, FileWriter}

class HashingSpec extends AnyWordSpec with Matchers:

  "sha1" should:
    "calculate the SHA-1 hash of a file's content" in:
      val tmpFile = File.createTempFile("hashingspec", ".test")
      tmpFile.deleteOnExit()

      val fw = FileWriter(tmpFile)
      fw.write("TEST_DATA")
      fw.close()

      Hashing.sha1(tmpFile).toLowerCase shouldBe "e88dfe5ea9ab61ad9ffe368a3c699c6c1b1e20a0"
