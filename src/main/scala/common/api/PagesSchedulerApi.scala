package common.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import com.typesafe.scalalogging.LazyLogging
import common.ActorSystemSupport
import facebook.api.FacebookProtocol
import facebook.service.FacebookService
import scheduler.service.SchedulerService

import scala.util.{Failure, Success}

/**
  * Created by kuba on 14/09/2017.
  */
class PagesSchedulerApi(facebookService: FacebookService)
  extends CustomExceptionHandler with FacebookProtocol with SprayJsonSupport with LazyLogging with ActorSystemSupport {

  val routes = authUrlRoute ~ authCallbackRoute ~ startSchedulerRoute ~ helloWorldRoute

  private def authUrlRoute =
    path("auth" / "url") {
      get {
        onComplete(facebookService.getLoginDialogUrl) {
          case Success(authUrl) => complete(StatusCodes.OK -> authUrl)
          case Failure(ex) => failWith(ex)
        }
      }
    }

  private def authCallbackRoute =
    path("auth" / "callback") {
      get {
        parameters('code) { verificationCode =>
          onComplete(facebookService.createPageAccessToken(verificationCode)) {
            case Success(_) => complete(StatusCodes.OK -> None)
            case Failure(ex) => failWith(ex)
          }
        }
      }
    }

  private def startSchedulerRoute =
    path("scheduler" / "start") {
      get {
        SchedulerService.start(facebookService)
        complete(StatusCodes.OK -> None)
      }
    }


  //  private def googleCallbackRoute =
  //    path("Callback") {
  //      get {
  //        onComplete(Http().singleRequest(
  //          HttpRequest(
  //            method = HttpMethods.GET,
  //            uri = Uri("http://localhost:44497")
  //          )
  //        )) {
  //          case Success(response) =>
  //            response.status match {
  //              case StatusCodes.OK =>
  //                complete(StatusCodes.OK -> "YEST!")
  //              case _ =>
  //                logger.error(response.entity.toString)
  //                complete(StatusCodes.OK -> "DUPA")
  //            }
  //          case Failure(ex) => failWith(ex)
  //        }
  //      }
  //    }

  private def helloWorldRoute =
    path("") {
      get {
        complete(StatusCodes.OK -> "Hello world")
      }
    }

}

trait CustomExceptionHandler {
  implicit def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: Exception => ctx => {
      ctx.complete(StatusCodes.BadRequest -> e.getMessage)
    }
  }
}