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

import java.io.{File, FileInputStream}
import java.security.{DigestInputStream, MessageDigest}

object Hashing {

  def validateFileSha1(sha1Sum: String, file: File): Boolean = {
    val digest = MessageDigest.getInstance("SHA-1")
    val fis    = new FileInputStream(file)
    val dis    = new DigestInputStream(fis, digest)

    val buffer = new Array[Byte](4096)
    while (dis.available() > 0) {
      dis.read(buffer)
    }
    val fileSha1 = digest.digest.map(b => String.format("%02x", Byte.box(b))).mkString
    sha1Sum.equalsIgnoreCase(fileSha1)
  }

}
