package database

import java.net.URI
import java.sql.SQLException

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.dbcp2.BasicDataSource

/**
  * Created by kuba on 17/06/2017.
  */
object DataSource extends LazyLogging {
  val config: Config = ConfigFactory.load

  lazy val dbUrl: String = config.getString("postgres.databaseUrl")
  val dbUri = new URI(dbUrl)
  val connectionPool = new BasicDataSource()

  if (dbUri.getUserInfo != null) {
    connectionPool.setUsername(dbUri.getUserInfo.split(":")(0))
    connectionPool.setPassword(dbUri.getUserInfo.split(":")(1))
  }
  connectionPool.setDriverClassName("org.postgresql.Driver")
  connectionPool.setUrl(dbUrl)
  connectionPool.setInitialSize(5)

  createPostgresSchema()

  def createPostgresSchema(): Unit = {
    val connection = DataSource.connectionPool.getConnection
    val stmt = connection.createStatement()
    try {
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tokens (token text PRIMARY KEY)")
    } catch {
      case e: SQLException => logger.error(e.toString)
    } finally {
      stmt.close()
      connection.close()
      logger.info("Postgres initialized")
    }
  }

}

