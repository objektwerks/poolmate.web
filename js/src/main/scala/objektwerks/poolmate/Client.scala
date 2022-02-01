package objektwerks.poolmate

import com.raquo.laminar.api.L._

import org.scalajs.dom._
import org.scalajs.dom.console.log

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("Client")
class Client(apiUrl: String) extends js.Object:
  log(s"api url: $apiUrl")
  render(
    container = document.getElementById("container"),
    rootNode = div( child <-- PageRouter.splitter.$view )
  )