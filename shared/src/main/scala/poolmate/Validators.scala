package poolmate

object Validators:
  extension (value: String)
    def isLicense: Boolean = if value.nonEmpty then value.length == 36 else false
    def isEmailAddress: Boolean = value.nonEmpty && value.length >= 3 && value.contains("@")
    def isPin: Boolean = value.length == 7
    def isName: Boolean = value.length >= 2 && value.length <= 24

  extension (value: Int)
    def isGreaterThan1899 = value > 1899
    def isGreaterThan999 = value > 999

  extension (id: Long)
    def isZero: Boolean = id == 0
    def isGreaterThanZero: Boolean = id > 0

  extension (login: Enter)
    def isValid: Boolean = login.pin.isPin

  extension (deactivate: Deactivate)
    def isValid: Boolean = deactivate.license.isLicense

  extension (reactivate: Reactivate)
    def isValid: Boolean = reactivate.license.isLicense

  extension (account: Account)
    def isActivated: Boolean =
      account.id >= 0 &&
      account.license.isLicense &&
      account.pin.isPin &&
      account.activated > 0 &&
      account.deactivated == 0
    def isDeactivated: Boolean =
      account.id >= 0 &&
      account.license.isLicense &&
      account.pin.isPin &&
      account.activated == 0 &&
      account.deactivated > 0

  extension (pool: Pool) def isValid =
    pool.id >= 0 &&
    pool.license.isLicense &&
    pool.name.isName &&
    pool.built > 0 &&
    pool.volume >= 1000 &&
    pool.cost > 0

  extension (measurement: Measurement)
    def isValid: Boolean =
      import Measurement.*
      measurement.id >= 0 &&
      measurement.poolId > 0 &&
      measurement.measured > 0 &&
      tempRange.contains(measurement.temp) &&
      totalHardnessRange.contains(measurement.totalHardness) &&
      totalChlorineRange.contains(measurement.totalChlorine) &&
      totalBromineRange.contains(measurement.totalBromine) &&
      freeChlorineRange.contains(measurement.freeChlorine) &&
      (measurement.ph >= 6.2 && measurement.ph <= 8.4) &&
      totalAlkalinityRange.contains(measurement.totalAlkalinity) &&
      cyanuricAcidRange.contains(measurement.cyanuricAcid)

  extension (cleaning: Cleaning)
    def isValid: Boolean =
      cleaning.id >= 0 &&
      cleaning.poolId > 0 &&
      cleaning.cleaned > 0

  extension (chemical: Chemical)
    def isValid: Boolean =
      chemical.id >= 0 &&
      chemical.poolId > 0 &&
      chemical.added > 0 &&
      chemical.chemical.nonEmpty &&
      chemical.amount > 0 &&
      chemical.unit.nonEmpty

  extension (supply: Supply)
    def isValid: Boolean =
      supply.id >= 0 &&
      supply.poolId > 0 &&
      supply.purchased > 0 &&
      supply.item.nonEmpty &&
      supply.amount > 0 &&
      supply.unit.nonEmpty &&
      supply.cost > 0

  extension (repair: Repair)
    def isValid: Boolean =
      repair.id >= 0 &&
      repair.poolId > 0 &&
      repair.repaired > 0 &&
      repair.repair.nonEmpty &&
      repair.cost > 0

  extension (pump: Pump)
    def isValid: Boolean =
      pump.id >= 0 &&
      pump.poolId > 0 &&
      pump.installed > 0 &&
      pump.model.nonEmpty &&
      pump.cost > 0

  extension (timer: Timer)
    def isValid: Boolean =
      timer.id >= 0 &&
      timer.poolId > 0 &&
      timer.installed > 0 &&
      timer.model.nonEmpty &&
      timer.cost > 0

  extension (timerSetting: TimerSetting)
    def isValid: Boolean =
      timerSetting.id >= 0 &&
      timerSetting.timerId > 0 &&
      timerSetting.dateSet > 0 &&
      timerSetting.timeOn > 0 &&
      timerSetting.timeOff > 0 &&
      timerSetting.timeOff > timerSetting.timeOn

  extension (heater: Heater)
    def isValid: Boolean =
      heater.id >= 0 &&
      heater.poolId > 0 &&
      heater.installed > 0 &&
      heater.model.nonEmpty &&
      heater.cost > 0

  extension (heaterSetting: HeaterSetting)
    def isValid: Boolean =
      heaterSetting.id >= 0 &&
      heaterSetting.heaterId > 0 &&
      heaterSetting.temp > 0 &&
      heaterSetting.dateSet > 0

  extension (surface: Surface)
    def isValid: Boolean =
      surface.id >= 0 &&
      surface.poolId > 0 &&
      surface.installed > 0 &&
      surface.kind.nonEmpty &&
      surface.cost > 0

  extension (deck: Deck)
    def isValid: Boolean =
      deck.id >= 0 &&
      deck.poolId > 0 &&
      deck.installed > 0 &&
      deck.kind.nonEmpty &&
      deck.cost > 0