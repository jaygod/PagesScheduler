package facebook.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import facebook.model.FacebookDto.{AuthUrl, PageAccessToken, PageData}
import spray.json.DefaultJsonProtocol

trait FacebookProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val pageAccessTokenFormat2 = jsonFormat1(PageAccessToken)
  implicit val pageDataFormat = jsonFormat2(PageData)
  implicit val authUrlFormat = jsonFormat1(AuthUrl)

}
