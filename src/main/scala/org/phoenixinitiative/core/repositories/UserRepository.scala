package org.phoenix.core.repositories

import com.typesafe.scalalogging.LazyLogging
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Success, Failure}

/** A User account
 *
 *  @constructor create a new User
 *  @param id numerical id for user
 *  @param username login username of user
 *  @param name full name of user
 *  @param slug url version of name
 *  @param bio user bio for user page
 *  @param email email for password resets, etc
 *  @param hashedPassword hash of password
 *  @param authLevel level of authority 0-2
 */
case class User(
  id: Option[Long] = None,
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
    def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
    def username = column[String]("USERNAME")
    def hashedPassword = column[String]("HASHED_PASSWORD")
    def name = column[String]("NAME")
    def slug = column[String]("SLUG")
    def bio = column[String]("BIO")
    def email = column[String]("EMAIL")
    def authLevel = column[Int]("AUTH_LEVEL")

    def * = (id, username, hashedPassword, name, slug, bio, email, authLevel) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]

  class UserRepository(){

    // Blocking function to ensure that the db has the schema for other calls
    def createTable() = {
      // Block with await
      val result = Await.ready(db.run(users.schema.create), 5 seconds).value.get

      // Log error or success
      result match {
        case Success(value) => logger.debug("user schema created")
        case Failure(e) => logger.error(e.getStackTrace.mkString("\n"))
      }
    }

    def getAllUsers(): Future[Seq[User]] = db.run(users.result)

    def getUserById(id: Long): Future[Option[User]] = db.run(users.filter(_.id === id).result.headOption)

  
    def getUserByUsername(username: String): Future[Option[User]] = db.run(users.filter(_.username === username).result.headOption)
  
    def createUser(user: User): Future[User] = db.run(
      (users returning users.map(_.id)
        into ((user, id) => user.copy(id = id))
      ) += user)
  
    def deleteUser(id: Long): Future[Int] = db.run(users.filter(_.id === id).delete)
  
    def updateUser(user: User) = db.run(users.filter(_.id === user.id.get).update(user))
   
  }

}

