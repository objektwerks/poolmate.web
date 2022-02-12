package objektwerks.poolmate

import com.raquo.laminar.api.L.*

import org.scalajs.dom.console.log

import Components.*
import Validators.*

object LoginView:
  def apply(emailAddress: Var[String], pin: Var[String]): HtmlElement =
    val emailAddressError = new EventBus[String]
    val pinError = new EventBus[String]
    frm(
      hdr("Login"),
      lbl("Email"),
      txt.amend {
        typ("email")
        minLength(3)
        required(true)
        placeholder("address@email.com")
        value <-- emailAddress
        onInput.mapToValue.filter(_.nonEmpty).setAsValue --> emailAddress
        onKeyUp.mapToValue --> { value =>
          if value.isEmailAddress then emailAddressError.emit("")
          else emailAddressError.emit("Enter a valid email address.")
        }
      },
      err(emailAddressError),
      lbl("Pin"),
      txt.amend {
        typ("text")
        minLength(6)
        maxLength(6)
        required(true)
        placeholder("abc123")
        value <-- pin
        onInput.mapToValue.filter(_.nonEmpty).setAsValue --> pin
        onKeyUp.mapToValue --> { value =>
          if value.isPin then pinError.emit("")
          else pinError.emit("Enter a valid 6-character pin.")
        }      
      },
      err(pinError),
      cbar(
        btn("Login").amend {
          disabled <-- emailAddress.signal.combineWithFn(pin.signal) {
            (email, pin) => !(email.isEmailAddress && pin.isPin)
          }
        }
      ).amend {
        onSubmit --> { event =>
          event.preventDefault()
          log(s"email address: ${emailAddress.now()} pin: ${pin.now()}")
          PageRouter.router.pushState(IndexPage)
        }  
      }
    )