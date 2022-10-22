package workorder

import com.raquo.laminar.api.L.*

import org.scalajs.dom.console.log

import Components.*
import Validator.*

object ProfileView extends View:
  def apply(): HtmlElement =
    def handler(either: Either[Fault, Event]): Unit =
      either match
        case Left(fault) => errorBus.emit(s"Save profile failed: ${fault.cause}")
        case Right(event) =>
          event match
            case UserSaved(_, _, _) =>
              clearErrors()
              route(WorkOrdersPage)
            case _ => log(s"Profile -> handler failed: $event")

    div(
      bar(
        btn("Work Orders").amend {
          onClick --> { _ =>
            log("Profile -> Work Orders menu item onClick")
            route(WorkOrdersPage)
          }
        }      
      ),
      div(
        hdr("Profile"),
        lbl("License"),
        rotxt.amend {
          value <-- Model.userVar.signal.map(_.license)
        },
        lbl("Pin"),
        rotxt.amend {
          value <-- Model.userVar.signal.map(_.pin)
        },
        cbar(
          btn("Save").amend {
            onClick --> { _ =>
              log("Profile -> Save button onClick")
              val command = SaveUser(Model.userVar.now())
              call(command, handler)
            }
          } 
        )
      )
    )