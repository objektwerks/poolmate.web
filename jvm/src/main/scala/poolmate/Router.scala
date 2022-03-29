package poolmate

import cask.main.Routes
import cask.model.{Request, Response}
import com.typesafe.scalalogging.LazyLogging

import java.time.Instant

import Serializers.given

import upickle.default.{read, write}

final class Router(dispatcher: Dispatcher, store: Store) extends Routes with LazyLogging:
  @cask.get("/now")
  def index() = Response(Instant.now.toString)

  @cask.post("/command")
  def command(request: Request) =
    val command = read[Command](request.text())
    logger.debug(s"*** Command: $command")

    val event = dispatcher.dispatch(command)
    logger.debug(s"*** Event: $event")
    event match {
      case unauthorized: Unauthorized =>
        logger.error(s"Router unauthorized: $unauthorized")
        store.addFault( Fault( s"Router unauthorized: ${unauthorized.license}" ) )
      case fault: Fault =>
        logger.error(s"Router fault: $fault")
        store.addFault(fault)
      case _ =>
    }
    write[Event](event)

  initialize()