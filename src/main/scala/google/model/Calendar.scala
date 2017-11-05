package google.model

import java.time.ZonedDateTime

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import google.converers.JavaConverters._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by kuba on 28/05/2017.
  */

class Calendar()(implicit executionContext: ExecutionContext) extends Service with LazyLogging {

  private val config = ConfigFactory.load().getConfig("google")
  val applicationName = config.getString("applicationName")
  val eventUpdatedAddon = config.getString("eventUpdatedAddon")

  private val instanceAsJava = this.asJava

  def listEvents(from: ZonedDateTime, to: ZonedDateTime): Future[List[Event]] = Future {
    val jEvents = instanceAsJava.events.list("primary")
      .setMaxResults(30)
      .setTimeMin(from.asGoogle)
      .setTimeMax(to.asGoogle)
      .setOrderBy("startTime")
      .setSingleEvents(true)
      .execute()
      .getItems

    jEvents.asScala.toList.map(_.asScala)
  }

  def create(event: Event, calendar: String = "primary", sendNotifications: Boolean = true): Future[Event] = Future {
    instanceAsJava.events()
      .insert(calendar, event.asJava)
      .setSendNotifications(sendNotifications)
      .execute()
      .asScala
  }

  def update(event: Event, calendar: String = "primary"): Future[Event] = Future {
    require(event.id.isDefined)
    try {
      val eventU = instanceAsJava.events()
        .update(calendar, event.id.get,
          Event(
            event.title + eventUpdatedAddon,
            event.description,
            event.startTime,
            event.endTime,
            event.participantEmails,
            event.recurrence,
            event.id
          ).asJava
        )
        .execute()

      eventU.asScala
    } catch {
      case e: Throwable =>
        logger.error("Exception", e)
        event
    }
  }


}
