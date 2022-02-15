package objektwerks.poolmate

import com.raquo.laminar.api.L.*

import org.scalajs.dom.console.log

import Components.*

object AccountView:
  def apply(account: Var[Account]): HtmlElement =
    div(
      bar(
        btn("Pools").amend {
          onClick --> { _ =>
            log("Pools onClick")
            PageRouter.router.pushState(PoolsPage)
          }
        }      
      ),
      div(
        hdr("Account"),
        lbl("License"),
        rotxt.amend {
          value <-- account.signal.map(_.license)
        },
        lbl("Email Address"),
        rotxt.amend {
          value <-- account.signal.map(_.emailAddress)
        },
        lbl("Pin"),
        rotxt.amend {
          value <-- account.signal.map(_.pin)
        },
        lbl("Activated"),
        rotxt.amend {
          value <-- account.signal.map(_.activated.toString)
        },
        lbl("Deactivated"),
        rotxt.amend {
          value <-- account.signal.map(_.deactivated.toString)
        }
      )
    )