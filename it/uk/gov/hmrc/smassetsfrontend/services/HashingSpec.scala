package uk.gov.hmrc.smassetsfrontend.services

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{File, FileWriter}

class HashingSpec extends AnyWordSpec with Matchers{

  "validateFileSha1" must {

    "test if the SHA-1 hash of a file's content equals a given hex-string" in {

      val tmpFile = File.createTempFile("hashingspec", ".test")
      tmpFile.deleteOnExit()

      val fw = new FileWriter(tmpFile)
      fw.write("TEST_DATA")
      fw.close()

      Hashing.validateFileSha1("e88dfe5ea9ab61ad9ffe368a3c699c6c1b1e20a0".toUpperCase, tmpFile) mustBe true  // TEST_DATA
      Hashing.validateFileSha1("fd6a5ecb0c6d5148842ac5a363fcb56a58341f32".toUpperCase, tmpFile) mustBe false // FAIL
    }

  }

}
