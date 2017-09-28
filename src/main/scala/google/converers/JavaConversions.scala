package google.converers

import com.google.api.services.{calendar => jCalendar}
import google.model.{Calendar, Event}

import scala.language.{implicitConversions, postfixOps}

object JavaConversions {


  implicit def scalaCalendarAsJavaCalendarConversion(service: Calendar): jCalendar.Calendar = {
    new com.google.api.services.calendar.Calendar.Builder(service.httpTransport, service.jsonFactory, service.credential)
      .setApplicationName(service.applicationName)
      .setHttpRequestInitializer(service.credential)
      .build()
  }

  implicit def scalaEventAsJavaEventConversion(b: Event): jCalendar.model.Event = {
    import collection.JavaConverters._
    val event = new jCalendar.model.Event

    val participants = b.participantEmails
      .map(
        _.map(
          email => new jCalendar.model.EventAttendee().setEmail(email)
        )
      )

    event.setSummary(b.title)
    if (b.description isDefined) {
      event.setDescription(b.description.get)
    }
    if (b.participantEmails isDefined) {
      event.setAttendees(participants.get.asJava)
    }
    if (b.startTime isDefined) {
      event.setStart(b.startTime.get)
    }
    if (b.endTime isDefined) {
      event.setEnd(b.endTime.get)
    }
    if (b.recurrence isDefined) {
      event.setRecurrence(b.recurrence.get.asJava)
    }
    if (b.id isDefined) {
      event.setId(b.id.get)
    }
    event
  }

  implicit def javaEventAsScalaEventConversion(b: jCalendar.model.Event): Event = {
    import collection.JavaConverters._
    Event(
      b.getSummary, {
        if (b.getDescription != "") Some(b.getDescription) else None
      }, {
        Option(b.getStart)
      }, {
        Option(b.getEnd)
      },

      Option(b.getAttendees)
        .map(attendees => attendees.asScala.toList)
        .map(listOfAttendees => listOfAttendees.map(_.getEmail)),
      Option(b.getRecurrence).map(_.asScala.toList),
      Option(b.getId)
    )
  }

  implicit def javaZonedDateTimeAsGoogleDateTimeConversion(b: java.time.ZonedDateTime)
  : com.google.api.client.util.DateTime = {

    new com.google.api.client.util.DateTime(b.toInstant.toString)
  }

  implicit def googleDateTimeAsJavaZoneDateTimeConversion(b: com.google.api.client.util.DateTime)
  : java.time.ZonedDateTime = {
    java.time.ZonedDateTime.parse(b.toStringRfc3339.toCharArray)
  }

  implicit def javaZonedDateTimeAsGoogleEventDateTimeConversion(b: java.time.ZonedDateTime)
  : jCalendar.model.EventDateTime = {

    new jCalendar.model.EventDateTime()
      .setDateTime(b)
      .setTimeZone(b.getZone.toString)
  }

  implicit def googleEventDateTimeAsJavaZonedDateTimeConversion(b: jCalendar.model.EventDateTime)
  : java.time.ZonedDateTime = {

    val formatter = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
    java.time.ZonedDateTime.parse(b.getDateTime.toStringRfc3339.toCharArray, formatter)
  }


}
