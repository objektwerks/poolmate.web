package poolmate

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import java.util.concurrent.TimeUnit

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.sys.process.Process

import Validator.*

class DispatcherTest extends AnyFunSuite with Matchers with LazyLogging:
  Process("psql -d poolmate -f ddl.sql").run().exitValue() // TODO

  val conf = ConfigFactory.load("test.server.conf")

  val emailer = Emailer(conf)
  val store = Store(conf, Store.cache(minSize = 4, maxSize = 10, expireAfter = 24.hour))
  val service = Service(store)
  val dispatcher = Dispatcher(emailer, service)

  test("dispatcher") {
    // TODO
  }

  def testRegister(dispatcher: Dispatcher): Unit = ()

  def testLogin(dispatcher: Dispatcher, account: Account): Unit = ()