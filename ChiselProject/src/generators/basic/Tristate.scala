import chisel3._
import chisel3.util._


/** Bundle representing a tristate pin.
  */
class Tristate extends Bundle {
  val data = Bool()
}