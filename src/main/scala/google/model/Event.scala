package google.model

import java.time.ZonedDateTime

/**
  * Created by kuba on 28/05/2017.
  */
case class Event(
                  title: String,
                  description: Option[String],
                  startTime: Option[ZonedDateTime],
                  endTime: Option[ZonedDateTime],
                  participantEmails: Option[List[String]] = None,
                  recurrence: Option[List[String]] = None,
                  id: Option[String] = None,
                  color: Option[String] = None
                )

