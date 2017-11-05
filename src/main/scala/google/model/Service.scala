package google.model

import java.io.InputStreamReader

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.CalendarScopes
import com.typesafe.scalalogging.LazyLogging

import scala.util.Properties

/**
  * Created by kuba on 28/05/2017.
  */
trait Service extends LazyLogging {

  val httpTransport = new NetHttpTransport
  val jsonFactory = new JacksonFactory
  val credential = getClientCredentials

  private def getClientCredentials: Credential = {
    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
    val DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/calendar-java-quickstart")
    val DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR)

    val JSON_FACTORY = JacksonFactory.getDefaultInstance
    // Load client secrets.
    val in = this.getClass.getResourceAsStream("/client_id.json")
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in))

    // Build flow and trigger user authorization request.
    val flow = new GoogleAuthorizationCodeFlow.Builder(
      HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, CalendarScopes.all())
      .setDataStoreFactory(DATA_STORE_FACTORY)
      .setAccessType("offline")
      .build()

    val serverReceiver = new LocalServerReceiver.Builder()
      .setHost("http://remindo.nazwa.pl")
      .setPort(Properties.envOrElse("PORT", "8081").toInt)
      .build()

    val credential = new AuthorizationCodeInstalledApp(flow, serverReceiver).authorize("user")
    logger.info("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath)

    credential
  }

}
