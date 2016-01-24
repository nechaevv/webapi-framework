package ru.osfb.webapi.utils

/**
 * Created by sgl on 20.09.15.
 */
object Hex {
  def apply(bytes: Array[Byte]) = bytes.map("%02X" format _).mkString
}
