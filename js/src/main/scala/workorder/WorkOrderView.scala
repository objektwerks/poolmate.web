package workorder

import com.raquo.laminar.api.L.*

import org.scalajs.dom.console.log

import Components.*
import Validator.*

object WorkOrderView extends View:
  def apply(): HtmlElement =
    val role = Model.userVar.now().role
    Model.workOrderMode match
      case Mode.add => add(role)
      case Mode.edit => edit(role)
      case Mode.readonly => readonly()

  def add(role: String): HtmlElement =
    val titleErrorBus = new EventBus[String]
    val issueErrorBus = new EventBus[String]
    val streetAddressErrorBus = new EventBus[String]
    val resolutionErrorBus = new EventBus[String]

    div(
      bar(
        btn("Work Orders").amend {
          onClick --> { _ => route(WorkOrdersPage) }
        }      
      ),
      hdr("Work Order"),
      err(errorBus),
      lbl("Number"),
      rotxt.amend {
        value <-- Model.workOrderVar.signal.map(_.number.toString)
      },
    )

  def edit(role: String): HtmlElement =
    val titleErrorBus = new EventBus[String]
    val issueErrorBus = new EventBus[String]
    val streetAddressErrorBus = new EventBus[String]
    val resolutionErrorBus = new EventBus[String]
    
    div(
      bar(
        btn("Work Orders").amend {
          onClick --> { _ => route(WorkOrdersPage) }
        }      
      ),
      hdr("Work Order"),
      err(errorBus),
      lbl("Number"),
      rotxt.amend {
        value <-- Model.workOrderVar.signal.map(_.number.toString)
      },
    )

  def readonly(): HtmlElement =
    div(

    )

  def handler(either: Either[Fault, Event]): Unit =
    either match
      case Left(fault) => errorBus.emit(s"Save work order failed: ${fault.cause}")
      case Right(event) =>
        event match
          case WorkOrderAdded(_, _, _) | WorkOrderSaved(_, _, _) =>
            clearErrorBus()
            route(WorkOrdersPage)
          case _ => log("work order view: handler failed: %o", event)

  def refactor(): HtmlElement =
    div(
      lbl("Homeowner"),
      rotxt.amend {
        value <-- Model.workOrderVar.signal.map(workOrder => Model.userName(workOrder.homeownerId))
      },
      lbl("Service Provider"),
      rotxt.amend {
        value <-- Model.workOrderVar.signal.map(workOrder => Model.userName(workOrder.serviceProviderId))
      },
      lbl("Service Providers"),
      list( listServiceProviders(Model.serviceProvidersVar) ),
      lbl("Title"),
      txt.amend {
        onInput.mapToValue.filter(_.nonEmpty) --> { value =>
          Model.workOrderVar.update(workOrder => workOrder.copy(title = value))
        }
        onKeyUp.mapToValue --> { value =>
          if value.isTitle then clear(titleErrorBus)
          else emit(titleErrorBus, titleInvalid)
        }
      },
      err(titleErrorBus),
      lbl("Issue"),
      txt.amend {
        onInput.mapToValue.filter(_.nonEmpty) --> { value =>
          Model.workOrderVar.update(workOrder => workOrder.copy(issue = value))
        }
        onKeyUp.mapToValue --> { value =>
          if value.isIssue then clear(issueErrorBus)
          else emit(titleErrorBus, issueInvalid)
        }
      },
      err(issueErrorBus),
      lbl("Street Address"),
      street.amend {
        onInput.mapToValue.filter(_.nonEmpty) --> { value =>
          Model.workOrderVar.update(workOrder => workOrder.copy(streetAddress = value))
        }
        onKeyUp.mapToValue --> { value =>
          if value.isStreetAddress then clear(streetAddressErrorBus)
          else emit(streetAddressErrorBus, streetAddressInvalid)
        }
      },
      err(streetAddressErrorBus),
      lbl("Resolution"),
      txtarea().amend {
        onInput.mapToValue.filter(_.nonEmpty) --> { value =>
          Model.workOrderVar.update(workOrder => workOrder.copy(resolution = value))
        }
        onKeyUp.mapToValue --> { value =>
          if value.isResolution then clear(resolutionErrorBus)
          else emit(resolutionErrorBus, titleInvalid)
        }
      },
      err(resolutionErrorBus),
      lbl("Opened"),
      rotxt.amend {
        value <-- Model.workOrderVar.signal.map(_.opened)
      },
      lbl("Closed"), // TODO Need checkbox!
      rotxt.amend {
        value <-- Model.workOrderVar.signal.map(_.closed)
      },
      cbar(
        btn("Save").amend {
          disabled <-- Model.workOrderVar.signal.map { workOrder => !workOrder.isValid }
          onClick --> { _ =>
            val command = SaveWorkOrder(Model.workOrderVar.now(), Model.userVar.now().license)
            log("work order view: save button onClick command: %o", command)
            call(command, handler)
          }
        })
    )
