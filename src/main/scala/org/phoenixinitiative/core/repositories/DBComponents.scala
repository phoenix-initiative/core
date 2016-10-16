package org.phoenix.core.repositories

import slick.driver.JdbcProfile

/** Generic Database access interface */
trait DBComponent {

  /** The driver specified
   *
   *  Can be H2, sqlite, mysql, etc
   *  The slick api will be imported from this driver
   */
  val driver: JdbcProfile

  // Import the api from the driver
  import driver.api._

  /** The database being accessed
   *
   *  Can be populated with a config or in code
   */
  val db: Database

}

/** H2 DBComponent */
trait H2DBComponent extends DBComponent {
 
  val driver = slick.driver.H2Driver
 
  import driver.api._
 
  val h2Url = "jdbc:h2:mem:test;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1"
 
  val db: Database = Database.forURL(url = h2Url, driver = "org.h2.Driver")
 
}
