package org.phoenix.core.test

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import org.phoenix.core.repositories._

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

class UserRepositorySpec extends UnitSpec with ScalaFutures{
  "A UserRepository" should "create users" in {

    val scott = User(
      username="scotthansen",
      name="Scott Hansen",
      slug="scott-hansen",
      bio="I make some music",
      email="shansen@tycho.net",
      hashedPassword="superSecretPassword",
      authLevel=2
    )

    val keith = User(
      username="keithmoon",
      name="Keith Moon",
      slug="keith-moon",
      bio="I drum sometimes",
      email="vomit@me.net",
      hashedPassword="secretPassword",
      authLevel=2
    )

    val erlich = User(
      username="erlichbachman",
      name="Erlich Bachman",
      slug="erlich-bachman",
      bio="Come to my alcatraz party",
      email="erlich@bachmanity.com",
      hashedPassword="thisisyourmomyournotmyson",
      authLevel=2
    )

    // Check everything but id as id comes from db insertion
    def userCheck(original: User, inserted: User) {
      original.username should be (inserted.username)
      original.name should be (inserted.name)
      original.slug should be (inserted.slug)
      original.bio should be (inserted.bio)
      original.email should be (inserted.email)
      original.hashedPassword should be (inserted.hashedPassword)
      original.authLevel should be (inserted.authLevel)

      // Make sure db created and returned an id for the user
      assert(inserted.id.isDefined)
    }

    // Initialize db with blocking schema creation call
    DAL.userRepository.createTable()

    // Check individual users are inserted and returned with id
    ScalaFutures.whenReady(DAL.userRepository.createUser(scott)) { user =>
      userCheck(scott, user)
    }

    ScalaFutures.whenReady(DAL.userRepository.createUser(keith)) { user =>
      userCheck(keith, user)
    }
    ScalaFutures.whenReady(DAL.userRepository.createUser(erlich)) { user =>
      userCheck(erlich, user)
    }

    // Check if created users are returned correctly
    ScalaFutures.whenReady(DAL.userRepository.getAllUsers()) { dbusers =>
      val users: List[User] = List(scott, keith, erlich)
      for (dbuser <- dbusers) {
        // find user by username b/c no ids in test users
        val testuser =  users.filter(u => (u.username == dbuser.username))(0)
        userCheck(testuser, dbuser)
      
      }
    }

  }

}
