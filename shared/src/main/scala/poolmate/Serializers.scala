package poolmate

object Serializers:
  import upickle.default.*

  given userRW: ReadWriter[User] = macroRW
  given workOrderRW: ReadWriter[WorkOrder] = macroRW