package poolmate

import com.raquo.laminar.api.L.*

import org.scalajs.dom.console.log

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import Components.*
import Error.*
import Validators.*

object RegisterView extends View:
  def apply(emailAddressVar: Var[String]): HtmlElement =
    val emailAddressErrorBus = new EventBus[String]

    def handler(event: Either[Fault, Event]): Unit =
      event match
        case Right(event) =>
          event match
            case Registering() =>
              clearErrors()
              route(LoginPage)
            case _ =>
        case Left(fault) => errorBus.emit(s"Register failed: ${fault.cause}")
      
    div(
      hdr("Register"),
      err(errorBus),
      lbl("Email Address"),
      email.amend {
        value <-- emailAddressVar
        onInput.mapToValue.filter(_.nonEmpty).setAsValue --> emailAddressVar
        onKeyUp.mapToValue --> { emailAddress =>
          if emailAddress.isEmailAddress then emailAddressErrorBus.emit("") else emailAddressErrorBus.emit(emailAddressError)
        }
      },
      err(emailAddressErrorBus),
      cbar(
        btn("Register").amend {
          disabled <-- emailAddressVar.signal.map(email => !email.isEmailAddress)
          onClick --> { _ =>
            log(s"Register onClick -> email address: ${emailAddressVar.now()}")
            val command = Register(emailAddressVar.now())
            call(command, handler)
          }
        },
      )
    )