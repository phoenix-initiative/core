package org.phoenix.core.repositories

import com.typesafe.scalalogging.LazyLogging
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Success, Failure}
import java.util.UUID

/** A User account
 *
 *  @constructor create a new User
 *  @param uuid numerical id for user
 *  @param username login username of user
 *  @param name full name of user
 *  @param slug url version of name
 *  @param bio user bio for user page
 *  @param email email for password resets, etc
 *  @param hashedPassword hash of password
 *  @param authLevel level of authority 0-2
 */
case class User(
  uuid: UUID = UUID.randomUUID(),
  username: String,
  hashedPassword: String,
  name: String,
  slug: String,
  bio: String,
  email: String,
  authLevel: Int
)

/** A Database table for users
 *  Extends DBComponent to gain access to slick api and DB
 */
trait UserRepositoryComponent { this: DBComponent with LazyLogging =>

  // Get the slick api from DBComponent
  import driver.api._

  class Users(tag: Tag) extends Table[User](tag, "USERS"){
    def uuid = column[UUID]("USER_UUID", O.PrimaryKey)
    def username = column[String]("USER_USERNAME")
    def hashedPassword = column[String]("USER_HASHED_PASSWORD")
    def name = column[String]("USER_NAME")
    def slug = column[String]("USER_SLUG")
    def bio = column[String]("USER_BIO")
    def email = column[String]("USER_EMAIL")
    def authLevel = column[Int]("USER_AUTH_LEVEL")

    def * = (uuid, username, hashedPassword, name, slug, bio, email, authLevel) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]

  class UserRepository(){

    // Blocking function to ensure that the db has the schema for other calls
    def createTable() = {
      // Block with await
      val result = Await.ready(
        db.run(users.schema.create), 5 seconds
      ).value.get

      // Log error or success
      result match {
        case Success(value) => logger.debug("user schema created")
        case Failure(e) => logger.error(e.getStackTrace.mkString("\n"))
      }
    }

    def deleteTable() = {
      // Block with await
      val result = Await.ready(
        db.run(users.schema.drop), 5 seconds
      ).value.get

      // Log error or success
      result match {
        case Success(value) => logger.debug("user schema dropped")
        case Failure(e) => logger.error(e.getStackTrace.mkString("\n"))
      }
    }

    def getAllUsers(): Future[Seq[User]] = db.run(
      users.result
    )

    def getUserById(uuid: UUID): Future[Option[User]] = db.run(
      users.filter(_.uuid === uuid).result.headOption
    )
  
    def getUserByUsername(username: String): Future[Option[User]] = db.run(
      users.filter(_.username === username).result.headOption
    )
  
    def createUser(user: User): Future[Int] = db.run(
      users += user
    )

    def createUsers(newUsers: Seq[User]): Future[Option[Int]] = db.run(
      users ++= newUsers
    )
  
    def deleteUser(uuid: UUID): Future[Int] = db.run(
      users.filter(_.uuid === uuid).delete
    )
  
    def updateUser(user: User) = db.run(
      users.filter(_.uuid === user.uuid).update(user)
    )
   
  }

}

