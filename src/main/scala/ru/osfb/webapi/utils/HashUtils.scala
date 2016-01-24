package ru.osfb.webapi.utils

import java.security.MessageDigest

/**
 * Created by sgl on 27.06.15.
 */
object HashUtils {
  def hash(str: String, alg: String = "SHA-256"): Array[Byte] = {
    val hasher = MessageDigest.getInstance(alg)
    hasher.update(str.getBytes("UTF-8"))
    hasher.digest()
  }
}
