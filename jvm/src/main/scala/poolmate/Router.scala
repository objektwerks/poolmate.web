package poolmate

import cask.main.Routes
import cask.model.{Request, Response}
import com.typesafe.scalalogging.LazyLogging

import java.time.Instant

import upickle.default.{read, write}

import Serializer.given

final class Router(handler: Handler) extends Routes with LazyLogging:
  @cask.get("/now")
  def index() = Response(Instant.now.toString)

  @cask.post("/register")
  def register(request: Request) =
    val register = read[Register](request.text())
    val registered = handler.register(register)
    write[Registered](registered)

  @cask.post("/login")
  def login(request: Request) =
    val login = read[Login](request.text())
    val loggedIn = handler.login(login)
    write[LoggedIn](loggedIn)

  initialize()