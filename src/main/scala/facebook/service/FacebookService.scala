package facebook.service

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.restfb.FacebookClient.AccessToken
import com.restfb.scope.{ExtendedPermissions, ScopeBuilder}
import com.restfb.types.Conversation
import com.restfb.types.send.SendResponse
import com.restfb.{DefaultFacebookClient, Parameter, Version}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import common.ActorSystemSupport
import facebook.api.FacebookProtocol
import facebook.data.TokenRepository
import facebook.model.FacebookDto.{AuthUrl, PageAccessToken, PageData}

import scala.collection.JavaConverters._
import scala.concurrent.Future

class FacebookService(pageAccessTokenOption: Option[String])
  extends LazyLogging with ActorSystemSupport with FacebookProtocol {

  private val facebookConfig = ConfigFactory.load().getConfig("facebook")
  private val userId = facebookConfig.getString("userId")
  private val appId = facebookConfig.getString("appId")
  private val appSecret = facebookConfig.getString("appSecret")
  private val callbackUrl = facebookConfig.getString("callbackUrl")
  private val pageAccessTokenUrl = facebookConfig.getString("pageAccessTokenUrl")
  private val tokenRepository = new TokenRepository()
  private val scope = new ScopeBuilder()
    .addPermission(ExtendedPermissions.MANAGE_PAGES)
    .addPermission(ExtendedPermissions.PAGES_SHOW_LIST)
    .addPermission(ExtendedPermissions.READ_PAGE_MAILBOXES)
    .addPermission(ExtendedPermissions.PAGES_MESSAGING)
  private var fbClient: DefaultFacebookClient = _

  pageAccessTokenOption match {
    case Some(pageAccessToken) =>
      fbClient = new DefaultFacebookClient(pageAccessToken, Version.LATEST)
    case None =>
      fbClient = new DefaultFacebookClient(Version.LATEST)
  }

  def getLoginDialogUrl: Future[AuthUrl] = Future {
    AuthUrl(fbClient.getLoginDialogUrl(appId, callbackUrl, scope))
  }

  def createPageAccessToken(verificationCode: String): Future[Option[PageAccessToken]] = {
    val userShortLivedToken = fbClient.obtainUserAccessToken(appId, appSecret, callbackUrl, verificationCode)
    val longLivedAccessToken = fbClient.obtainExtendedAccessToken(appId, appSecret, userShortLivedToken.getAccessToken)

    getPageAccessToken(longLivedAccessToken).flatMap {
      case Some(pageAccessToken) =>
        fbClient = new DefaultFacebookClient(pageAccessToken.access_token, Version.LATEST)
        tokenRepository.insert(pageAccessToken.access_token).map(_ => Some(pageAccessToken))
      case None =>
        Future.successful(None)
    }
  }

  def fetchConversations(): Seq[Conversation] =
    fbClient.fetchConnection[Conversation](
      s"$userId/conversations",
      classOf[Conversation],
      Parameter.`with`("fields", "id,participants,updated_time")
    ).getData.asScala


  def sendNotification(conversationId: String, eventStartTime: ZonedDateTime): Future[SendResponse] = Future {
    val dayOfWeek = toLocalizedDayOfWeek(DateTimeFormatter.ofPattern("EEEE").format(eventStartTime))
    val eventDateFormatted = DateTimeFormatter.ofPattern(s"dd.MM - HH:mm").format(eventStartTime)

    fbClient.publish[SendResponse](s"$conversationId/messages", classOf[SendResponse],
      Parameter.`with`("message", s"Proszę o potwierdzenie terminu wizyty $eventDateFormatted ($dayOfWeek)"))
  }

  private def getPageAccessToken(longLivedUserAccessToken: AccessToken): Future[Option[PageAccessToken]] =
    Http().singleRequest(
      HttpRequest(
        method = HttpMethods.GET,
        headers = List(headers.Accept(MediaRanges.`application/*`)),
        uri = Uri(pageAccessTokenUrl.format(Version.LATEST.getUrlElement, longLivedUserAccessToken.getAccessToken))
      )
    ).flatMap { response =>
      response.status match {
        case StatusCodes.OK =>
          logger.info("Generated new page access token")
          Unmarshal(response.entity).to[PageData].map(pageAccessToken => Some(pageAccessToken.data.head))
        case _ =>
          logger.error(response.entity.toString)
          Future.successful(None)
      }
    }

  private def toLocalizedDayOfWeek(dayOfWeek: String): String = dayOfWeek match {
    case "Monday" => "Poniedziałek"
    case "Tuesday" => "Wtorek"
    case "Wednesday" => "Środa"
    case "Thursday" => "Czwartek"
    case "Friday" => "Piątek"
    case "Saturday" => "Sobota"
    case "Sunday" => "Niedziela"
  }

}
