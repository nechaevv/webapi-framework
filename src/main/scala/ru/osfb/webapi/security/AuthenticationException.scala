package ru.osfb.webapi.security

import ru.osfb.webapi.core.ApiException

/**
  * Created by sgl on 25.01.16.
  */
class AuthenticationException extends ApiException("AUTH_FAILED", "Authentication failed")