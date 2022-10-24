package workorder

import com.raquo.laminar.api.L.*

import scala.scalajs.js.Date

object Components:
  private val inputCss = "w3-input w3-hover-light-gray w3-text-indigo"

  def bar(elms: HtmlElement*): Div =
    div(cls("w3-bar"), elms)

  def cbar(elms: HtmlElement*): Div =
    div(cls("w3-bar w3-margin-top w3-center"), elms)

  def btn(text: String): Button =
    button(cls("w3-button w3-round w3-indigo"), text)

  def rbtn(text: String): Button =
    button(cls("w3-button w3-round w3-indigo w3-right"), text)

  def lbl(text: String): Label =
    label(cls("w3-left-align w3-text-indigo"), text)

  def txt: Input =
    input(cls(inputCss), required(true))

  def rotxt: Input =
    input(cls("w3-input w3-light-gray w3-text-indigo"), readOnly(true))

  def txtarea(rowCount: Int = 2, isReadOnly: Boolean = false): TextArea =
    textArea(cls("w3-hover-light-gray w3-text-indigo"), rows(rowCount), readOnly(isReadOnly))

  def email: Input =
    input(cls(inputCss), typ("email"), minLength(3), required(true))

  def pin: Input =
    input(cls(inputCss), typ("text"), minLength(7), maxLength(7), required(true))

  def int: Input =
    input(cls(inputCss), typ("number"), pattern("\\d*"), stepAttr("1"), required(true))

  def dbl: Input =
    input(cls(inputCss), typ("number"), pattern("[0-9]+([.,][0-9]+)?"), stepAttr("0.01"), required(true))

  def hdr(text: String): HtmlElement =
    h5(cls("w3-indigo"), text)

  def err(errBus: EventBus[String]): Div =
    div(cls("w3-container w3-border-white w3-text-red"), child.text <-- errBus.events)

  def street: Input =
    input(cls(inputCss), minLength(7), required(true))

  def roles: Select =
    select(cls("w3-select w3-text-indigo"),
      option(Roles.homeowner, selected(true)),
      option(Roles.serviceProvider)
    )

  def list(items: List[String]): Select =
    select(cls("w3-select w3-text-indigo"),
      children <-- Var(items.map(item => option(item))).signal
    )

  def list(items: Signal[List[Li]]): HtmlElement =
    ul(cls("w3-ul w3-hoverable"), children <-- items)

  def listServiceProviders(serviceProviders: Var[List[User]]): Signal[List[Li]] =
    serviceProviders.signal.split(_.id)((id, _, serviceProviderSignal) =>
      item(serviceProviderSignal.map(_.name)).amend {
        onClick --> { _ =>
          serviceProviders.now().find(_.id == id).foreach { serviceProvider =>
            Model.workOrderVar.update(workOrder => workOrder.copy(serviceProviderId = serviceProvider.id))
          }
        }
      }
    )

  def listWorkOrders(workOrders: Var[List[WorkOrder]]): Signal[List[Li]] =
    workOrders.signal.split(_.number)((number, _, workOrderSignal) =>
      item(workOrderSignal.map(_.title)).amend {
        onClick --> { _ =>
          workOrders.now().find(_.number == number).foreach { workOrder =>
            Model.workOrderVar.set(workOrder)
            PageRouter.router.pushState(WorkOrderPage)
          }
        }
      }
    )

  def item(strSignal: Signal[String]): Li =
    li(cls("w3-text-indigo w3-display-container"), child.text <-- strSignal)