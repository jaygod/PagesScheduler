import akka.http.scaladsl.Http
import com.typesafe.scalalogging.LazyLogging
import common.ActorSystemSupport
import common.api.PagesSchedulerApi
import facebook.data.TokenRepository
import facebook.service.FacebookService
import scheduler.service.SchedulerService

import scala.util.{Failure, Properties, Success}

object Main extends App with LazyLogging with ActorSystemSupport {

  private val tokenRepository = new TokenRepository()

  tokenRepository.find.onComplete {
    case Success(Some(token)) =>
      val facebookService = new FacebookService(Some(token))
      val routes = new PagesSchedulerApi(facebookService).routes
      val httpPort = Properties.envOrElse("PORT", "8080").toInt // for Heroku compatibility
    val bindingFuture = Http().bindAndHandle(routes, "0.0.0.0", httpPort)

      bindingFuture.andThen {
        case Success(success) => logger.info(success.toString)
        case Failure(ex) => logger.error(ex.toString)
      }

      logger.warn("Using existing access token")
      SchedulerService.start(facebookService)
    case Success(None) =>
      logger.warn("Access token is missing. You should require for a new one")
      val facebookService = new FacebookService(None)
      val routes = new PagesSchedulerApi(facebookService).routes
      val httpPort = Properties.envOrElse("PORT", "8080").toInt // for Heroku compatibility
    val bindingFuture = Http().bindAndHandle(routes, "0.0.0.0", httpPort)

      bindingFuture.andThen {
        case Success(success) => logger.info(success.toString)
        case Failure(ex) => logger.error(ex.toString)
      }

    case Failure(ex) =>
      logger.error(ex.toString)

  }
}
