package poolmate

import com.raquo.laminar.api.L.*

import org.scalajs.dom.console.log

import Components.*
import Validators.*

object AccountView extends View:
  def apply(accountVar: Var[Account]): HtmlElement =
    def deactivateHandler(event: Either[Fault, Event]): Unit =
      event match
        case Right(event) =>
          event match
            case Deactivated(account) =>
              clearErrors()
              accountVar.set(account)
              route(AppPage)
            case _ => log(s"Account deactivate view handler failed: $event")
        case Left(fault) => errorBus.emit(s"Deactivate failed: ${fault.cause}")
 
    def reactivateHandler(event: Either[Fault, Event]): Unit =
      event match
        case Right(event) =>
          event match
            case Reactivated(account) =>
              clearErrors()
              accountVar.set(account)
              route(AppPage)
            case _ => log(s"Account reactivate view handler failed: $event")
        case Left(fault) => errorBus.emit(s"Reactivate failed: ${fault.cause}")

    div(
      bar(
        btn("App").amend {
          onClick --> { _ =>
            log("Account -> Home onClick")
            route(AppPage)
          }
        }      
      ),
      div(
        hdr("Account"),
        lbl("License"),
        rotxt.amend {
          value <-- accountVar.signal.map(_.license)
        },
        lbl("Pin"),
        rotxt.amend {
          value <-- accountVar.signal.map(_.pin)
        },
        lbl("Activated"),
        rotxt.amend {
          value <-- accountVar.signal.map(_.activated.toString)
        },
        lbl("Deactivated"),
        rotxt.amend {
          value <-- accountVar.signal.map(_.deactivated.toString)
        },
        cbar(
          btn("Deactivate").amend {
            disabled <-- accountVar.signal.map { account => if account.isDeactivated then true else false }
            onClick --> { _ =>
              log("Account -> Deactivate onClick")
              val command = Deactivate(accountVar.now().license)
              call(command, deactivateHandler)
            }
          },
          btn("Reactivate").amend {
            disabled <-- accountVar.signal.map { account => if account.isActivated then true else false }
            onClick --> { _ =>
              log("Account -> Reactivate onClick")
              val command = Reactivate(accountVar.now().license)
              call(command, reactivateHandler)
            }
          }      
        )
      )
    )