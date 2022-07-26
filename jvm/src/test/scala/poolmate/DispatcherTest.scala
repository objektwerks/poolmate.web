package poolmate

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import java.util.concurrent.TimeUnit

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.sys.process.Process

import Validators.*

class DispatcherTest extends AnyFunSuite with Matchers with LazyLogging:
  Process("psql -d poolmate -f ddl.sql").run().exitValue()

  val conf = ConfigFactory.load("test.server.conf")
  val store = Store(conf, Store.cache(minSize = 4, maxSize = 10, expireAfter = 24.hour))
  val emailSender = EmailSender(conf, store)
  val service = Service(store)
  val authorizer = Authorizer(service)
  val validator = Validator()
  val dispatcher = Dispatcher(authorizer, validator, service, emailSender)

  test("dispatcher") {
    testDispatcher(dispatcher, store)
    testEmail(store)
    testFault(store)
  }

  def testDispatcher(dispatcher: Dispatcher, store: Store): Unit = {
    var account = testJoin(dispatcher)
    store.listAccounts().head shouldBe account

    testEnter(dispatcher, account)
    account = testDeactivate(dispatcher, account)
    account = testReactivate(dispatcher, account)

    var pool = Pool(license = account.license, name = "test", built = 20010101, cost = 2500)
    pool = testAddPool(dispatcher, pool)
    testListPools(dispatcher, account)
    testUpdatePool(dispatcher, pool.copy(volume = 10000))

    var measurement = Measurement(poolId = pool.id, measured = 20010201)
    measurement = testAddMeasurement(dispatcher, pool, measurement)
    testListMeasurements(dispatcher, pool)
    testUpdateMeasurement(dispatcher, pool, measurement.copy(freeChlorine = 5))

    var cleaning = Cleaning(poolId = pool.id, cleaned = 20010201)
    cleaning = testAddCleaning(dispatcher, pool, cleaning)
    testListCleanings(dispatcher, pool)
    testUpdateCleaning(dispatcher, pool, cleaning.copy(deck = true))

    var chemical = Chemical(poolId = pool.id, added = 20010101, chemical = "chlorine", amount = 1, unit = UoM.gallon.abrv)
    chemical = testAddChemical(dispatcher, pool, chemical)
    testListChemicals(dispatcher, pool)
    testUpdateChemical(dispatcher, pool, chemical.copy(amount = 2))

    var supply = Supply(poolId = pool.id, purchased = 20010101, item = "chlorine", amount = 1, unit = UoM.gallon.abrv, cost = 5)
    supply = testAddSupply(dispatcher, pool, supply)
    testListSupplies(dispatcher, pool)
    testUpdateSupply(dispatcher, pool, supply.copy(cost = 6))

    var repair = Repair(poolId = pool.id, repaired = 20100101, repair = "pump", cost = 100)
    repair = testAddRepair(dispatcher, pool, repair)
    testListRepairs(dispatcher, pool)
    testUpdateRepair(dispatcher, pool, repair.copy(cost = 105))

    var pump = Pump(poolId = pool.id, installed = 20010101, model = "hayward", cost = 455)
    pump = testAddPump(dispatcher, pool, pump)
    testListPumps(dispatcher, pool)
    testUpdatePump(dispatcher, pool, pump.copy(model = "pentair"))

    var timer = Timer(poolId = pool.id, installed = 20010101, model = "intermatic", cost = 75)
    timer = testAddTimer(dispatcher, pool, timer)
    testListTimers(dispatcher, pool)
    testUpdateTimer(dispatcher, pool, timer.copy(model = "smartpool"))

    var timerSetting = TimerSetting(timerId = timer.id, dateSet = 20010101, timeOn = 800, timeOff = 1700)
    timerSetting = testAddTimerSetting(dispatcher, pool, timerSetting)
    testListTimerSettings(dispatcher, pool, timer)
    testUpdateTimerSetting(dispatcher, pool, timerSetting.copy(timeOff = 1730))

    var heater = Heater(poolId = pool.id, installed = 20010201, model = "hayward", cost = 2500)
    heater = testAddHeater(dispatcher, pool, heater)
    testListHeaters(dispatcher, pool)
    testUpdateHeater(dispatcher, pool, heater.copy(model = "pentair"))

    var heaterSetting = HeaterSetting(heaterId = heater.id, dateSet = 20011201, temp = 85)
    heaterSetting = testAddHeaterSetting(dispatcher, pool, heaterSetting)
    testListHeaterSettings(dispatcher, pool, heater)
    testUpdateHeaterSetting(dispatcher, pool, heaterSetting.copy(dateSet = 20020501))

    var surface = Surface(poolId = pool.id, installed = 20010101, kind = "concrete", cost = 3200)
    surface = testAddSurface(dispatcher, pool, surface)
    testListSurfaces(dispatcher, pool)
    testUpdateSurface(dispatcher, pool, surface.copy(kind = "pebble"))

    var deck = Deck(poolId = pool.id, installed = 20010103, kind = "tile", cost = 2400)
    deck = testAddDeck(dispatcher, pool, deck)
    testListDecks(dispatcher, pool)
    testUpdateDeck(dispatcher, pool, deck.copy(kind = "pavers"))
  }

  def testJoin(dispatcher: Dispatcher): Account =
    val command = Join(emailAddress = conf.getString("email.to"))
    dispatcher.dispatch(command) match
      case Joined(account) =>
        account.isActivated shouldBe true
        account
      case event: Event => logger.error(event.toString); fail()

  def testEnter(dispatcher: Dispatcher, account: Account): Unit =
    val command = Enter(account.emailAddress, account.pin)
    dispatcher.dispatch(command) match
      case loggedIn: Entered => account shouldBe loggedIn.account
      case event: Event => logger.error(event.toString); fail()

  def testDeactivate(dispatcher: Dispatcher, account: Account): Account =
    val command = Deactivate(account.license)
    dispatcher.dispatch(command) match
      case Deactivated(account) =>
        account.isDeactivated shouldBe true
        account
      case event: Event => logger.error(event.toString); fail()

  def testReactivate(dispatcher: Dispatcher, account: Account): Account =
    val command = Reactivate(account.license)
    dispatcher.dispatch(command) match
      case Reactivated(account) =>
        account.isActivated shouldBe true
        account
      case event: Event => logger.error(event.toString); fail()

  def testAddPool(dispatcher: Dispatcher, pool: Pool): Pool =
    val add = AddPool(pool.license, pool)
    dispatcher.dispatch(add) match
      case PoolAdded(pool: Pool) =>
        pool.id > 0 shouldBe true
        pool
      case event: Event => logger.error(event.toString); fail()

  def testListPools(dispatcher: Dispatcher, account: Account): Unit =
    val list = ListPools(account.license)
    dispatcher.dispatch(list) match
      case PoolsListed(pools) => pools.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdatePool(dispatcher: Dispatcher, pool: Pool): Unit =
    val update = UpdatePool(pool.license, pool)
    dispatcher.dispatch(update) shouldBe Updated()

  def testAddMeasurement(dispatcher: Dispatcher, pool: Pool, measurement: Measurement): Measurement =
    val add = AddMeasurement(pool.license, measurement)
    dispatcher.dispatch(add) match
      case MeasurementAdded(measurement: Measurement) =>
        measurement.id > 0 shouldBe true
        measurement
      case event: Event => logger.error(event.toString); fail()

  def testListMeasurements(dispatcher: Dispatcher, pool: Pool): Unit =
    val list = ListMeasurements(pool.license, pool.id)
    dispatcher.dispatch(list) match
      case MeasurementsListed(measurements) => measurements.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdateMeasurement(dispatcher: Dispatcher, pool: Pool, measurement: Measurement): Unit =
    val update = UpdateMeasurement(pool.license, measurement)
    dispatcher.dispatch(update) shouldBe Updated()

  def testAddCleaning(dispatcher: Dispatcher, pool: Pool, cleaning: Cleaning): Cleaning =
    val add = AddCleaning(pool.license, cleaning)
    dispatcher.dispatch(add) match
      case CleaningAdded(cleaning: Cleaning) =>
        cleaning.id > 0 shouldBe true
        cleaning
      case event: Event => logger.error(event.toString); fail()

  def testListCleanings(dispatcher: Dispatcher, pool: Pool): Unit =
    val list = ListCleanings(pool.license, pool.id)
    dispatcher.dispatch(list) match
      case CleaningsListed(cleanings) => cleanings.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdateCleaning(dispatcher: Dispatcher, pool: Pool, cleaning: Cleaning): Unit =
    val update = UpdateCleaning(pool.license, cleaning)
    dispatcher.dispatch(update) shouldBe Updated()

  def testAddChemical(dispatcher: Dispatcher, pool: Pool, chemical: Chemical): Chemical =
    val add = AddChemical(pool.license, chemical)
    dispatcher.dispatch(add) match
      case ChemicalAdded(chemical: Chemical) =>
        chemical.id > 0 shouldBe true
        chemical
      case event: Event => logger.error(event.toString); fail()

  def testListChemicals(dispatcher: Dispatcher, pool: Pool): Unit =
    val list = ListChemicals(pool.license, pool.id)
    dispatcher.dispatch(list) match
      case ChemicalsListed(chemicals) => chemicals.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdateChemical(dispatcher: Dispatcher, pool: Pool, chemical: Chemical): Unit =
    val update = UpdateChemical(pool.license, chemical)
    dispatcher.dispatch(update) shouldBe Updated()

  def testAddSupply(dispatcher: Dispatcher, pool: Pool, supply: Supply): Supply =
    val add = AddSupply(pool.license, supply)
    dispatcher.dispatch(add) match
      case SupplyAdded(supply: Supply) =>
        supply.id > 0 shouldBe true
        supply
      case event: Event => logger.error(event.toString); fail()

  def testListSupplies(dispatcher: Dispatcher, pool: Pool): Unit =
    val list = ListSupplies(pool.license, pool.id)
    dispatcher.dispatch(list) match
      case SuppliesListed(supplies) => supplies.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdateSupply(dispatcher: Dispatcher, pool: Pool, supply: Supply): Unit =
    val update = UpdateSupply(pool.license, supply)
    dispatcher.dispatch(update) shouldBe Updated()

  def testAddRepair(dispatcher: Dispatcher, pool: Pool, repair: Repair): Repair =
    val add = AddRepair(pool.license, repair)
    dispatcher.dispatch(add) match
      case RepairAdded(repair: Repair) =>
        repair.id > 0 shouldBe true
        repair
      case event: Event => logger.error(event.toString); fail()

  def testListRepairs(dispatcher: Dispatcher, pool: Pool): Unit =
    val list = ListRepairs(pool.license, pool.id)
    dispatcher.dispatch(list) match
      case RepairsListed(repairs) => repairs.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdateRepair(dispatcher: Dispatcher, pool: Pool, repair: Repair): Unit =
    val update = UpdateRepair(pool.license, repair)
    dispatcher.dispatch(update) shouldBe Updated()

  def testAddPump(dispatcher: Dispatcher, pool: Pool, pump: Pump): Pump =
    val add = AddPump(pool.license, pump)
    dispatcher.dispatch(add) match
      case PumpAdded(pump: Pump) =>
        pump.id > 0 shouldBe true
        pump
      case event: Event => logger.error(event.toString); fail()

  def testListPumps(dispatcher: Dispatcher, pool: Pool): Unit =
    val list = ListPumps(pool.license, pool.id)
    dispatcher.dispatch(list) match
      case PumpsListed(pumps) => pumps.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdatePump(dispatcher: Dispatcher, pool: Pool, pump: Pump): Unit =
    val update = UpdatePump(pool.license, pump)
    dispatcher.dispatch(update) shouldBe Updated()

  def testAddTimer(dispatcher: Dispatcher, pool: Pool, timer: Timer): Timer =
    val add = AddTimer(pool.license, timer)
    dispatcher.dispatch(add) match
      case TimerAdded(timer: Timer) =>
        timer.id > 0 shouldBe true
        timer
      case event: Event => logger.error(event.toString); fail()

  def testListTimers(dispatcher: Dispatcher, pool: Pool): Unit =
    val list = ListTimers(pool.license, pool.id)
    dispatcher.dispatch(list) match
      case TimersListed(timers) => timers.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdateTimer(dispatcher: Dispatcher, pool: Pool, timer: Timer): Unit =
    val update = UpdateTimer(pool.license, timer)
    dispatcher.dispatch(update) shouldBe Updated()

  def testAddTimerSetting(dispatcher: Dispatcher, pool: Pool, setting: TimerSetting): TimerSetting =
    val add = AddTimerSetting(pool.license, setting)
    dispatcher.dispatch(add) match
      case TimerSettingAdded(setting: TimerSetting) =>
        setting.id > 0 shouldBe true
        setting
      case event: Event => logger.error(event.toString); fail()

  def testListTimerSettings(dispatcher: Dispatcher, pool: Pool, timer: Timer): Unit =
    val list = ListTimerSettings(pool.license, timer.id)
    dispatcher.dispatch(list) match
      case TimerSettingsListed(settings) => settings.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdateTimerSetting(dispatcher: Dispatcher, pool: Pool, setting: TimerSetting): Unit =
    val update = UpdateTimerSetting(pool.license, setting)
    dispatcher.dispatch(update) shouldBe Updated()

  def testAddHeater(dispatcher: Dispatcher, pool: Pool, heater: Heater): Heater =
    val add = AddHeater(pool.license, heater)
    dispatcher.dispatch(add) match
      case HeaterAdded(heater: Heater) =>
        heater.id > 0 shouldBe true
        heater
      case event: Event => logger.error(event.toString); fail()

  def testListHeaters(dispatcher: Dispatcher, pool: Pool): Unit =
    val list = ListHeaters(pool.license, pool.id)
    dispatcher.dispatch(list) match
      case HeatersListed(heaters) => heaters.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdateHeater(dispatcher: Dispatcher, pool: Pool, heater: Heater): Unit =
    val update = UpdateHeater(pool.license, heater)
    dispatcher.dispatch(update) shouldBe Updated()

  def testAddHeaterSetting(dispatcher: Dispatcher, pool: Pool, setting: HeaterSetting): HeaterSetting =
    val add = AddHeaterSetting(pool.license, setting)
    dispatcher.dispatch(add) match
      case HeaterSettingAdded(setting: HeaterSetting) =>
        setting.id > 0 shouldBe true
        setting
      case event: Event => logger.error(event.toString); fail()

  def testListHeaterSettings(dispatcher: Dispatcher, pool: Pool, heater: Heater): Unit =
    val list = ListHeaterSettings(pool.license, heater.id)
    dispatcher.dispatch(list) match
      case HeaterSettingsListed(settings) => settings.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdateHeaterSetting(dispatcher: Dispatcher, pool: Pool, setting: HeaterSetting): Unit =
    val update = UpdateHeaterSetting(pool.license, setting)
    dispatcher.dispatch(update) shouldBe Updated()

  def testAddSurface(dispatcher: Dispatcher, pool: Pool, surface: Surface): Surface =
    val add = AddSurface(pool.license, surface)
    dispatcher.dispatch(add) match
      case SurfaceAdded(surface: Surface) =>
        surface.id > 0 shouldBe true
        surface
      case event: Event => logger.error(event.toString); fail()

  def testListSurfaces(dispatcher: Dispatcher, pool: Pool): Unit =
    val list = ListSurfaces(pool.license, pool.id)
    dispatcher.dispatch(list) match
      case SurfacesListed(surfaces) => surfaces.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdateSurface(dispatcher: Dispatcher, pool: Pool, surface: Surface): Unit =
    val update = UpdateSurface(pool.license, surface)
    dispatcher.dispatch(update) shouldBe Updated()

  def testAddDeck(dispatcher: Dispatcher, pool: Pool, deck: Deck): Deck =
    val add = AddDeck(pool.license, deck)
    dispatcher.dispatch(add) match
      case DeckAdded(deck: Deck) =>
        deck.id > 0 shouldBe true
        deck
      case event: Event => logger.error(event.toString); fail()

  def testListDecks(dispatcher: Dispatcher, pool: Pool): Unit =
    val list = ListDecks(pool.license, pool.id)
    dispatcher.dispatch(list) match
      case DecksListed(decks) => decks.size shouldBe 1
      case event: Event => logger.error(event.toString); fail()

  def testUpdateDeck(dispatcher: Dispatcher, pool: Pool, deck: Deck): Unit =
    val update = UpdateDeck(pool.license, deck)
    dispatcher.dispatch(update) shouldBe Updated()

  def testEmail(store: Store): Unit =
    store.listUnprocessedEmails.size shouldBe 1
    val email = store.listUnprocessedEmails.head
    store.processEmail(email.copy(processed = true, valid = true))
    store.listUnprocessedEmails.size shouldBe 0

  def testFault(store: Store): Unit =
    val fault = Fault("fault")
    store.addFault(fault)
    store.listFaults.size shouldBe 1