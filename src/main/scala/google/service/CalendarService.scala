package google.service

import java.time.ZonedDateTime

import com.typesafe.scalalogging.LazyLogging
import common.ActorSystemSupport
import google.model.{Calendar, Event}

import scala.concurrent.Future

class CalendarService extends LazyLogging with ActorSystemSupport {

  private val calendar = new Calendar()

  def listEvents: Future[List[Event]] =
    calendar
      .listEvents(ZonedDateTime.now(), ZonedDateTime.now().plusDays(1))
      .map(filterAlreadyUpdatedEvents)

  def update(event: Event): Future[Event] =
    calendar.update(event)

  private def filterAlreadyUpdatedEvents(events: List[Event]): List[Event] =
    events.filterNot(_.title.contains(calendar.eventUpdatedAddon))

}
