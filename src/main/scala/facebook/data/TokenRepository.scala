package facebook.data

import java.sql.SQLException

import com.typesafe.scalalogging.LazyLogging
import common.ActorSystemSupport
import database.DataSource

import scala.concurrent.Future

class TokenRepository() extends ActorSystemSupport with LazyLogging {

  def insert(token: String): Future[Unit] = Future {
    val connection = DataSource.connectionPool.getConnection
    val stmt = connection.createStatement()
    stmt.executeUpdate(s"""INSERT INTO tokens(token) VALUES ('${token}')""")
    stmt.close()
    connection.close()
  }

  def find: Future[Option[String]] = Future {
    val connection = DataSource.connectionPool.getConnection
    val stmt = connection.createStatement()
    val rs = stmt.executeQuery("SELECT token FROM tokens")
    try {
      if (rs.next()) {
        Some(rs.getString(1))
      } else {
        None
      }
    } catch {
      case ex: SQLException =>
        logger.error(ex.toString)
        None
    } finally {
      rs.close()
      stmt.close()
      connection.close()
    }
  }

}