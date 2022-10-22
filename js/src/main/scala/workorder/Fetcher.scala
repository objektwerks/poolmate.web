package workorder

import com.raquo.laminar.api.L._

import org.scalajs.dom
import org.scalajs.dom.BlobPart
import org.scalajs.dom.File
import org.scalajs.dom.FormData
import org.scalajs.dom.Headers
import org.scalajs.dom.HttpMethod
import org.scalajs.dom.RequestInit
import org.scalajs.dom.console.log

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Thenable.Implicits.*

import Serializer.given

import upickle.default.{read, write}

object Fetcher:
  val post = "post"
  val jsonHeaders = new Headers {
    js.Array(
      js.Array("Content-Type", "application/json; charset=UTF-8"),
      js.Array("Accept", "application/json")
    )
  }
  val jsonParameters = new RequestInit {
    method = HttpMethod.POST
    headers = jsonHeaders
  }
  val formDataHeaders = new Headers {
    js.Array()
  }
  val formDataParameters = new RequestInit {
    method = HttpMethod.POST
    headers = formDataHeaders
  }

  def now: Future[String] =
    (
      for
        response <- dom.fetch(Urls.now)
        text <- response.text()
      yield text
    ).recover { case error: Exception =>
      s"Now failed: ${error.getMessage}"
    }

  // Uncomment to enable client formdata support.
  def call(command: Command, handler: (either: Either[Fault, Event]) => Unit) =
    val event: Future[Event] = command match
      case Register(_, _, _, _) => post(command, jsonParameters, Urls.register)
      case Login(_, _) => post(command, jsonParameters, Urls.login)
      case SaveUser(_) => post(command, jsonParameters, Urls.userSave)
      case AddWorkOrder(_, _) =>
        // addWorkOrder: AddWorkOrder =>
        // val formData = addWorkOrderToFormData(addWorkOrder, Model.imageFile)
        // post(formData, formDataParameters, Urls.workOrderAdd)
        post(command, jsonParameters, Urls.workOrderAdd)
      case SaveWorkOrder(_, _) =>
        // saveWorkOrder: SaveWorkOrder =>
        // val formData = saveWorkOrderToFormData(command, Model.imageFile)
        // post(formData, formDataParameters, Urls.workOrderAdd)
        post(command, jsonParameters, Urls.workOrderSave)
      case ListWorkOrders(_, _) => post(command, jsonParameters, Urls.workOrdersList)

    handle(event, handler)

  // Uncomment to enable client formdata support.
  private def post(command: Command, parameters: RequestInit, url: String): Future[Event] =
    parameters.body = write[Command](command)
    // command: FormData | Command
    // parameters.body = if command.isInstanceOf[FormData] then command.asInstanceOf[FormData] else write[Command](command.asInstanceOf[Command]) 
    log(s"Proxy:post command: $command url: $url parameters: $parameters")
    (
      for
        response <- dom.fetch(url, parameters)
        text <- response.text()
      yield
        log(s"Proxy:post text: $text")
        val event = read[Event](text)
        log (s"Proxy:post event: $event")
        event
      ).recover { case error: Exception =>
        log(s"Proxy:post failure: ${error.getCause}")
        Fault(error)
      }

  private def handle(future: Future[Event], handler: (either: Either[Fault, Event]) => Unit): Unit =
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

  private def addWorkOrderToFormData(addWorkOrder: AddWorkOrder, imageFile: Option[ImageFile]): FormData =
    val workOrder = addWorkOrder.workOrder.copy(imageUrl = imageFile.fold("")(i => i.url))
    workOrderToFormData(addWorkOrder.copy(workOrder = workOrder), imageFile)

  private def saveWorkOrderToFormData(saveWorkOrder: SaveWorkOrder, imageFile: Option[ImageFile]): FormData =
    val workOrder = saveWorkOrder.workOrder.copy(imageUrl = imageFile.fold("")(i => i.url))
    workOrderToFormData(saveWorkOrder.copy(workOrder = workOrder), imageFile)

  private def workOrderToFormData(command: AddWorkOrder | SaveWorkOrder, imageFile: Option[ImageFile]): FormData =
    val formData = new FormData()
    log(s"*** fetcher: model image file: $imageFile")
    if (imageFile.isDefined) then
      val image = imageFile.get
      formData.append("imageFileName", image.filename)
      formData.append("image", image.file, image.filename)
      log(s"*** fetcher: real image file: ${image.filename}")
    else
      val filename = s"z-${DateTime.now}.txt"
      val file = new File(new js.Array(0), "delete me!")
      formData.append("imageFileName", filename)
      formData.append("image", file, filename)
      log("*** fetcher: fake image file:", filename)
    if command.isInstanceOf[AddWorkOrder] then formData.append("addWorkOrderAsJson", write[AddWorkOrder](command.asInstanceOf[AddWorkOrder]))
    else formData.append("saveWorkOrderAsJson", write[SaveWorkOrder](command.asInstanceOf[SaveWorkOrder]))
    log(s"formdata: $formData")
    formData