package ru.osfb.webapi.session

import java.util.UUID

/**
 * Created by sgl on 28.06.15.
 */
case class UserSession(userId: UUID, clientToken: Option[String])
