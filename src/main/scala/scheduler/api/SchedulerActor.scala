package scheduler.api

import akka.actor.{Actor, ActorLogging, Props}
import com.restfb.types.Conversation
import common.ActorSystemSupport
import facebook.service.FacebookService
import google.service.CalendarService
import scheduler.api.SchedulerActorMessages.SendNotifications

import scala.collection.JavaConverters._
import scala.util.{Failure, Success}

/**
  * Created by kuba on 28/05/2017.
  */
object SchedulerActor {

  def props(calendarService: CalendarService, facebookService: FacebookService): Props =
    Props(new SchedulerActor(calendarService, facebookService))

}

class SchedulerActor(calendarService: CalendarService, facebookService: FacebookService)
  extends Actor with ActorLogging with ActorSystemSupport {

  override def receive: Receive = {
    case SendNotifications => handleSendNotifications()

    case msg: Any => log.info(s"UNSUPPORTED MESSAGE $msg")
  }

  def handleSendNotifications(): Unit =
    calendarService.listEvents.foreach {_.foreach { event =>
        facebookService.fetchConversations().foreach { conversation =>
          if (isEventCorrelatedWithConversation(event.title, conversation)) {
            facebookService.sendNotification(conversation.getId, event.startTime.get).onComplete {
              case Success(_) => calendarService.update(event).onComplete {
                case Success(_) => log.info(s"Event for ${event.title} updated")
                case Failure(error) => log.error(error.getMessage)
              }
              case Failure(error) => log.error(error.getMessage)
            }
          }
        }
      }
    }

  private def isEventCorrelatedWithConversation(eventTitle: String, conversation: Conversation): Boolean = {
    val participantsNames = conversation.getParticipants.asScala.map(_.getName)
    participantsNames.contains(eventTitle)
  }

  //    calendar.create(
  //      Event("TEST EVENT", Some("description"), Some(ZonedDateTime.now().plusDays(1)), Some(ZonedDateTime.now().plusDays(1).plusMinutes(30))),
  //      sendNotifications = false
  //    ).foreach(println)

}

object SchedulerActorMessages {

  case object SendNotifications

  case object Create

  case object Update

}
