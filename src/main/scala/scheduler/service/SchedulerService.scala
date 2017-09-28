package scheduler.service

import com.typesafe.scalalogging.LazyLogging
import common.ActorSystemSupport
import facebook.service.FacebookService
import google.service.CalendarService
import scheduler.api.SchedulerActor
import scheduler.api.SchedulerActorMessages._

import scala.concurrent.duration._

/**
  * Created by kuba on 28/05/2017.
  */
object SchedulerService extends LazyLogging with ActorSystemSupport {

  def start(facebookService: FacebookService): Unit = {
    val calendarService = new CalendarService() // TODO
    val actor = actorSystem.actorOf(SchedulerActor.props(calendarService, facebookService))
    val _ = actorSystem.scheduler.schedule(500.milliseconds, 10.second, actor, SendNotifications)
    logger.info("PagesScheduler started!")
  }

}
