package ru.osfb.webapi.core

/**
  * Created by sgl on 25.01.16.
  */
class ApiException(code: String, description: String) extends RuntimeException(code)