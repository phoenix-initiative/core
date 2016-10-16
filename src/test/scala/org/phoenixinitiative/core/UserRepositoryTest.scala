package org.phoenix.core.test

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import org.phoenix.core.repositories._

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

class UserRepositorySpec extends UnitSpec with ScalaFutures {

  // Some test users
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

    // Set for easy use
    val testUsers = Set(scott, keith, erlich)
 

  "A UserRepository" should "create single users with createUser" in {
    
    // Initialize db with blocking schema creation call
    DAL.userRepository.createTable()

    // Create three users individually
    Await.ready(DAL.userRepository.createUser(scott), 2 seconds)

    Await.ready(DAL.userRepository.createUser(keith), 2 seconds)

    Await.ready(DAL.userRepository.createUser(erlich), 2 seconds)

    // Check if created users are returned correctly
    ScalaFutures.whenReady(DAL.userRepository.getAllUsers()) { dbusers =>
      // Compare using sets b/c order does not matter
      dbusers.toSet should equal (testUsers)
    }

    // Clean up db by deleting table
    DAL.userRepository.deleteTable()

    // Should fal
    //val result = Await.ready(DAL.userRepository.createUser(scott), 2 seconds)
    //assert(result.isInstanceOf[Failure[Any]])
 
  }

  it should "create multiple users with createUsers" in {

    // Initialize db with blocking schema creation call
    DAL.userRepository.createTable()

    // Create three users
    Await.ready(DAL.userRepository.createUsers(testUsers.toSeq), 2 seconds)
 
    // Check if created users are returned correctly
    ScalaFutures.whenReady(DAL.userRepository.getAllUsers()) { dbusers =>
      // Compare using sets b/c order does not matter
      dbusers.toSet should equal (testUsers)
    }

    // Clean up db by deleting table
    DAL.userRepository.deleteTable()
  }

  it should "retrieve users by slug and id" in {
    // Initialize db with blocking schema creation call
    DAL.userRepository.createTable()

    // Create three users
    Await.ready(DAL.userRepository.createUsers(testUsers.toSeq), 2 seconds)

    // Perform id checks
    def checkGetById(user: User) {
      ScalaFutures.whenReady(
        DAL.userRepository.getUserById(user.uuid)
      ) { dbuser =>
        dbuser.get should equal (user)
      }
    }

    // Perform username checks
    def checkGetByUsername(user: User) {
      ScalaFutures.whenReady(
        DAL.userRepository.getUserByUsername(user.username)
      ) { dbuser =>
        dbuser.get should equal (user)
      }
    }
 
    testUsers.map((testUser: User) => {
        checkGetById(testUser)
        checkGetByUsername(testUser)
    })

    // Clean up db by deleting table
    DAL.userRepository.deleteTable()
 
  
  }

}
