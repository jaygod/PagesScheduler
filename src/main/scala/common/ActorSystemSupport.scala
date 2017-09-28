package common

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import scala.concurrent.duration._

trait ActorSystemSupport {

  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = actorSystem.dispatcher
  implicit val timeout: Timeout = Timeout(10.seconds)

}
