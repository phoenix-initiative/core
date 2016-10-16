package org.phoenix.core.services

import com.typesafe.scalalogging.LazyLogging
import org.phoenix.core.repositories.{User, DAL}
import scala.concurrent._
import ExecutionContext.Implicits.global
import java.util.UUID

trait UserServiceComponent { this: LazyLogging =>

  class UserService() {

    def createUser(newUser: User): Future[Boolean] = DAL.userRepository.createUser(newUser) map { linesChanged: Int =>
      if (linesChanged > 0) true // Success, something changed
      else false // Failure, no change in db
    }

    def deleteUser(uuid: UUID): Future[Boolean] = DAL.userRepository.deleteUser(uuid) map { linesChanged: Int =>
      if (linesChanged > 0) true // Success
      else false // Failure, no change in db
    }

    def getUserByUsername(username: String): Future[Option[User]] = DAL.userRepository.getUserByUsername(username)
  
  }

}
