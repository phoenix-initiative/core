package org.phoenix.core.repositories

import com.typesafe.scalalogging.LazyLogging

object DAL extends UserRepositoryComponent with H2DBComponent with LazyLogging{
  val userRepository = new UserRepository()
}

