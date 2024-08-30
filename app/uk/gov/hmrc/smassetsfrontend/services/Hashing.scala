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

import java.io.{File, FileInputStream, InputStream}
import java.security.{DigestInputStream, MessageDigest}

object Hashing:
  private val digest = MessageDigest.getInstance("SHA-1")

  def sha1(file: File): String =
    sha1(FileInputStream(file))

  def sha1(is: InputStream): String =
    val dis = DigestInputStream(is, digest)

    try
      val buffer = new Array[Byte](4096)
      while (dis.available() > 0)
        dis.read(buffer)
    finally
      dis.close()

    digest.digest.map(b => String.format("%02x", Byte.box(b))).mkString
