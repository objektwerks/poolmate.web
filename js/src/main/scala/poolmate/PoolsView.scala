package poolmate

import com.raquo.laminar.api.L.*

import org.scalajs.dom.console.log

import Components.*

object PoolsView extends View:
  def apply(model: Model[Pool], accountVar: Var[Account]): HtmlElement =
    def handler(event: Either[Fault, Event]): Unit =
      event match
        case Right(event) =>
          event match
            case PoolsListed(pools: Seq[Pool]) =>
              clearErrors()
              model.setEntities(pools)
            case _ =>
        case Left(fault) => errorBus.emit(s"List pools failed: ${fault.cause}")

    div(
      bar(
        btn("Account").amend {
          onClick --> { _ =>
            log("Pools -> Account menu item onClick")
            route(AccountPage)
          }
        }      
      ),
      div(
        onLoad --> { _ => call(ListPools(accountVar.now().license), handler) },
        hdr("Pools"),
        list(
          split(model.entitiesVar, (id: Long) => PoolPage(id))
        )
      ),
      cbar(
        btn("Add").amend {
          onClick --> { _ =>
            log(s"Pools -> Add onClick")
            route(PoolPage())
          }
        }
      )
    )