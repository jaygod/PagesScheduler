package google.converers

import com.google.api.services.{calendar => jCalendar}
import google.converers.JavaConversions._
import google.model.{Calendar, Event}

import scala.language.implicitConversions

object JavaConverters {

  class AsJava[C](op: => C) {
    def asJava: C = op
  }

  class AsScala[C](op: => C) {
    def asScala: C = op
  }

  class AsGoogle[C](op: => C) {
    def asGoogle: C = op
  }

  implicit def scalaCalendarAsJavaCalendarConverter(b: Calendar): AsJava[jCalendar.Calendar] = {
    new AsJava(scalaCalendarAsJavaCalendarConversion(b))
  }

  implicit def scalaEventAsJavaEventConverter(b: Event): AsJava[jCalendar.model.Event] = {
    new AsJava(scalaEventAsJavaEventConversion(b))
  }

  implicit def javaEventAsScalaEventConverter(b: jCalendar.model.Event): AsScala[Event] = {
    new AsScala(javaEventAsScalaEventConversion(b))
  }

  implicit def javaZonedDateTimeAsGoogleDateTimeConverter(b: java.time.ZonedDateTime)
  : AsGoogle[com.google.api.client.util.DateTime] = {
    new AsGoogle(javaZonedDateTimeAsGoogleDateTimeConversion(b))
  }

  implicit def googleDateTimeAsJavaZonedDateTimeConverter(b: com.google.api.client.util.DateTime)
  : AsJava[java.time.ZonedDateTime] = {
    new AsJava(googleDateTimeAsJavaZoneDateTimeConversion(b))
  }


}
