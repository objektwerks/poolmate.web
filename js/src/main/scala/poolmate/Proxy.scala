package poolmate

import com.raquo.laminar.api.L.*

import org.scalajs.dom
import org.scalajs.dom.Headers
import org.scalajs.dom.HttpMethod
import org.scalajs.dom.RequestInit
import org.scalajs.dom.console.log

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Thenable.Implicits.*

import Serializer.given

import upickle.default.*

object Proxy:
  private val hdrs = new Headers {
      js.Array(
        js.Array("Content-Type", "application/json; charset=utf-8"),
        js.Array("Accept", "application/json")
      )
    }
  private val params = new RequestInit {
    method = HttpMethod.POST
    headers = hdrs
  }

  def now: Future[String] =
    ( 
      for
        response <- dom.fetch(Url.now)
        text     <- response.text()
      yield text
    ).recover {
      case failure: Exception => s"Now failed: ${failure.getMessage}"
    }

  def call(command: Command,
           handler: (either: Either[Fault, Event]) => Unit) =
    val event = post(command)
    handle(event, handler)

  private def post(command: Command): Future[Event] =
    log(s"Proxy:post command: $command")
    params.body = write[Command](command)
    log(s"Proxy:post request: ${params.body}")
    (
      for
        response <- dom.fetch(Url.command, params)
        text     <- response.text()
      yield
        log(s"Proxy:post response: $text")
        val event = read[Event](text)
        log(s"Proxy:post event: $event")
        event
    ).recover {
      case failure: Exception =>
        log(s"Proxy:post failure: $failure")
        Fault(failure)
    }

  private def handle(future: Future[Event],
                     handler: (either: Either[Fault, Event]) => Unit): Unit =
    future map { event =>
      handler(
        event match
          case fault: Fault =>
            log(s"Proxy:handle fault: $fault")
            Left(fault)
          case event: Event =>
            log(s"Proxy:handle event: $event")
            Right(event)
      )
    }