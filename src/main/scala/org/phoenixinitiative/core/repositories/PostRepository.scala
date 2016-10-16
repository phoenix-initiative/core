package org.phoenix.core.repositories

import com.typesafe.scalalogging.LazyLogging
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import java.util.UUID
import java.sql.{Date, Timestamp}
import java.time.Instant

import slick.lifted.CaseClassShape

/** a Post object
 *
 * @constructor create new Post
 * @param uuid unique id for post, default is new random uuid
 * @param title user-facing title for story
 * @param slug url version of title
 * @param content html content for the story
 * @param excerpt plain text exceprt for below story
 * @param status unpublished/published
 * @param owner user who has control of post
 * @param createdAt when post was created, defaults to now
 * @param updatedAt when post was last updated, defaults to now
 * @param publishedAt when post was published if published, defaults to None
 */
case class Post(
  uuid: UUID = UUID.randomUUID(),
  title: String,
  slug: String,
  content: String,
  excerpt: String,
  status: Int,
  authorUUIDs: Seq[UUID],
  createdAt: Timestamp = Timestamp.from(Instant.now()),
  updatedAt: Timestamp = Timestamp.from(Instant.now()),
  publishedAt: Option[Timestamp] = None
)

/** Post data that goes under the Posts table
 *
 */
case class TablePost(
  uuid: UUID,
  title: String,
  slug: String,
  content: String,
  excerpt: String,
  status: Int,
  createdAt: Timestamp,
  updatedAt: Timestamp,
  publishedAt: Option[Timestamp]
)

/** a database table for posts
 *  extneds DBComponent to gain access to slick api and DB
 */
trait PostRepositoryComponent {
  this: DBComponent
  with UserRepositoryComponent
  with LazyLogging =>

  // get the slick API from DBComponent
  import driver.api._

  class Posts(tag: Tag) extends Table[TablePost](tag, "POSTS"){
    def uuid = column[UUID]("POST_UUID", O.PrimaryKey)
    def title = column[String]("POST_TITLE")
    def slug = column[String]("POST_SLUG")
    def content = column[String]("POST_CONTENT")
    def excerpt = column[String]("POST_EXCERPT")
    def status = column[Int]("POST_STATUS")
    def createdAt = column[Timestamp]("POST_CREATED_AT")
    def updatedAt = column[Timestamp]("POST_UPDATED_AT")
    def publishedAt = column[Option[Timestamp]]("POST_PUBLISHED_AT")

    def * = (
      uuid, title,
      slug, content,
      excerpt, status,
      createdAt, updatedAt, publishedAt
    ) <> (TablePost.tupled, TablePost.unapply)
  
  }

  val posts = TableQuery[Posts]

  class PostToUsers(tag: Tag) extends Table[(UUID, UUID)](tag, "POST_USERS"){
    def postUUID = column[UUID]("POST_UUID")
    def userUUID = column[UUID]("USER_UUID")
    // Order of user, ie: 1, 2, 3
    def user_ordinal = column[Int]("USER_ORDINAL")

    def post = foreignKey("POST_FK", postUUID, posts)(_.uuid,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade
    )

    def user = foreignKey("USER_FK", userUUID, users)(_.uuid,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade
    )

    def * = (postUUID, userUUID)
  }

  val postToUsers = TableQuery[PostToUsers]

  // Blocking function to ensure that the db has the schema for other calls
  def createTable() = {
    // Block with await
    val result = Await.ready(
      db.run(DBIO.seq(
        posts.schema.create,
        postToUsers.schema.create
      )), 5 seconds
    ).value.get
 
    // Log error or success
    result match {
      case Success(value) => logger.debug("posts schema created")
      case Failure(e) => logger.error(e.getStackTrace.mkString("\n"))
    }
  }
 
  def deleteTable() = {
    // Block with await
    val result = Await.ready(
      db.run(DBIO.seq(
        posts.schema.drop,
        postToUsers.schema.drop
      )), 5 seconds
    ).value.get
 
    // Log error or success
    result match {
      case Success(value) => logger.debug("posts schema dropped")
      case Failure(e) => logger.error(e.getStackTrace.mkString("\n"))
    }
  }

  /** Combines the data from the Post table and the postToUsers table into Post
   *
   * @param data Tuple with the TablePost obj and a Seq of tuple user mappings
   * @return A post object with all data
   */
  def combineData(tablepost: TablePost, userUUIDs: Seq[UUID]): Post = {
    val combinedPost = Post(
      tablepost.uuid,
      tablepost.title,
      tablepost.slug,
      tablepost.content,
      tablepost.excerpt,
      tablepost.status,
      userUUIDs,
      tablepost.createdAt,
      tablepost.updatedAt,
      tablepost.publishedAt
    )

    return combinedPost
  }

  /** Splits the data from Post object into table data
   *  
   *  @param post Post object with all data
   *  @return Tuple of type (TablePost, Seq[UUID])
   */
  def splitData(post: Post): (TablePost,  Seq[UUID]) = {
    val tablepost = TablePost(
      post.uuid,
      post.title,
      post.slug,
      post.content,
      post.excerpt,
      post.status,
      post.createdAt,
      post.updatedAt,
      post.publishedAt
    )

    return (tablepost, post.authorUUIDs)

  }

  val postUserUUIDsQuery = Compiled(
    (postUUID: Rep[UUID]) => 
      for {
        ptu <- postToUsers if postUUID === ptu.postUUID
      } yield ptu.userUUID
  )

  // Slow because of multiple DB roundtrips...
  // Fix in future with a join-based query for all users

  def getPostUserUUIDs(postUUID: UUID): Future[Seq[UUID]] = db.run(
    postUserUUIDsQuery(postUUID).result
  )

  def getAllTablePosts(): Future[Seq[TablePost]] = db.run(
    posts.result
  )

  def getAllPosts(): Future[Seq[Post]] = Future.sequence(getAllTablePosts().map( 
    tableposts =>
      tableposts.map(tablepost => 
          getPostUserUUIDs(tablepost.uuid).flatMap(
            uuids => Future(combineData(tablepost, uuids))
          )
      )
  ))

  def getPostById(uuid: UUID): Future[Option[Post]] = ???

  def getPostBySlug(slug: String): Future[Option[Post]] = ???

  def getPostByUserId(uuid: UUID): Future[Seq[Post]] = ???

  def getPostByUsername(username: String): Future[Seq[Post]] = ???

  // Too many splitData calls here
  def createPost(post: Post): Future[Unit] = db.run(DBIO.seq(
    // Add main db entry
    posts += splitData(post)._1,
    // Add all the author entries
    postToUsers ++= splitData(post)._2.map((post.uuid, _))
  ))

  def createPosts(newPosts: Seq[Post]): Future[Option[Int]] = ???

  def deletePostById(uuid: UUID): Future[Unit] = db.run(DBIO.seq(
    posts.filter(_.uuid === uuid).delete,
    postToUsers.filter(_.postUUID === uuid).delete
  ))

  def deletePostBySlug(slug: String): Future[Int] = ???

}
