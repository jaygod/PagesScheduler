package facebook.model

import spray.json.JsValue

object FacebookDto {

  final case class PageData(data: Seq[PageAccessToken], paging: JsValue)

  final case class PageAccessToken(access_token: String)

  final case class AuthUrl(url: String)

}